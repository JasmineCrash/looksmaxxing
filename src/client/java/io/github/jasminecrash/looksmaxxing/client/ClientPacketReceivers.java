package io.github.jasminecrash.looksmaxxing.client;

import io.github.jasminecrash.looksmaxxing.ModDataAttachments;
import io.github.jasminecrash.looksmaxxing.networking.ModPackets;
import io.github.jasminecrash.looksmaxxing.utils.Constants;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class ClientPacketReceivers {
    public static void registerClientReceivers() {

        ClientPlayNetworking.registerGlobalReceiver(ModPackets.SetEnthrallPayload.TYPE, ((payload, context) -> {
            Player localPlayer = context.player();
            Player caster = localPlayer.level().getPlayerByUUID(UUID.fromString(payload.uuid()));
            if (caster == null) { return; }
            caster.setAttached(ModDataAttachments.CASTING_ENTHRALL, payload.casting());
            caster.setAttached(ModDataAttachments.ENTHRALL_CASTING_START_TIME, payload.casting() ? localPlayer.level().getGameTime() : -Constants.ENTHRALL_CASTING_TIME);
        }));
    }
}
