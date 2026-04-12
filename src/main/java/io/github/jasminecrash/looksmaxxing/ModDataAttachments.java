package io.github.jasminecrash.looksmaxxing;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;

import java.util.function.Consumer;

public class ModDataAttachments {

    private static <T> AttachmentType<T> registerAttachment(String name, Consumer<AttachmentRegistry.Builder<T>> builderLambda) {
        return AttachmentRegistry.create(
                Identifier.fromNamespaceAndPath(Looksmaxxing.MOD_ID, name),
                builderLambda
        );
    }

    public static final AttachmentType<Boolean> CASTING_ENTHRALL = registerAttachment(
            "casting_enthrall",
            booleanBuilder -> booleanBuilder.initializer(() -> false)
    );
    public static final AttachmentType<Boolean> IS_ENTHRALLED = registerAttachment(
            "is_enthralled",
            booleanBuilder -> booleanBuilder
                    .persistent(Codec.BOOL)
                    .initializer(() -> false)
                    .copyOnDeath()
    );
    public static final AttachmentType<Integer> ENTHRALL_CASTING_RANGE = registerAttachment(
            "enthrall_casting_range",
            integerBuilder -> integerBuilder
                    .persistent(Codec.INT)
                    .initializer(() -> 16)
                    .copyOnDeath()
    );
    public static final AttachmentType<Float> ENTHRALL_CASTING_ANGLE = registerAttachment(
            "enthrall_casting_angle",
            floatBuilder -> floatBuilder
                    .persistent(Codec.FLOAT)
                    .initializer(() -> 90.0f)
                    .copyOnDeath()
    );
    public static final AttachmentType<String> ENTHRALL_OWNER_STRING_UUID = registerAttachment(
            "enthrall_owner_string_uuid",
            stringBuilder -> stringBuilder
                    .persistent(Codec.STRING)
                    .initializer(() -> "")
                    .copyOnDeath()
    );
    public static final AttachmentType<Double> ENTHRALL_ANIMATION_PARAMETER = registerAttachment(
            "enthrall_animation_parameter",
            stringBuilder -> stringBuilder
                    .initializer(() -> 0.0d)
    ); //note: this attachment should NOT be networked for performance’s sake

    public static void registerAttachments() {

    }



}
