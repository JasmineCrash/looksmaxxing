package io.github.jasminecrash.looksmaxxing.networking;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import io.github.jasminecrash.looksmaxxing.Looksmaxxing;
import io.github.jasminecrash.looksmaxxing.ModDataAttachments;
import io.github.jasminecrash.looksmaxxing.rts_mechanics.RTSCommand;
import io.github.jasminecrash.looksmaxxing.rts_mechanics.RTSCommandType;
import io.github.jasminecrash.looksmaxxing.rts_mechanics.RTSCommandTypes;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public abstract class ServerPacketReceivers {

    private static <D> RTSCommand decodeCommand(String typeName, JsonElement json) {
        RTSCommandType<?> rawType;
        try {
            rawType = RTSCommandTypes.byName(typeName);
        } catch (IllegalArgumentException e) {
            return null; // unknown type - reject silently
        }

        @SuppressWarnings("unchecked")
        RTSCommandType<D> type = (RTSCommandType<D>) rawType;

        return type.codec().parse(JsonOps.INSTANCE, json)
                .mapOrElse(
                        data -> RTSCommand.of(type, data),
                        err  -> {
                            Looksmaxxing.LOGGER.warn("Failed to decode RTS command '{}': {}", typeName, err.message());
                            return null;
                        }
                );
    }
    
    public static void registerServerReceivers() {

        ServerPlayNetworking.registerGlobalReceiver(ModPackets.IssueCommandC2SPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            //Looksmaxxing.LOGGER.info("player {} sent a command!", player.getPlainTextName());
            //Entity entity = player.level().getEntity(payload.entityIds().getFirst());
            ServerLevel level = player.level();
            List<Mob> mobs = new ArrayList<>();
            payload.entityIds().forEach(id -> {
                        mobs.add((Mob)level.getEntity(id));
                    }
            );

            RTSCommand command = decodeCommand(payload.commandTypeName(), payload.commandDataJSON());
            for(Mob mob : mobs) {
                if (command == null || mob == null) { continue; }
                ArrayDeque<RTSCommand> queue = mob.getAttachedOrCreate(ModDataAttachments.COMMAND_QUEUE);
                if (!payload.enqueue()) { queue.clear(); }
                queue.add(command);
                mob.setAttached(ModDataAttachments.COMMAND_QUEUE, queue);
            }

            //testing
            //BlockPos presumedTarget = (BlockPos) mob.getAttachedOrThrow(ModDataAttachments.COMMAND_QUEUE).getFirst().data(RTSCommandTypes.byName("move_to"));
            //Looksmaxxing.LOGGER.info("position: {}, {} commands in queue", presumedTarget.toShortString(), mob.getAttachedOrThrow(ModDataAttachments.COMMAND_QUEUE).size());
            //mob.goalSelector.getAvailableGoals().stream().toList().forEach(wrappedGoal -> Looksmaxxing.LOGGER.info("goalPriority: {}", wrappedGoal.getPriority()));
        });

        ServerPlayNetworking.registerGlobalReceiver(ModPackets.SetEnthrallPayload.TYPE, (payload, context) -> {
            ServerPlayer caster = context.player();
            if (caster.getAttachedOrCreate(ModDataAttachments.CASTING_ENTHRALL)) { return; }
            caster.setAttached(ModDataAttachments.CASTING_ENTHRALL, true);
            caster.setAttached(ModDataAttachments.ENTHRALL_CASTING_START_TIME, caster.level().getGameTime());

            ModPackets.SetEnthrallPayload syncPacket = new ModPackets.SetEnthrallPayload(caster.getStringUUID(), true);
            ServerPlayNetworking.send(caster, syncPacket);
            PlayerLookup.tracking(caster).forEach(
                    serverPlayer -> ServerPlayNetworking.send(serverPlayer, syncPacket)
            ); //TODO: work out how to sync this for all players that can see the caster, even if they weren't initially tracking the caster, without blowing out someone's network card
        });
    }
}
