package io.github.jasminecrash.looksmaxxing.networking;

import com.google.gson.JsonElement;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.List;

import static io.github.jasminecrash.looksmaxxing.Looksmaxxing.MOD_ID;

public abstract class ModPackets {
    private static final int COMMAND_DATA_MAX_JSON_CHARS = 256;
    public record IssueCommandC2SPayload(
            List<Integer> entityIds,
            String commandTypeName,
            JsonElement commandDataJSON,
            boolean enqueue  // false = replace queue, true = append
    ) implements CustomPacketPayload {

        public static final CustomPacketPayload.Type<IssueCommandC2SPayload> TYPE =
                new CustomPacketPayload.Type<>(
                        Identifier.fromNamespaceAndPath(MOD_ID, "issue_command")
                );

        public static final StreamCodec<RegistryFriendlyByteBuf, IssueCommandC2SPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.INT.apply(ByteBufCodecs.list()),          IssueCommandC2SPayload::entityIds,
                        ByteBufCodecs.STRING_UTF8,                              IssueCommandC2SPayload::commandTypeName,
                        ByteBufCodecs.lenientJson(COMMAND_DATA_MAX_JSON_CHARS), IssueCommandC2SPayload::commandDataJSON,
                        ByteBufCodecs.BOOL,                                     IssueCommandC2SPayload::enqueue,
                        IssueCommandC2SPayload::new
                );

        @Override
        public CustomPacketPayload.@NonNull Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public static void registerPackets() {
        PayloadTypeRegistry.serverboundPlay().register(
                IssueCommandC2SPayload.TYPE,
                IssueCommandC2SPayload.STREAM_CODEC
        );
    }
}
