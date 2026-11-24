package net.compressedtnt;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * Networking for the fuse-timer GUI:
 * <ul>
 *  <li>server -> client {@link OpenFuseScreen}: "open the timer screen for the TNT at this position";</li>
 *  <li>client -> server {@link SetFuse}: "prime that TNT with this many seconds" (0 = normal 4 s fuse).</li>
 * </ul>
 */
public final class FuseNet {
        private FuseNet() {
        }

        public record OpenFuseScreen(BlockPos pos) implements CustomPayload {
                public static final CustomPayload.Id<OpenFuseScreen> ID =
                                new CustomPayload.Id<>(Identifier.of(CompressedTnt.MOD_ID, "open_fuse_screen"));
                public static final PacketCodec<RegistryByteBuf, OpenFuseScreen> CODEC = PacketCodec.tuple(
                                BlockPos.PACKET_CODEC, OpenFuseScreen::pos,
                                OpenFuseScreen::new);

                @Override
                public CustomPayload.Id<? extends CustomPayload> getId() {
                        return ID;
                }
        }

        public record SetFuse(BlockPos pos, int seconds) implements CustomPayload {
                public static final CustomPayload.Id<SetFuse> ID =
                                new CustomPayload.Id<>(Identifier.of(CompressedTnt.MOD_ID, "set_fuse"));
                public static final PacketCodec<RegistryByteBuf, SetFuse> CODEC = PacketCodec.tuple(
                                BlockPos.PACKET_CODEC, SetFuse::pos,
                                PacketCodecs.VAR_INT, SetFuse::seconds,
                                SetFuse::new);

                @Override
                public CustomPayload.Id<? extends CustomPayload> getId() {
                        return ID;
                }
        }

        public static void registerCodecs() {
                PayloadTypeRegistry.playS2C().register(OpenFuseScreen.ID, OpenFuseScreen.CODEC);
                PayloadTypeRegistry.playC2S().register(SetFuse.ID, SetFuse.CODEC);
        }
}
