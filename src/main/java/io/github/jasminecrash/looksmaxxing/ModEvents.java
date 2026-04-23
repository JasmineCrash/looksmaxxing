package io.github.jasminecrash.looksmaxxing;

import io.github.jasminecrash.looksmaxxing.networking.ModPackets;
import io.github.jasminecrash.looksmaxxing.utils.Constants;
import io.github.jasminecrash.looksmaxxing.utils.TargetingUtils;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public class ModEvents {
    public static void registerServerEvents() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (!player.getAttachedOrCreate(ModDataAttachments.CASTING_ENTHRALL)) {
                    continue;
                }
                if (player.getAttachedOrCreate(ModDataAttachments.ENTHRALL_CASTING_START_TIME, () -> -Constants.ENTHRALL_CASTING_TIME) < player.level().getGameTime() - Constants.ENTHRALL_CASTING_TIME) {
                    player.setAttached(ModDataAttachments.CASTING_ENTHRALL, false);
                    player.setAttached(ModDataAttachments.ENTHRALL_CASTING_START_TIME, -Constants.ENTHRALL_CASTING_TIME);

                    //enthrall application on targeted mobs
                    TargetingUtils.getEnthrallMobTargets(player).forEach(mob -> {
                        mob.setAttached(ModDataAttachments.IS_ENTHRALLED, true);
                        mob.setAttached(ModDataAttachments.ENTHRALL_OWNER_STRING_UUID, player.getStringUUID());
                    });

                    ModPackets.SetEnthrallPayload syncPacket = new ModPackets.SetEnthrallPayload(player.getStringUUID(), false);
                    PlayerLookup.all(server).forEach(
                            serverPlayer -> ServerPlayNetworking.send(serverPlayer, syncPacket)
                    );
                }
            }
        });
    }


}
