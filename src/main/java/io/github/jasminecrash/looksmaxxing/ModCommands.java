package io.github.jasminecrash.looksmaxxing;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import io.github.jasminecrash.looksmaxxing.networking.ModPackets;
import io.github.jasminecrash.looksmaxxing.rts_mechanics.RTSCommand;
import io.github.jasminecrash.looksmaxxing.rts_mechanics.RTSCommandTypes;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayDeque;
import java.util.List;
import java.util.stream.Collectors;

public class ModCommands {

    private static void registerCommand(String commandName, Command<CommandSourceStack> commandLambda) {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(Commands.literal(commandName).executes(commandLambda)));
    }

    private static <T> void registerCommandWithArg(String commandName, String argName, ArgumentType<T> argType, Command<CommandSourceStack> commandLambda) {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(Commands.literal(commandName)
                .then(Commands.argument(argName, argType)
                        .executes(commandLambda))));
    }

    public static void registerCommands() {

        registerCommand("summonMob", commandContext -> {
            CommandSourceStack source = commandContext.getSource();
            Player player = source.getPlayer();
            ServerLevel level = source.getLevel();
            if (!source.isPlayer() || player == null) { return 0; }
            List<Entity> entities = level.getEntities(player, player.getBoundingBox().inflate(32), (entity) -> entity instanceof Mob);
            if (entities.isEmpty()) { return 0; }
            Mob nearestMob = (Mob) entities.getFirst();
            for (Entity entity : entities) {
                if (player.getEyePosition().closerThan(
                        entity.getEyePosition(),
                        nearestMob.getEyePosition().distanceToSqr(player.getEyePosition())
                )) { nearestMob = (Mob) entity; }
            }
            ModPackets.IssueCommandC2SPayload command = RTSCommand.toPayload(
                    nearestMob.getId(),
                    RTSCommandTypes.MOVE_TO,
                    player.getOnPos(),
                    true
            );
            ClientPlayNetworking.send(command);
            Mob finalMob = nearestMob;
            source.sendSuccess(() -> Component.literal(
                    "sent MOVE_TO command to " + finalMob.getPlainTextName() +
                            ", with ID " + finalMob.getId() + "\n" + "Command JSON: " +
                            command.commandDataJSON().toString()
            ), false);
            return 1;
        });

        registerCommand("summonManyMobs", commandContext -> {
            CommandSourceStack source = commandContext.getSource();
            Player player = source.getPlayer();
            ServerLevel level = source.getLevel();
            if(!source.isPlayer() || player == null) { return 0; }
            List<Entity> entities = level.getEntities(player, player.getBoundingBox().inflate(64), (entity) -> entity instanceof Mob);
            List<Integer> ids = entities.stream()
                    .mapToInt(Entity::getId)
                    .boxed()
                    .collect(Collectors.toList());
            ModPackets.IssueCommandC2SPayload command = RTSCommand.toPayload(
                    ids,
                    RTSCommandTypes.MOVE_TO,
                    player.getOnPos(),
                    true
            );

            ClientPlayNetworking.send(command);
            return 1;
        });

        registerCommand("debugTargets", commandContext -> {
            CommandSourceStack source = commandContext.getSource();
            Player player = source.getPlayer();
            ServerLevel level = source.getLevel();
            if (!source.isPlayer() || player == null) { return 0; }
            List<Entity> enthralledMobs = level.getEntities(player, player.getBoundingBox().inflate(64),
                    (entity) -> entity instanceof Mob mob && mob.getAttachedOrCreate(ModDataAttachments.IS_ENTHRALLED));
            if (enthralledMobs.isEmpty()) {
                source.sendFailure(Component.literal("No enthralled mobs nearby"));
                return 0;
            }
            for (Entity e : enthralledMobs) {
                Mob mob = (Mob) e;
                LivingEntity target = mob.getTarget();
                String targetInfo = target == null ? "none" : target.getDisplayName().getString() + " (alive: " + target.isAlive() + ")";
                ArrayDeque<RTSCommand> queue = mob.getAttachedOrCreate(ModDataAttachments.COMMAND_QUEUE);
                String queueInfo = queue.isEmpty() ? "empty" : queue.size() + " command(s), first: " + RTSCommandTypes.nameOf(queue.peekFirst().type());
                source.sendSuccess(() -> Component.literal("  " + mob.getDisplayName().getString() + " → target: " + targetInfo + " | queue: " + queueInfo), false);
            }
            return 1;
        });

        registerCommandWithArg("attack", "target", EntityArgument.entity(), commandContext -> {
            CommandSourceStack source = commandContext.getSource();
            Player player = source.getPlayer();
            ServerLevel level = source.getLevel();
            if(!source.isPlayer() || player == null) { return 0; }
            Entity targetEntity = EntityArgument.getEntity(commandContext, "target");
            if(!(targetEntity instanceof LivingEntity target)) {
                source.sendFailure(Component.literal("Target must be a living entity"));
                return 0;
            }
            if(!target.isAlive()) {
                source.sendFailure(Component.literal("Target is dead"));
                return 0;
            }
            List<Entity> enthralledMobs = level.getEntities(player, player.getBoundingBox().inflate(64),
                    (entity) -> entity instanceof Mob mob && mob.getAttachedOrCreate(ModDataAttachments.IS_ENTHRALLED));
            if(enthralledMobs.isEmpty()) {
                source.sendFailure(Component.literal("No enthralled mobs nearby"));
                return 0;
            }
            List<Integer> ids = enthralledMobs.stream()
                    .mapToInt(Entity::getId)
                    .boxed()
                    .collect(Collectors.toList());
            ModPackets.IssueCommandC2SPayload command = RTSCommand.toPayload(
                    ids,
                    RTSCommandTypes.ATTACK_TARGET,
                    target.getUUID(),
                    true
            );
            ClientPlayNetworking.send(command);
            source.sendSuccess(() -> Component.literal(
                    "Issued ATTACK_TARGET to " + enthralledMobs.size() + " mob(s), targeting " + target.getDisplayName().getString()
            ), false);
            return 1;
        });

        registerCommand("unenthrallAll", commandContext -> {
            CommandSourceStack source = commandContext.getSource();
            Player player = source.getPlayer();
            ServerLevel level = source.getLevel();
            if (!source.isPlayer() || player == null) { return 0; }
            Iterable<Entity> allEntities = level.getAllEntities();
            int[] count = {0};
            for (Entity e : allEntities) {
                if (e instanceof Mob mob && mob.getAttachedOrCreate(ModDataAttachments.IS_ENTHRALLED)) {
                    mob.setAttached(ModDataAttachments.IS_ENTHRALLED, false);
                    mob.setAttached(ModDataAttachments.COMMAND_QUEUE, new ArrayDeque<>());
                    mob.setTarget(null);
                    mob.getNavigation().stop();
                    count[0]++;
                }
            }
            source.sendSuccess(() -> Component.literal("Unenthralled " + count[0] + " mob(s)"), false);
            return 1;
        });

    }
}
