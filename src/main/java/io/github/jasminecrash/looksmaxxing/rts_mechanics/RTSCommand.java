package io.github.jasminecrash.looksmaxxing.rts_mechanics;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import io.github.jasminecrash.looksmaxxing.networking.ModPackets;
import net.minecraft.world.entity.Mob;

import java.util.ArrayList;
import java.util.List;

public final class RTSCommand {
    private final RTSCommandType<?> type;
    private final Object data;

    private <D> RTSCommand(RTSCommandType<D> type, D data) {
        this.type = type;
        this.data = data;
    }

    public static <D> RTSCommand of(RTSCommandType<D> type, D data) {
        return new RTSCommand(type, data);
    }

    public RTSCommandType<?> type() { return type; }

    //I Will Type-Check MYSELF, Thank You Very Much.
    @SuppressWarnings("unchecked")
    public <D> D data(RTSCommandType<D> expectedType) {
        if (this.type != expectedType) {
            throw new IllegalArgumentException("Command type mismatch");
        }
        return (D) data;
    }

    //fire the type's onStart method with the stored command data
    @SuppressWarnings("unchecked")
    public void start(Mob mob) {
        ((RTSCommandType<Object>) type).onStart(mob, data);
    }

    public static <D> ModPackets.IssueCommandC2SPayload toPayload(List<Integer> entityIds, RTSCommandType<D> type, D data, boolean enqueue) {
        JsonElement json = type.codec()
                .encodeStart(JsonOps.INSTANCE, data)
                .getOrThrow(msg -> new IllegalArgumentException("Failed to encode command data: " + msg));

        return new ModPackets.IssueCommandC2SPayload(entityIds, RTSCommandTypes.nameOf(type), json, enqueue);
    }
    public static <D> ModPackets.IssueCommandC2SPayload toPayload(int entityId, RTSCommandType<D> type, D data, boolean enqueue) {
        ArrayList<Integer> list = new ArrayList<>();
        list.add(entityId);
        return toPayload(list, type, data, enqueue);
    }
}
