package me.earth.phobos.util;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.earth.phobos.Phobos;
import me.earth.phobos.features.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;

public class MessageUtil {
    public static final String messagePrefix = ChatFormatting.GRAY + "[" + "Skobos" + ChatFormatting.GRAY + "] " + ChatFormatting.RESET;
    public static final String errorPrefix = ChatFormatting.DARK_RED + "[" + "Skobos" + "] " + ChatFormatting.RESET;

    public static void sendRawMessage(String message) {
        if(Minecraft.getMinecraft().player != null) {
            Minecraft.getMinecraft().player.sendMessage(new TextComponentString(message));
        }
    }

    public static void sendMessage(String message) {
        sendRawMessage(messagePrefix + message);
    }

    public static void sendError(String message) {
        sendRawMessage(errorPrefix + message);
    }

    public static void sendRemovableMessage(String message, int id) {
        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new TextComponentString(messagePrefix + message), id);
    }

    public static String getModulePrefix(Module module, ChatFormatting color) {
        return ChatFormatting.GRAY + "[" + color + module.getName() + ChatFormatting.GRAY + "]" + ChatFormatting.RESET;
    }
}
