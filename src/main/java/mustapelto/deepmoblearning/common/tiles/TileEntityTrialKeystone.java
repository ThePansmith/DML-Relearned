package mustapelto.deepmoblearning.common.tiles;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import mustapelto.deepmoblearning.DMLConstants;
import mustapelto.deepmoblearning.common.capability.CapabilityPlayerTrial;
import mustapelto.deepmoblearning.common.capability.CapabilityPlayerTrialProvider;
import mustapelto.deepmoblearning.common.entities.EntityGlitch;
import mustapelto.deepmoblearning.common.inventory.ContainerTileEntity;
import mustapelto.deepmoblearning.common.inventory.ContainerTrialKeystone;
import mustapelto.deepmoblearning.common.inventory.ItemHandlerTrialKey;
import mustapelto.deepmoblearning.common.network.*;
import mustapelto.deepmoblearning.common.trials.AttunementData;
import mustapelto.deepmoblearning.common.trials.affix.TrialAffix;
import mustapelto.deepmoblearning.common.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static mustapelto.deepmoblearning.DMLConstants.Sounds;
import static mustapelto.deepmoblearning.DMLConstants.Trials;

public class TileEntityTrialKeystone extends TileEntityContainer implements ITickable {

    private final ItemHandlerTrialKey trialKey = new ItemHandlerTrialKey();
    private final Set<EntityPlayerMP> participants = Collections.newSetFromMap(new WeakHashMap<>());

    private ItemStack activeTrialKey = ItemStack.EMPTY;
    private AttunementData activeTrialData;

    private int currentWave = 0;
    private int mobsDefeated = 0;
    private int mobsSpawned = 0;
    private boolean active = false;
    private int ticksToNextWave = 0;
    private long tickCount = 0;
    private ImmutableList<TrialAffix> affixes = ImmutableList.of();

    @Override
    public void update() {
        tickCount++;
        if (world.isRemote)
            return;

        if (isTrialActive()) {
            disableFlying();
            removeDistantParticipants();

            if (participants.isEmpty()) {
                List<EntityPlayerMP> nearbyPlayers = PlayerHelper.getLivingPlayersInArea(world, getPos(), 80, 60, -30);
                nearbyPlayers.forEach(p -> p.sendMessage(new TextComponentTranslation("deepmoblearning.trial.message.failed")));
                nearbyPlayers.forEach(this::resetCapability);
                stopTrial(true, false);
            } else progressTrial();
        }

        // Every 5 seconds
        if (tickCount % 100 == 0) markDirty();
    }

    private void progressTrial() {
        if (ticksToNextWave > 0) {
            ticksToNextWave--;
            if (ticksToNextWave == 0) {
                startNextWave();
            }
            // Return early during wave intermission
            return;
        } else if (currentWave <= getLastWave()) {
            if (mobsSpawned < getWaveMobTotal()) {
                if (tickCount % (20 * getSpawnDelay()) == 0) {
                    spawnTrialMob();
                }
            }

            if (mobsDefeated >= getWaveMobTotal()) {
                if (currentWave == (getLastWave() - 1)) {
                    stopTrial(false, true);
                } else {
                    ticksToNextWave = 100;
                    participants.forEach(participant -> PlayerHelper.sendMessageToOverlay(participant, Trials.Message.WAVE_COUNTDOWN));
                    SoundHelper.playSound(world, pos, Sounds.WAVE_COUNTDOWN);
                }
            }
        } else {
            setDefaultTrialState();
        }

        affixes.forEach(TrialAffix::run);

        // Every 14 seconds
        if (tickCount % 280 == 0) spawnGlitch();

        // Every 15 seconds
        if (tickCount % 300 == 0) updateState();
    }

    //
    // GUI called functions
    //

    public void startTrial() {
        if (!canStartTrial())
            return;

        if (world.isRemote) {
            DMLPacketHandler.sendToServer(new MessageTrialStart(this));
        } else {
            markDirty();

            activeTrialKey = getTrialKey().copy();
            // trialKey.setStackInSlot(0, ItemStack.EMPTY);
            activeTrialData = TrialKeyHelper.getAttunement(activeTrialKey).orElse(null);
            if (activeTrialData == null) {
                // Invalid Trial Key -> abort Trial
                stopTrial(true, false);
                return;
            }

            participants.addAll(
                    PlayerHelper.getLivingPlayersInArea(
                            world,
                            pos,
                            DMLConstants.TrialKeystone.TRIAL_AREA_RADIUS,
                            DMLConstants.TrialKeystone.TRIAL_AREA_HEIGHT,
                            0
                    )
            );

            affixes = TrialKeyHelper.getAffixes(activeTrialKey, pos, world);
            trialKey.setStackInSlot(0, ItemStack.EMPTY);
            setActive(true);

            updateCapability();
            updateState();
            onWaveStart();
        }
    }

