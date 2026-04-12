package io.github.jasminecrash.looksmaxxing.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public abstract class TargetingUtils {

    public static List<Mob> getMobsInCube(Vec3 AABBCenter, double boxWidth, ServerLevel level) {
        AABB targetingBox = new AABB(
                new Vec3(AABBCenter.toVector3f()).subtract(boxWidth/2),
                new Vec3(AABBCenter.toVector3f()).add(boxWidth/2)
        );
        return level.getEntitiesOfClass(Mob.class, targetingBox);
    }
    public static List<Player> getPlayersInCube(Vec3 AABBCenter, double boxWidth, ServerLevel level) {
        AABB targetingBox = new AABB(
                new Vec3(AABBCenter.toVector3f()).subtract(boxWidth/2),
                new Vec3(AABBCenter.toVector3f()).add(boxWidth/2)
        );
        return level.getEntitiesOfClass(Player.class, targetingBox);
    }
    public static List<BlockPos> getBlockPosInCube(Vec3 AABBCenter, double boxWidth) {
        AABB targetingBox = new AABB(
                new Vec3(AABBCenter.toVector3f()).subtract(boxWidth/2),
                new Vec3(AABBCenter.toVector3f()).add(boxWidth/2)
        );
        List<BlockPos> blocks = new ArrayList<>();
        for(BlockPos pos : BlockPos.betweenClosed(targetingBox)) {
            blocks.add(pos);
        }
        return blocks;
    }
}
