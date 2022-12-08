package mustapelto.deepmoblearning.common.trials.affix;

import mustapelto.deepmoblearning.DMLConstants;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class LootHoarderAffix extends TrialAffix {

    private int ticks = 0;

    public LootHoarderAffix() {
        super();
    }

    public LootHoarderAffix(BlockPos pos, World world) {
        super(pos, world);
    }

    @Override
    public String getId() {
        return DMLConstants.Trials.Affix.LOOT_HOARDERS;
    }

    @Override
    public LootHoarderAffix copy(BlockPos pos, World world) {
        return new LootHoarderAffix(pos, world);
    }

    @Override
    public void run() {
        ticks++;
        // Once every 15 seconds, 25% chance
        if (ticks % 300 == 0) {
            ThreadLocalRandom rand = ThreadLocalRandom.current();
            if (rand.nextInt(1, 100) > 75) {
                EntityZombie hoarder = new EntityZombie(world);
                hoarder.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(Items.GOLDEN_HELMET));
                hoarder.setCustomNameTag("Loot Hoarder");
                hoarder.setChild(true);

                int randomX = pos.getX() + rand.nextInt(-5, 5);
                int randomY = pos.getY() + rand.nextInt(0, 1);
                int randomZ = pos.getZ() + rand.nextInt(-5, 5);

                hoarder.setLocationAndAngles(randomX, randomY, randomZ, 0, 0);
                hoarder.getEntityData().setString(DMLConstants.Trials.TRIAL_AFFIX_CONNECTION, DMLConstants.Trials.Affix.LOOT_HOARDERS);
                hoarder.enablePersistence();

                // Get loot table
                // todo constant?
                LootTable table = world.getLootTableManager().getLootTableFromLocation(new ResourceLocation(DMLConstants.ModInfo.ID, "loot_hoarder"));
                LootContext ctx = new LootContext.Builder((WorldServer) world).build();
                List<ItemStack> looted = table.generateLootForPools(world.rand, ctx);

                if (looted.size() > 0) {
                    hoarder.setHeldItem(EnumHand.MAIN_HAND, looted.get(0));
                }

                world.spawnEntity(hoarder);
            }
            ticks = 0;
        }
    }

    @Override
    public String getAffixName() {
        return TextFormatting.GOLD + I18n.format("deepmoblearning.affix.loot_hoarder.name") + TextFormatting.RESET;
    }
}