    public void stopTrial(boolean abort, boolean sendMessages) {
        affixes.forEach(TrialAffix::cleanUp);
        if (!abort) {
            if (sendMessages) {
                participants.forEach(p -> PlayerHelper.sendMessageToOverlay(p, Trials.Message.TRIAL_COMPLETE));
                SoundHelper.playSound(world, pos, Sounds.TRIAL_WON);
            }

            ImmutableList<ItemStack> rewards = activeTrialData.getRewards();
            rewards.forEach(stack -> {
                EntityItem item = new EntityItem(world, pos.getX(), pos.getY() + 2, pos.getZ(), stack);
                item.setDefaultPickupDelay();
                world.spawnEntity(item);
            });
        } else if (isTrialActive() && sendMessages) {
            participants.forEach(p -> PlayerHelper.sendMessageToOverlay(p, Trials.Message.TRIAL_ABORT));
        }
        setDefaultTrialState();
    }

    //
    // Inventory
    //

    @Override
    public ContainerTileEntity getContainer(InventoryPlayer inventoryPlayer) {
        return new ContainerTrialKeystone(this, inventoryPlayer);
    }

    public ItemStack getTrialKey() {
        return trialKey.getStackInSlot(0);
    }

    public boolean hasTrialKey() {
        return ItemStackHelper.isTrialKey(getTrialKey());
    }

    public void setTrialKey(ItemStack stack) {
        trialKey.setStackInSlot(0, stack);
    }

    //
    // Trial state and conditions
    //

    private void onWaveStart() {
        SoundHelper.playSound(world, pos, Sounds.WAVE_START);
        participants.forEach(p -> PlayerHelper.sendMessageToOverlay(p, Trials.Message.WAVE_NUMBER));
    }

    private void startNextWave() {
        currentWave++;
        mobsDefeated = 0;
        mobsSpawned = 0;
        participants.clear();
        participants.addAll(PlayerHelper.getLivingPlayersInArea(world, pos, DMLConstants.TrialKeystone.TRIAL_AREA_RADIUS, DMLConstants.TrialKeystone.TRIAL_AREA_HEIGHT, 0));
        participants.forEach(p -> DMLPacketHandler.sendToClientPlayer(new MessageUpdateTileEntity(this), p));
        updateCapability();
        onWaveStart();
    }

    public void onPlayerDied(EntityPlayerMP player) {
        participants.remove(player);
        resetCapability(player);
    }

    public void onMobDied() {
        mobsDefeated++;
        updateCapability();
    }

    public boolean isTrialAreaClear() {
        int groundY = pos.getY() - 1;
        int keystoneY = pos.getY();
        int areaMaxY = pos.getY() + DMLConstants.TrialKeystone.TRIAL_AREA_HEIGHT;
        int areaMinX = pos.getX() - DMLConstants.TrialKeystone.TRIAL_AREA_RADIUS;
        int areaMaxX = pos.getX() + DMLConstants.TrialKeystone.TRIAL_AREA_RADIUS;
        int areaMinZ = pos.getZ() - DMLConstants.TrialKeystone.TRIAL_AREA_RADIUS;
        int areaMaxZ = pos.getZ() + DMLConstants.TrialKeystone.TRIAL_AREA_RADIUS;

        // Check if layer below Trial area is "ground"
        Iterable<BlockPos> groundLayer = BlockPos.getAllInBox(areaMinX, groundY, areaMinZ, areaMaxX, groundY, areaMaxZ);
        for(BlockPos blockPos : groundLayer) {
            if (!world.getBlockState(blockPos).isFullBlock())
                return false;
        }

        // Check if layers above Trial area are "air"
        Iterable<BlockPos> airLayer = BlockPos.getAllInBox(areaMinX, keystoneY, areaMinZ, areaMaxX, areaMaxY, areaMaxZ);
        for (BlockPos blockPos : airLayer) {
            if (blockPos.equals(this.pos))
                continue; // Skip Trial Keystone block

            IBlockState state = world.getBlockState(blockPos);
            Block block = state.getBlock();
            if (!block.isAir(state, world, blockPos))
                return false;
        }

        return true;
    }

    public boolean isTrialActive() {
        return active;
    }

