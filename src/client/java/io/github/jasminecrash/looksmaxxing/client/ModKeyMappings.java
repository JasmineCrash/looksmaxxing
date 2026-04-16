package io.github.jasminecrash.looksmaxxing.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.jasminecrash.looksmaxxing.Looksmaxxing;
import io.github.jasminecrash.looksmaxxing.ModDataAttachments;
import io.github.jasminecrash.looksmaxxing.networking.ModPackets;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public abstract class ModKeyMappings {
    private static boolean enthrallKeyWasPressed = false;

    static KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(Looksmaxxing.MOD_ID, "looksmaxxing_keymappings")
    );

    static KeyMapping enthrallKey = KeyMappingHelper.registerKeyMapping(
            new KeyMapping(
                    "key." + Looksmaxxing.MOD_ID + ".cast_enthrall", // The translation key for the key mapping.
                    InputConstants.Type.KEYSYM, // // The type of the keybinding; KEYSYM for keyboard, MOUSE for mouse.
                    GLFW.GLFW_KEY_J, // The GLFW keycode of the key.
                    CATEGORY // The category of the mapping.
            ));

    public static void registerKeyMappings() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (enthrallKey.isDown() && !enthrallKeyWasPressed) {
                if (client.player != null && !client.player.getAttachedOrCreate(ModDataAttachments.CASTING_ENTHRALL)) {
                    client.player.sendSystemMessage(Component.literal("Casting..."));
                    ClientPlayNetworking.send(new ModPackets.SetEnthrallPayload(client.player.getStringUUID(), true));
                }
            }
            enthrallKeyWasPressed = enthrallKey.isDown();
        });
    }
}
