package mustapelto.deepmoblearning.common.tiles;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import mustapelto.deepmoblearning.DMLConstants;
import mustapelto.deepmoblearning.common.entities.EntityGlitch;
import mustapelto.deepmoblearning.common.network.DMLPacketHandler;
import mustapelto.deepmoblearning.common.network.MessageUpdateTileEntity;
import mustapelto.deepmoblearning.common.trials.AttunementData;
import mustapelto.deepmoblearning.common.trials.affix.TrialAffix;
import mustapelto.deepmoblearning.common.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

// TODO Organize and clean up
// TODO Fix in-progress trials not running on world load
public class TrialHandler {

    private final TileEntityTrialKeystone te;
    private final Set<EntityPlayerMP> participants = Collections.newSetFromMap(new WeakHashMap<>());

    private ItemStack activeTrialKey;
    private boolean active;
    private int currentWave;
    private int mobsDefeated;
    private int mobsSpawned;
    private int ticksToNextWave;

    protected TrialHandler(TileEntityTrialKeystone te) {
        this.te = te;
    }

    protected void runTrial() {
        disableFlying();
        removeDistantParticipants();

        if (participants.isEmpty()) {

            // End the trial, all participants have left or died
            List<EntityPlayerMP> nearbyPlayers = PlayerHelper.getLivingPlayersInArea(getWorld(), getPos(), 80, 60, -30);
            nearbyPlayers.forEach(p -> p.sendMessage(new TextComponentTranslation("deepmoblearning.trial.message.failed")));
            nearbyPlayers.forEach(PlayerHelper::resetTrialCapability);
            stopTrial(true, false);
            return;

        } else if (ticksToNextWave > 0) {

            // Progress the wave, and advance to next wave if needed
            ticksToNextWave--;
            if (ticksToNextWave == 0) {
                startNextWave();
            }
            // Return early during wave intermission
            return;

        } else if (currentWave <= getLastWave()) {

            // Spawn mobs during the current wave if there are any left to spawn
            if (mobsSpawned < getWaveMobTotal()) {
                if (getTimer() % (20 * getSpawnDelay()) == 0) {
                    EntityHelper.spawnTrialMob(getRandomTrialEntity(), getWorld(), getPos(), getAffixes());
                    mobsSpawned++;
                }
            }

            // Complete the trial if all waves are finished
            if (mobsDefeated >= getWaveMobTotal()) {
                if (currentWave == (getLastWave() - 1)) {
                    stopTrial(false, true);
                    return;
                } else {
                    ticksToNextWave = 100;
                    participants.forEach(participant -> PlayerHelper.sendMessageToOverlay(participant, DMLConstants.Trials.Message.WAVE_COUNTDOWN));
                    SoundHelper.playSound(getWorld(), getPos(), DMLConstants.Sounds.WAVE_COUNTDOWN);
                }
            }
        } else {

            // End the trial
            resetTrial();
            return;
        }

        // Run affixes always while the trial is running
        getAffixes().forEach(TrialAffix::run);

        // Every 14 seconds
        if (getTimer() % 280 == 0) spawnGlitch();
    }

    protected boolean startTrial(ItemStack trialKey) {
        activeTrialKey = trialKey.copy();
        if (getData() == null) {
            // Invalid Trial Key -> abort Trial
            stopTrial(true, false);
        }

        participants.addAll(
                PlayerHelper.getLivingPlayersInArea(
                        getWorld(),
                        getPos(),
                        DMLConstants.TrialKeystone.TRIAL_AREA_RADIUS,
                        DMLConstants.TrialKeystone.TRIAL_AREA_HEIGHT,
                        0
                )
        );

        active = true;

        updateCapability();
        onWaveStart();
        return true;
    }

    protected void stopTrial(boolean abort, boolean sendMessages) {
        getAffixes().forEach(TrialAffix::cleanUp);
        if (!abort) {
            if (sendMessages) {
                participants.forEach(p -> PlayerHelper.sendMessageToOverlay(p, DMLConstants.Trials.Message.TRIAL_COMPLETE));
                SoundHelper.playSound(getWorld(), getPos(), DMLConstants.Sounds.TRIAL_WON);
            }

            ImmutableList<ItemStack> rewards = getRewards();
            rewards.forEach(stack -> {
                EntityItem item = new EntityItem(getWorld(), getPos().getX(), getPos().getY() + 2, getPos().getZ(), stack);
                item.setDefaultPickupDelay();
                getWorld().spawnEntity(item);
            });
        } else if (isTrialActive() && sendMessages) {
            participants.forEach(p -> PlayerHelper.sendMessageToOverlay(p, DMLConstants.Trials.Message.TRIAL_ABORT));
        }
        resetTrial();
    }

