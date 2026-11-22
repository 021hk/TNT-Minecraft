package net.compressedtnt;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Sent from the client when the player presses the fuse +/- keybindings while
 * holding a compressed TNT item. The server (authority) applies the delta to
 * the fuse seconds stored in the item's custom data, clamped to 1-300.
 */
public record SetFusePayload(int delta) implements CustomPayload {
        public static final CustomPayload.Id<SetFusePayload> ID =
                        new CustomPayload.Id<>(Identifier.of(CompressedTnt.MOD_ID, "set_fuse"));

        public static final PacketCodec<RegistryByteBuf, SetFusePayload> CODEC =
                        PacketCodec.tuple(PacketCodecs.VAR_INT, SetFusePayload::delta, SetFusePayload::new);

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
                return ID;
        }
}
