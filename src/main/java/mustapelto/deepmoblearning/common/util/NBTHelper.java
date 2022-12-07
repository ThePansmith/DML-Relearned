package mustapelto.deepmoblearning.common.util;

import com.google.common.collect.ImmutableList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;

public class NBTHelper {
    //
    // ItemStack methods
    //

    public static NBTTagCompound getOrCreateTag(ItemStack stack) {
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());

        NBTTagCompound result = stack.getTagCompound();
        if (result == null)
            throw new NullPointerException("ItemStack NBTTagCompound is null when it shouldn't be");
        return result;
    }

    @Nullable
    public static NBTTagCompound getTag(ItemStack stack) {
        return stack.hasTagCompound() ? stack.getTagCompound() : null;
    }

    public static boolean hasKey(ItemStack stack, String key) {
        NBTTagCompound nbt = getTag(stack);
        return nbt != null && nbt.hasKey(key);
    }

    public static void removeKey(ItemStack stack, String key) {
        NBTTagCompound nbt = getTag(stack);
        if (nbt == null)
            return;

        nbt.removeTag(key);

        if (nbt.isEmpty())
            stack.setTagCompound(null);
    }

    public static void setInteger(ItemStack stack, String key, int value) {
        NBTTagCompound nbt = getOrCreateTag(stack);
        nbt.setInteger(key, value);
    }

    public static int getInteger(ItemStack stack, String key, int defaultValue) {
        NBTTagCompound nbt = getTag(stack);
        return nbt != null ? getInteger(nbt, key, defaultValue) : defaultValue;
    }

    public static int getInteger(ItemStack stack, String key) {
        return getInteger(stack, key, 0);
    }

    public static void setString(ItemStack stack, String key, String value) {
        NBTTagCompound nbt = getOrCreateTag(stack);
        nbt.setString(key, value);
    }

    public static String getString(ItemStack stack, String key, String defaultValue) {
        NBTTagCompound nbt = getTag(stack);
        return nbt != null ? getString(nbt, key, defaultValue) : defaultValue;
    }

    public static String getString(ItemStack stack, String key) {
        return getString(stack, key, "");
    }

    public static NBTTagList getTagList(ItemStack stack, String key) {
        NBTTagCompound nbt = getTag(stack);
        return nbt != null ? nbt.getTagList(key, Constants.NBT.TAG_COMPOUND) : new NBTTagList();
    }

    public static void setTagList(ItemStack stack, String key, NBTTagList list) {
        NBTTagCompound nbt = getOrCreateTag(stack);
        nbt.setTag(key, list);
    }

    //
    // NBTTagCompound methods
    //

    public static int getInteger(NBTTagCompound compound, String key, int defaultValue) {
        return compound.hasKey(key, Constants.NBT.TAG_INT) ? compound.getInteger(key) : defaultValue;
    }

    public static int getInteger(NBTTagCompound compound, String key) {
        return getInteger(compound, key, 0);
    }

    public static boolean getBoolean(NBTTagCompound compound, String key, boolean defaultValue) {
        return compound.hasKey(key, Constants.NBT.TAG_BYTE) ? compound.getBoolean(key) : defaultValue;
    }

    public static boolean getBoolean(NBTTagCompound compound, String key) {
        return getBoolean(compound, key, false);
    }

    public static String getString(NBTTagCompound compound, String key, String defaultValue) {
        return compound.hasKey(key, Constants.NBT.TAG_STRING) ? compound.getString(key) : defaultValue;
    }

    public static String getString(NBTTagCompound compound, String key) {
        return getString(compound, key, "");
    }
}