    private void onWaveStart() {
        SoundHelper.playSound(getWorld(), getPos(), DMLConstants.Sounds.WAVE_START);
        participants.forEach(p -> PlayerHelper.sendMessageToOverlay(p, DMLConstants.Trials.Message.WAVE_NUMBER));
    }

    private void startNextWave() {
        currentWave++;
        mobsDefeated = 0;
        mobsSpawned = 0;
        participants.clear();
        participants.addAll(PlayerHelper.getLivingPlayersInArea(getWorld(), getPos(), DMLConstants.TrialKeystone.TRIAL_AREA_RADIUS, DMLConstants.TrialKeystone.TRIAL_AREA_HEIGHT, 0));
        participants.forEach(p -> DMLPacketHandler.sendToClientPlayer(new MessageUpdateTileEntity(te), p));
        updateCapability();
        onWaveStart();
    }

    protected void onPlayerDied(EntityPlayerMP player) {
        participants.remove(player);
        PlayerHelper.resetTrialCapability(player);
    }

    protected void onMobDied() {
        mobsDefeated++;
        updateCapability();
    }

    protected boolean isTrialActive() {
        return active;
    }

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
                PlayerHelper.resetTrialCapability(player);
                iterator.remove();
            }
        }
    }

    private void spawnGlitch() {
        // Spawn randomly within the confines of the trial
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        int randomX = getPos().getX() + rand.nextInt(-5, 5);
        int randomY = getPos().getY() + rand.nextInt(0, 1);
        int randomZ = getPos().getZ() + rand.nextInt(-5, 5);

        // TODO based on tier??
        if(rand.nextInt(1, 100) <= getGlitchChance()) {
            EntityGlitch e = new EntityGlitch(getWorld());
            e.setLocationAndAngles(randomX, randomY, randomZ, 0, 0);
            e.enablePersistence();

            EntityPlayer target = e.world.getNearestAttackablePlayer(e.getPosition(), 32, 5);
            if(target != null  && target.isEntityAlive()) {
                e.setAttackTarget(target);
            }

            getAffixes().forEach(affix -> affix.applyToGlitch(e));

            getWorld().spawnEntity(e);

            participants.forEach(p -> PlayerHelper.sendMessageToOverlay(p, DMLConstants.Trials.Message.GLITCH_NOTIF));
            SoundHelper.playSound(getWorld(), getPos(), DMLConstants.Sounds.GLITCH_ALERT);
        }
    }

    protected boolean isTrialAreaClear() {
        int groundY = getPos().getY() - 1;
        int keystoneY = getPos().getY();
        int areaMaxY = getPos().getY() + DMLConstants.TrialKeystone.TRIAL_AREA_HEIGHT;
        int areaMinX = getPos().getX() - DMLConstants.TrialKeystone.TRIAL_AREA_RADIUS;
        int areaMaxX = getPos().getX() + DMLConstants.TrialKeystone.TRIAL_AREA_RADIUS;
        int areaMinZ = getPos().getZ() - DMLConstants.TrialKeystone.TRIAL_AREA_RADIUS;
        int areaMaxZ = getPos().getZ() + DMLConstants.TrialKeystone.TRIAL_AREA_RADIUS;

        // Check if layer below Trial area is "ground"
        Iterable<BlockPos> groundLayer = BlockPos.getAllInBox(areaMinX, groundY, areaMinZ, areaMaxX, groundY, areaMaxZ);
        for(BlockPos blockPos : groundLayer) {
            if (!getWorld().getBlockState(blockPos).isFullBlock())
                return false;
        }

        // Check if layers above Trial area are "air"
        Iterable<BlockPos> airLayer = BlockPos.getAllInBox(areaMinX, keystoneY, areaMinZ, areaMaxX, areaMaxY, areaMaxZ);
        for (BlockPos blockPos : airLayer) {
            if (blockPos.equals(getPos()))
                continue; // Skip Trial Keystone block

            IBlockState state = getWorld().getBlockState(blockPos);
            Block block = state.getBlock();
            if (!block.isAir(state, getWorld(), blockPos))
                return false;
        }

        return true;
    }

    private void updateCapability() {
        PlayerHelper.updateTrialCapability(
                participants,
                getWaveMobTotal(),
                currentWave,
                mobsDefeated,
                getLastWave(),
                getPos(),
                active
        );
    }

    protected void resetTrial() {
        active = false;
        mobsSpawned = 0;
        mobsDefeated = 0;
        currentWave = 0;
        ticksToNextWave = 0;
        activeTrialKey = ItemStack.EMPTY;
        updateCapability();
        participants.clear();
    }

    //
    // Trial state
    //

    protected int getCurrentWave() {
        return currentWave;
    }

    protected int getLastWave() {
        AttunementData data = getData();
        return data != null ? data.getMaxWave() : 0;
    }

    private int getWaveMobTotal() {
        AttunementData data = getData();
        return data != null ? data.getCurrentWaveMobTotal(currentWave) : 0;
    }

    private double getSpawnDelay() {
        AttunementData data = getData();
        return data != null ? data.getSpawnDelay() : 0;
    }

    private int getGlitchChance() {
        AttunementData data = getData();
        return data != null ? data.getGlitchChance() : 0;
    }

    // todo test this? fix this?
    @Nullable
    private EntityLiving getRandomTrialEntity() {
        AttunementData data = getData();
        if (data != null) {
            return data.getRandomEntity(getWorld()).orElse(null);
        }
        return null;
    }

    protected ImmutableList<ItemStack> getRewards() {
        AttunementData data = getData();
        return data != null ? data.getRewards() : ImmutableList.of();
    }

    //
    // Tile Entity Passthroughs
    //

    private BlockPos getPos() {
        return te.getPos();
    }

    private World getWorld() {
        return te.getWorld();
    }

    private long getTimer() {
        return te.getTimer();
    }

    //
    // Trial Key Passthroughs (with caching for convenience)
    //

    private AttunementData data;
    private ImmutableList<TrialAffix> affixes;

    protected ImmutableList<TrialAffix> getAffixes() {
        ImmutableList<TrialAffix> affixes = ImmutableList.of();

        // If state is good
        if (activeTrialKey != ItemStack.EMPTY) {
            if (this.affixes == null) {
                // cache miss
                affixes = this.affixes = TrialKeyHelper.getAffixes(activeTrialKey, getPos(), getWorld());
            } else {
                // cache hit
                affixes = this.affixes;
            }
        } else {
            // no active key, reset cache
            this.affixes = ImmutableList.of();
        }
        return affixes;
    }

    @Nullable
    private AttunementData getData() {
        AttunementData data = null;

        // If state is good
        if (activeTrialKey != ItemStack.EMPTY) {
            if (this.data == null) {
                // cache miss
                data = this.data = TrialKeyHelper.getAttunement(activeTrialKey).orElse(null);
            } else {
                // cache hit
                data = this.data;
            }
        } else {
            // no active key, reset cache
            this.data = null;
        }
        return data;
    }

    //
    // NBT / Sync
    //
    // TODO Make sure all of this is ""everything"" we need to sync/save

    // NBT Tag Names
    private static final String NBT_ACTIVE_TRIAL_KEY = "activeTrialKey";
    private static final String NBT_CURRENT_WAVE = "currentWave";
    private static final String NBT_MOBS_DEFEATED = "mobsDefeated";
    private static final String NBT_IS_ACTIVE = "active";

    protected ByteBuf getUpdateData(ByteBuf buf) {
        buf.writeBoolean(active);
        buf.writeInt(currentWave);
        ByteBufUtils.writeItemStack(buf, activeTrialKey);
        return buf;
    }

    protected void handleUpdateData(ByteBuf buf) {
        this.active = buf.readBoolean();
        this.currentWave = buf.readInt();
        this.activeTrialKey = ByteBufUtils.readItemStack(buf);
    }

    protected void writeToNBT(NBTTagCompound compound) {
        compound.setBoolean(NBT_IS_ACTIVE, active);
        compound.setInteger(NBT_CURRENT_WAVE, currentWave);
        compound.setInteger(NBT_MOBS_DEFEATED, mobsDefeated);
        if (activeTrialKey != null) {
            NBTTagCompound trialKey = activeTrialKey.writeToNBT(new NBTTagCompound());
            compound.setTag(NBT_ACTIVE_TRIAL_KEY, trialKey);
        }
    }

    protected void readFromNBT(NBTTagCompound compound) {
        this.active = compound.getBoolean(NBT_IS_ACTIVE);
        this.currentWave = compound.getInteger(NBT_CURRENT_WAVE);
        this.mobsDefeated = compound.getInteger(NBT_MOBS_DEFEATED);
        // If world is closed while Trial is running, already spawned but not defeated mobs will despawn.
        // We can get them back by resetting the count to the number of defeated mobs.
        this.mobsSpawned = mobsDefeated;

        NBTTagCompound trialKey = compound.getCompoundTag(NBT_ACTIVE_TRIAL_KEY);
        this.activeTrialKey = new ItemStack(trialKey);
    }
}
