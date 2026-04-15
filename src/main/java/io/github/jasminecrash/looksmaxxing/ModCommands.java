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
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.stream.Collectors;

public class ModCommands {

    private static void registerCommand(String commandName, Command<CommandSourceStack> commandLambda) {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal(commandName).executes(commandLambda));
        });
    }

    private static <T> void registerCommandWithArg(String commandName, String argName, ArgumentType<T> argType, Command<CommandSourceStack> commandLambda) {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal(commandName)
                    .then(Commands.argument(argName, argType)
                            .executes(commandLambda)));
        });
    }

    public static void registerCommands() {

        registerCommand("summonMob", commandContext -> {
            CommandSourceStack source = commandContext.getSource();
            Player player = source.getPlayer();
            ServerLevel level = source.getLevel();
            if(!source.isPlayer() || player == null) { return 0; }
            List<Entity> entities = level.getEntities(player, player.getBoundingBox().inflate(32), (entity) -> entity instanceof Mob);
            Mob nearestMob = (Mob) entities.getFirst();
            for(Entity entity : entities) {
                if(player.getEyePosition().closerThan(
                        entity.getEyePosition(),
                        nearestMob.getEyePosition().distanceToSqr(player.getEyePosition())
                )) { nearestMob = (Mob)entity; }
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
            //finalMob.setHealth(0);
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

    }
}
