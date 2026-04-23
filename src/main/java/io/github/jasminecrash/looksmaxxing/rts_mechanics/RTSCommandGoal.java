package io.github.jasminecrash.looksmaxxing.rts_mechanics;

import io.github.jasminecrash.looksmaxxing.ModDataAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.ArrayDeque;
import java.util.EnumSet;

public class RTSCommandGoal extends Goal {
    private final Mob mob;
    private BlockPos lastCheckedPos = null;
    private int stuckCheckTimer = 0;

    private static final int STUCK_CHECK_INTERVAL = 60;  //ticks between checks
    private static final double STUCK_MOVEMENT_THRESHOLD = 2.0; //blocks moved minimum

    public RTSCommandGoal(Mob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.noneOf(Flag.class));
    }

    private void clearFollowRangeBoost() {
        AttributeInstance followRange = mob.getAttribute(Attributes.FOLLOW_RANGE);
        if (followRange != null) {
            followRange.removeModifier(RTSAttributes.RTS_FOLLOW_RANGE_BOOST);
        }
    }

    private boolean isCommandComplete(RTSCommand command) {
        if (command.type() == RTSCommandTypes.MOVE_TO) {
            BlockPos target = command.data(RTSCommandTypes.MOVE_TO);

            // arrival check
            if (mob.blockPosition().closerThan(target, 2.0)) {
                clearFollowRangeBoost();
                return true;
            }

            // stuck check
            stuckCheckTimer++;
            if (stuckCheckTimer >= STUCK_CHECK_INTERVAL) {
                stuckCheckTimer = 0;
                BlockPos currentPos = mob.blockPosition();
                if (lastCheckedPos != null && currentPos.closerThan(lastCheckedPos, STUCK_MOVEMENT_THRESHOLD)) {
                    clearFollowRangeBoost();
                    return true;
                }
                lastCheckedPos = currentPos;
            }

            return false;
        }
        else if (command.type() == RTSCommandTypes.HOLD_POSITION) {
            return mob.getAttachedOrCreate(ModDataAttachments.COMMAND_QUEUE).size() > 1; // finishes immediately when there's more stuff queued up behind it
        }
        else if (command.type() == RTSCommandTypes.ATTACK_TARGET) {
            LivingEntity target = mob.getTarget();
            return target == null
                    || !target.isAlive()
                    || !mob.level().equals(target.level())
                    || mob.distanceTo(target) > 64.0;
        }
        return true;
    }

    private ArrayDeque<RTSCommand> queue() {
        return mob.getAttachedOrCreate(ModDataAttachments.COMMAND_QUEUE);
    }

    @Override
    public boolean canUse() {
        return !queue().isEmpty();
    }

    @Override
    public boolean canContinueToUse() {
        return !queue().isEmpty();
    }

    @Override
    public void start() {
        RTSCommand current = queue().peek();
        if (current != null) {
            current.start(mob);
            lastCheckedPos = mob.blockPosition();
            stuckCheckTimer = 0;
            setFlagsForCurrentCommand();
        }
    }

    @Override
    public void tick() {
        RTSCommand current = queue().peekFirst();
        if (current == null || !isCommandComplete(current)) { return; }

        queue().pollFirst();
        lastCheckedPos = mob.blockPosition();
        stuckCheckTimer = 0;
        RTSCommand next = queue().peekFirst();
        if (next != null) { next.start(mob); }
        setFlagsForCurrentCommand();
    }

    private void setFlagsForCurrentCommand() {
        RTSCommand current = queue().peekFirst();
        if (current == null) {
            setFlags(EnumSet.noneOf(Flag.class));
            return;
        }
        if (current.type() == RTSCommandTypes.MOVE_TO) {
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        } else {
            setFlags(EnumSet.noneOf(Flag.class));
        }
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }
}