    private boolean canStartTrial() {
        return hasTrialKey() && TrialKeyHelper.isAttuned(getTrialKey()) && !isTrialActive() && isTrialAreaClear();
    }

    public int getCurrentWave() {
        return currentWave;
    }

    public int getLastWave() {
        return activeTrialData != null ? activeTrialData.getMaxWave() : 0;
    }

    public int getMobsDefeated() {
        return mobsDefeated;
    }

    public int getWaveMobTotal() {
        return activeTrialData != null ? activeTrialData.getCurrentWaveMobTotal(currentWave) : 0;
    }

    public double getSpawnDelay() {
        return activeTrialData != null ? activeTrialData.getSpawnDelay() : 0;
    }

    @Nullable
    public AttunementData getTrialData() {
        return TrialKeyHelper.getAttunement(getTrialKey()).orElse(null);
    }

    //
    // Trial actions
    //

    private void disableFlying() {
        participants.forEach(p -> {
            if (!p.isDead && !p.capabilities.isCreativeMode && p.capabilities.allowFlying) {
                p.capabilities.allowFlying = false;
                p.capabilities.isFlying = false;
                p.sendPlayerAbilities();
            }
        });
    }

    private void removeDistantParticipants() {
        Iterator<EntityPlayerMP> iterator = participants.iterator();
        while (iterator.hasNext()) {
            EntityPlayerMP player = iterator.next();
            double distance = BlockDistance.getBlockDistance(getPos(), player.getPosition());
            if (distance > DMLConstants.TrialKeystone.TRIAL_ARENA_RADIUS) {

                player.sendMessage(new TextComponentTranslation("deepmoblearning.trial.message.player_left"));
                resetCapability(player);
                iterator.remove();
            }
        }
    }

    private void spawnTrialMob() {
        EntityLiving e = activeTrialData.getRandomEntity(world).orElse(null);
        if (e == null) return; // todo log? close the trial? do nothing?
        ThreadLocalRandom rand = ThreadLocalRandom.current();

        int randomX = pos.getX() + rand.nextInt(-5, 5);
        int randomY = pos.getY() + rand.nextInt(0, 1);
        int randomZ = pos.getZ() + rand.nextInt(-5, 5);

        e.setLocationAndAngles(randomX, randomY, randomZ, 0 ,0);
        e.getEntityData().setLong(Trials.TRIAL_KEYSTONE_POS, getPos().toLong());
        e.enablePersistence();

        EntityPlayer target = e.world.getNearestAttackablePlayer(e.getPosition(), 32, 5);
        if(target != null && target.isEntityAlive()) {
            e.setAttackTarget(target);
        }

        affixes.forEach(affix -> affix.apply(e));

        // Do not spawn them all at once (once every 2 sec atm)
        world.spawnEntity(e);
        mobsSpawned++;
    }

    private void spawnGlitch() {
        // Spawn randomly within the confines of the trial
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        int randomX = pos.getX() + rand.nextInt(-5, 5);
        int randomY = pos.getY() + rand.nextInt(0, 1);
        int randomZ = pos.getZ() + rand.nextInt(-5, 5);

        // TODO based on tier??
        if(rand.nextInt(1, 100) <= activeTrialData.getGlitchChance()) {
            EntityGlitch e = new EntityGlitch(world);
            e.setLocationAndAngles(randomX, randomY, randomZ, 0, 0);
            e.enablePersistence();

            EntityPlayer target = e.world.getNearestAttackablePlayer(e.getPosition(), 32, 5);
            if(target != null  && target.isEntityAlive()) {
                e.setAttackTarget(target);
            }

            affixes.forEach(affix -> affix.applyToGlitch(e));

            world.spawnEntity(e);

            participants.forEach(p -> PlayerHelper.sendMessageToOverlay(p, Trials.Message.GLITCH_NOTIF));
            SoundHelper.playSound(world, this.getPos(), Sounds.GLITCH_ALERT);
        }
    }

    private void updateCapability() {
        participants.forEach(p -> {
            CapabilityPlayerTrial cap = (CapabilityPlayerTrial) p.getCapability(CapabilityPlayerTrialProvider.PLAYER_TRIAL_CAP, null);
            cap.setWaveMobTotal(getWaveMobTotal());
            cap.setCurrentWave(currentWave);
            cap.setDefeated(mobsDefeated);
            cap.setLastWave(getLastWave());
            cap.setTilePos(pos.toLong());
            cap.setIsActive(active);
            cap.sync(p);
        });
    }

