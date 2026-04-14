package io.github.jasminecrash.looksmaxxing.rts_mechanics;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Mob;

import java.util.function.BiConsumer;

import static io.github.jasminecrash.looksmaxxing.Looksmaxxing.MOD_ID;

public final class RTSCommandType<D> {
    private final String translationKey;
    private final String descriptionKey;
    private final Codec<D> codec;
    private final StreamCodec<RegistryFriendlyByteBuf, D> streamCodec;
    private final BiConsumer<Mob, D> onStart;

    public RTSCommandType(
            String translationKey,
            String descriptionKey,
            Codec<D> codec,
            StreamCodec<RegistryFriendlyByteBuf, D> streamCodec,
            BiConsumer<Mob, D> onStart
    ) {
        this.translationKey = "command." + MOD_ID + translationKey + ".";
        this.descriptionKey = "command." + MOD_ID + descriptionKey + ".";
        this.codec = codec;
        this.streamCodec = streamCodec;
        this.onStart = onStart;
    }

    public Component getName() {
        return Component.translatable(translationKey);
    }

    public Component getDescription() {
        return Component.translatable(descriptionKey);
    }

    public void onStart(Mob mob, D data) {
        onStart.accept(mob, data);
    }

    public Codec<D> codec() { return codec; }
    public StreamCodec<RegistryFriendlyByteBuf, D> streamCodec() { return streamCodec; }
}
