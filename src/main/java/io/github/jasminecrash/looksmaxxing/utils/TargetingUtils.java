package io.github.jasminecrash.looksmaxxing.utils;

import io.github.jasminecrash.looksmaxxing.ModDataAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

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

    public static List<Mob> getEnthrallMobTargets(ServerPlayer player) {
        ServerLevel level = player.level();
        float angle = player.getAttachedOrCreate(ModDataAttachments.ENTHRALL_CASTING_ANGLE);
        int range = player.getAttachedOrCreate(ModDataAttachments.ENTHRALL_CASTING_RANGE);
        double alphaRad = Math.toRadians(angle);
        double betaRad = Math.toRadians(angle);

        Vector3f eyePos = player.getEyePosition().toVector3f();
        Vector3f eyeLook = player.getLookAngle().toVector3f();
        Vector3f eyeUp = player.getUpVector(0.0f).toVector3f();

        Matrix4f frustumMatrix = MathUtils.createFrustumMatrix(eyePos, eyeLook, eyeUp, (float) alphaRad, (float) betaRad, 0.5f, (float) range);
        Frustum targetingFrustum = new Frustum(frustumMatrix);
        AABB targetingAABB = targetingFrustum.getFrustumAABB();
        List<Mob> targets = new ArrayList<>();

        for (Mob mob : level.getEntitiesOfClass(Mob.class, targetingAABB)) {
            if (mob.getAttachedOrCreate(ModDataAttachments.IS_ENTHRALLED)
            || !targetingFrustum.testIntersection(mob.getEyePosition())
            || !checkLineOfSight(player, mob)) {
                continue;
            }
            targets.add(mob);
        }
        return targets;
    }

    public static boolean checkLineOfSight(ServerPlayer player, LivingEntity target) {
        Vec3 start = player.getEyePosition();
        Vec3 end = target.getEyePosition();
        BlockHitResult hit = player.level().clip(new ClipContext(start, end, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, player));
        return hit.getType() == net.minecraft.world.phys.HitResult.Type.MISS;
    }
}