    private void resetCapability(EntityPlayerMP player) {
        CapabilityPlayerTrial cap = (CapabilityPlayerTrial) player.getCapability(CapabilityPlayerTrialProvider.PLAYER_TRIAL_CAP, null);
        cap.setWaveMobTotal(0);
        cap.setCurrentWave(0);
        cap.setDefeated(0);
        cap.setLastWave(0);
        cap.setTilePos(0);
        cap.setIsActive(false);
        cap.sync(player);
    }

    //
    // RENDER
    //

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(getPos(), getPos().add(1, 2, 1));
    }


    //
    // SERVER/CLIENT SYNC
    //

    private void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public ByteBuf getUpdateData() {
        ByteBuf buf = super.getUpdateData();
        buf.writeBoolean(active);
        buf.writeInt(currentWave);
        ByteBufUtils.writeItemStack(buf, activeTrialKey);
        return buf;
    }

    @Override
    public void handleUpdateData(ByteBuf buf) {
        super.handleUpdateData(buf);
        this.active = buf.readBoolean();
        this.currentWave = buf.readInt();
        this.activeTrialKey = ByteBufUtils.readItemStack(buf);
        this.activeTrialData = TrialKeyHelper.getAttunement(activeTrialKey).orElse(null);
    }

    public void updateState() {
        IBlockState state = world.getBlockState(getPos());
        world.notifyBlockUpdate(getPos(), state, state, 3);
    }

    //
    // CAPABILITIES
    //

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) ||
                super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(trialKey);

        return super.getCapability(capability, facing);
    }

    //
    // NBT WRITE/READ
    //

    // NBT Tag Names
    private static final String NBT_TRIAL_KEY = "trialKey";
    private static final String NBT_TRIAL_STATE = "trialState";
    private static final String NBT_ACTIVE_TRIAL_KEY = "activeTrialKey";
    private static final String NBT_CURRENT_WAVE = "currentWave";
    private static final String NBT_MOBS_DEFEATED = "mobsDefeated";

    // Legacy NBT Tag Names
    private static final String NBT_LEGACY_TRIAL_KEY = "inventory";

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        NBTTagCompound inventory = new NBTTagCompound();
        inventory.setTag(NBT_TRIAL_KEY, trialKey.serializeNBT());
        compound.setTag(NBT_INVENTORY, inventory);

        NBTTagCompound trialState = new NBTTagCompound();
        NBTTagCompound activeTrialKeyNBT = new NBTTagCompound();
        activeTrialKey.writeToNBT(activeTrialKeyNBT);
        trialState.setTag(NBT_ACTIVE_TRIAL_KEY, activeTrialKeyNBT);
        compound.setTag(NBT_TRIAL_STATE, trialState);

        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        if (isLegacyNBT(compound)) {
            // Original DML tag -> read Trial Key from legacy inventory tag and set Trial State to default values
            trialKey.deserializeNBT(compound.getCompoundTag(NBT_LEGACY_TRIAL_KEY));
            setDefaultTrialState();
        } else {
            // DML:Relearned tag -> use new (nested) tag names
            NBTTagCompound inventory = compound.getCompoundTag(NBT_INVENTORY);
            trialKey.deserializeNBT(inventory.getCompoundTag(NBT_TRIAL_KEY));
            readTrialStateFromNBT(compound.getCompoundTag(NBT_TRIAL_STATE));
        }
    }

    // TODO get more info here
    private void readTrialStateFromNBT(NBTTagCompound compound) {
        currentWave = compound.getInteger(NBT_CURRENT_WAVE);
        mobsDefeated = compound.getInteger(NBT_MOBS_DEFEATED);
        // If world is closed while Trial is running, already spawned but not defeated mobs will despawn. We can get them back by resetting the count to the number of defeated mobs.
        mobsSpawned = mobsDefeated;

        activeTrialKey = new ItemStack(compound.getCompoundTag(NBT_ACTIVE_TRIAL_KEY));
        activeTrialData = TrialKeyHelper.getAttunement(activeTrialKey).orElse(null);
        if (activeTrialData == null) stopTrial(true, false);
    }

    private void setDefaultTrialState() {
        active = false;
        mobsSpawned = 0;
        mobsDefeated = 0;
        currentWave = 0;
        ticksToNextWave = 0;
        activeTrialKey = ItemStack.EMPTY;
        activeTrialData = null;
        affixes = ImmutableList.of();
        updateCapability();
        participants.clear();
    }

    private static boolean isLegacyNBT(NBTTagCompound nbt) {
        return !nbt.hasKey(NBT_TRIAL_STATE);
    }
}