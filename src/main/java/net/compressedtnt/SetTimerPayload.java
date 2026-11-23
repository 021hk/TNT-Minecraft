package net.compressedtnt;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Client -> server: "set the fuse timer of the compressed TNT item I am
 * holding to N seconds" (sent by the G-key input screen). seconds = 0 removes
 * the timer from the item again.
 */
public record SetTimerPayload(int seconds) implements CustomPayload {
        public static final CustomPayload.Id<SetTimerPayload> ID =
                        new CustomPayload.Id<>(Identifier.of(CompressedTnt.MOD_ID, "set_timer"));

        public static final PacketCodec<RegistryByteBuf, SetTimerPayload> CODEC = new PacketCodec<>() {
                @Override
                public void encode(RegistryByteBuf buf, SetTimerPayload value) {
                        buf.writeVarInt(Math.max(0, Math.min(3600, value.seconds)));
                }

                @Override
                public SetTimerPayload decode(RegistryByteBuf buf) {
                        return new SetTimerPayload(Math.max(0, Math.min(3600, buf.readVarInt())));
                }
        };

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
                return ID;
        }
}
