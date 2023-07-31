package mustapelto.deepmoblearning.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;

public class KeyboardHelper {

    public static boolean isHoldingSneakKey() {
        return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);
    }

    public static boolean isHoldingSprintKey() {
        return Keyboard.isKeyDown(Keyboard.KEY_LCONTROL);
    }

    public static String getSneakKeyName() {
        return I18n.format("deepmoblearning.key.lshift");
    }

    public static String getAttackKeyName() {
        return Minecraft.getMinecraft().gameSettings.keyBindAttack.getDisplayName();
    }

    public static String getSprintKeyName() {
        return I18n.format("deepmoblearning.key.lctrl");
    }

    public static String getUseKeyName() {
        return Minecraft.getMinecraft().gameSettings.keyBindUseItem.getDisplayName();
    }
}
