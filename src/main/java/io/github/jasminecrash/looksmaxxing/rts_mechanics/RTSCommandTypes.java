package io.github.jasminecrash.looksmaxxing.rts_mechanics;

import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.*;

public abstract class RTSCommandTypes {

    private static final Map<String, RTSCommandType<?>> BY_NAME = new LinkedHashMap<>();

    //hardcoded command types

    public static final RTSCommandType<BlockPos> MOVE_TO = register("move_to",
            new RTSCommandType<>(
                    "move_to",
                    "move_to.desc",
                    BlockPos.CODEC,
                    BlockPos.STREAM_CODEC.cast(),
                    (mob, pos) -> {
                        AttributeInstance followRange = mob.getAttribute(Attributes.FOLLOW_RANGE);
                        if (followRange != null) {
                            followRange.removeModifier(RTSAttributes.RTS_FOLLOW_RANGE_BOOST);
                            followRange.addTransientModifier(new AttributeModifier(
                                    RTSAttributes.RTS_FOLLOW_RANGE_BOOST,
                                    256.0,
                                    AttributeModifier.Operation.ADD_VALUE
                            ));
                        }
                        //mob.getNavigation().stop();
                        mob.getNavigation().moveTo(pos.getX(), pos.getY(), pos.getZ(), 1.0);
                    }
            )
    );

    public static final RTSCommandType<UUID> ATTACK_TARGET = register("attack_target",
            new RTSCommandType<>(
                    "attack_target",
                    "attack_target.desc",
                    UUIDUtil.CODEC,
                    UUIDUtil.STREAM_CODEC.cast(),
                    (mob, uuid) -> {
                        if (mob.level() instanceof ServerLevel serverLevel) {
                            Entity target = serverLevel.getEntity(uuid);
                            if (target instanceof LivingEntity living) {
                                mob.setTarget(living);
                            }
                        }
                    }
            )
    );

    public static final RTSCommandType<Unit> HOLD_POSITION = register("hold_position",
            new RTSCommandType<>(
                    "hold_position",
                    "hold_position.desc",
                    Codec.EMPTY.codec(),
                    StreamCodec.unit(Unit.INSTANCE),
                    (mob, ignored) -> mob.getNavigation().stop()
            )
    );

    //registry hoo-hah

    private static <D> RTSCommandType<D> register(String name, RTSCommandType<D> type) {
        if (BY_NAME.putIfAbsent(name, type) != null) {
            throw new IllegalStateException("Duplicate RTSCommandType: " + name);
        }
        return type;
    }

    public static RTSCommandType<?> byName(String name) {
        RTSCommandType<?> type = BY_NAME.get(name);
        if (type == null) { throw new IllegalArgumentException("Unknown RTSCommandType: " + name); }
        return type;
    }

    public static String nameOf(RTSCommandType<?> type) {
        return BY_NAME.entrySet().stream()
                .filter(e -> e.getValue() == type)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow();
    }

    public static Collection<RTSCommandType<?>> all() {
        return Collections.unmodifiableCollection(BY_NAME.values());
    }

    public static void registerRTSCommandTypes() {
    }
}
