package io.github.jasminecrash.looksmaxxing.networking;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import io.github.jasminecrash.looksmaxxing.Looksmaxxing;
import io.github.jasminecrash.looksmaxxing.ModDataAttachments;
import io.github.jasminecrash.looksmaxxing.rts_mechanics.RTSCommand;
import io.github.jasminecrash.looksmaxxing.rts_mechanics.RTSCommandType;
import io.github.jasminecrash.looksmaxxing.rts_mechanics.RTSCommandTypes;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

import java.util.ArrayDeque;

public abstract class ModPacketReceivers {

    private static <D> RTSCommand decodeCommand(String typeName, JsonElement json) {
        RTSCommandType<?> rawType;
        try {
            rawType = RTSCommandTypes.byName(typeName);
        } catch (IllegalArgumentException e) {
            return null; // unknown type — reject silently
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
            Looksmaxxing.LOGGER.info("player {} sent a command!", player.getPlainTextName());
            Entity entity = player.level().getEntity(payload.entityId());
            Mob mob = (Mob) entity;

            RTSCommand command = decodeCommand(payload.commandTypeName(), payload.commandDataJSON());
            if (command == null || mob == null) { return; }

            ArrayDeque<RTSCommand> queue = mob.getAttachedOrCreate(ModDataAttachments.COMMAND_QUEUE);
            if (!payload.enqueue()) { queue.clear(); }
            queue.add(command);
            mob.setAttached(ModDataAttachments.COMMAND_QUEUE, queue);

            //testing
            //BlockPos presumedTarget = (BlockPos) mob.getAttachedOrThrow(ModDataAttachments.COMMAND_QUEUE).getFirst().data(RTSCommandTypes.byName("move_to"));
            //Looksmaxxing.LOGGER.info("position: {}, {} commands in queue", presumedTarget.toShortString(), mob.getAttachedOrThrow(ModDataAttachments.COMMAND_QUEUE).size());
            //mob.goalSelector.getAvailableGoals().stream().toList().forEach(wrappedGoal -> Looksmaxxing.LOGGER.info("goalPriority: {}", wrappedGoal.getPriority()));
        });

        
    }

}
