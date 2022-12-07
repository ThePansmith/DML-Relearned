package mustapelto.deepmoblearning.common.inventory;

import mustapelto.deepmoblearning.common.items.ItemTrialKey;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import javax.annotation.Nonnull;

public class ItemHandlerTrialKey extends ItemHandlerBase {

    public ItemHandlerTrialKey() {
        super();
    }

    public ItemHandlerTrialKey(NonNullList<ItemStack> stacks) {
        super(stacks);
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return stack.getItem() instanceof ItemTrialKey && super.isItemValid(slot, stack);
    }
}
