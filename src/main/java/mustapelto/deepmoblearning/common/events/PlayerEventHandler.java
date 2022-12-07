package mustapelto.deepmoblearning.common.events;

import mustapelto.deepmoblearning.DMLConstants;
import mustapelto.deepmoblearning.DMLRelearned;
import mustapelto.deepmoblearning.common.DMLConfig;
import mustapelto.deepmoblearning.common.DMLRegistry;
import mustapelto.deepmoblearning.common.ServerProxy;
import mustapelto.deepmoblearning.common.items.ItemGlitchArmor;
import mustapelto.deepmoblearning.common.items.ItemGlitchHeart;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemRedstone;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@EventBusSubscriber
public class PlayerEventHandler {

    private static final List<UUID> FLYING_PLAYERS = new ArrayList<>();

    @SubscribeEvent
    public static void playerLeftClickedBlock(PlayerInteractEvent.LeftClickBlock event) {
        ItemStack stack = event.getItemStack();
        Item heldItem = stack.getItem();

        World world = event.getWorld();
        Block clickedBlock = world.getBlockState(event.getPos()).getBlock();
        Vec3d hitVector = event.getHitVec();

        if (DMLConfig.MISC_SETTINGS.SOOT_COVERED_REDSTONE_CRAFTING_ENABLED) {
            if (heldItem instanceof ItemRedstone && clickedBlock == Blocks.COAL_BLOCK) {
                if (event.getSide() == Side.SERVER) {
                    spawnItemEntity(hitVector, world, DMLRegistry.ITEM_SOOTED_REDSTONE, DMLConstants.Crafting.SOOTED_REDSTONE_PER_REDSTONE);
                    stack.shrink(1); // Reduce size of original redstone stack
                } else {
                    createRandomParticles(hitVector, world, ServerProxy.SmokeType.SMOKE);
                }
                event.setCanceled(true);
            }
        }

        if (heldItem instanceof ItemGlitchHeart && clickedBlock == Blocks.OBSIDIAN) {
            if (event.getSide() == Side.SERVER) {
                spawnItemEntity(hitVector, world, DMLRegistry.ITEM_GLITCH_FRAGMENT, DMLConstants.Crafting.GLITCH_FRAGMENTS_PER_HEART);
                stack.shrink(1);
            } else {
                createRandomParticles(hitVector, world, ServerProxy.SmokeType.CYAN);
            }
            event.setCanceled(true);
        }
    }

    private static void spawnItemEntity(Vec3d hitVector, World world, Item itemIn, int amount) {
        EntityItem item = new EntityItem(world, hitVector.x, hitVector.y + 0.5, hitVector.z, new ItemStack(itemIn, amount));
        item.setDefaultPickupDelay();
        world.spawnEntity(item);
    }

    private static void createRandomParticles(Vec3d hitVector, World world, ServerProxy.SmokeType type) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int i = 0; i < 3; i++) {
            DMLRelearned.proxy.spawnSmokeParticle(world,
                    hitVector.x + random.nextDouble(-0.4, 0.4),
                    hitVector.y + random.nextDouble(-0.1, 0.4),
                    hitVector.z + random.nextDouble(-0.4, 0.4),
                    random.nextDouble(-0.08, 0.08),
                    random.nextDouble(-0.08, 0),
                    random.nextDouble(-0.08, 0.08),
                    type);
        }
    }

    @SubscribeEvent
    public static void playerTickUpdate(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START)
            return;

        if (!DMLConfig.GLITCH_ARMOR_SETTINGS.GLITCH_CREATIVE_FLIGHT_ENABLED || event.player.world.isRemote)
            return;

        PlayerCapabilities capabilities = event.player.capabilities;
        UUID playerUUID = event.player.getUniqueID();

        if (ItemGlitchArmor.isSetEquipped(event.player)) {
            if (!capabilities.allowFlying) {
                capabilities.allowFlying = true;
                event.player.sendPlayerAbilities();
                if (!FLYING_PLAYERS.contains(playerUUID))
                    FLYING_PLAYERS.add(playerUUID);
            }
        } else {
            if (FLYING_PLAYERS.contains(playerUUID)) {
                if (capabilities.allowFlying && !event.player.isSpectator() && !event.player.isCreative()) {
                    capabilities.allowFlying = false;
                    capabilities.isFlying = false;
                    event.player.sendPlayerAbilities();
                }
                removeFlyingPlayer(playerUUID);
            }
        }
    }

    @SubscribeEvent
    public static void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        removeFlyingPlayer(event.player.getUniqueID());
    }

    private static void removeFlyingPlayer(UUID playerUUID) {
        FLYING_PLAYERS.removeIf(uuid -> uuid.equals(playerUUID));
    }
}
