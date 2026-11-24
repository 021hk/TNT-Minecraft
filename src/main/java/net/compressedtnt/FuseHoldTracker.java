package net.compressedtnt;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TntBlock;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The unified fuse timer for EVERY TNT (the 10 compressed tiers, the 4
 * material TNTs, the 2 tunnel TNTs and VANILLA TNT):
 *
 * <pre>
 *  1. right-click a placed TNT block - a screen opens;
 *  2. type the seconds (0-3600) and press "Start";
 *  3. the TNT is primed exactly like vanilla (it jumps, blinks and falls),
 *     but with the requested fuse - e.g. 120 s;
 *  4. "No timer" (or 0) primes it with the normal 4-second fuse;
 *  5. without any screen use it behaves 100% like vanilla TNT (flint &amp;
 *     steel, redstone, fire, explosions).
 * </pre>
 *
 * The {@code /tntfuse <seconds>} command is kept as a keyboard-only backup:
 * it uses the last right-clicked position, or a short raycast.
 */
public final class FuseHoldTracker {
        /** how long (in ticks) a right-clicked position stays valid for /tntfuse */
        private static final int REQUEST_TTL = 600;
        /** how far the command raycasts when there is no fresh right-click */
        private static final double RAYCAST_DISTANCE = 6.0;
        /** maximum seconds the timer accepts */
        public static final int MAX_SECONDS = 3600;

        private record Request(ServerWorld world, BlockPos pos, long tick) {
        }

        private static final Map<UUID, Request> REQUESTS = new HashMap<>();

        private FuseHoldTracker() {
        }

        /** Opens the fuse-timer screen for the TNT block at the given position. */
        public static void openFuseScreen(ServerPlayerEntity player, ServerWorld world, BlockPos pos) {
                if (!(world.getBlockState(pos).getBlock() instanceof TntBlock)) {
                        return;
                }
                REQUESTS.put(player.getUuid(), new Request(world, pos, world.getTime()));
                ServerPlayNetworking.send(player, new FuseNet.OpenFuseScreen(pos));
        }

        /** Removes stale right-click requests; called once per server tick. */
        public static void endServerTick(MinecraftServer server) {
                REQUESTS.entrySet().removeIf(e -> server.getTicks() - e.getValue().tick() > REQUEST_TTL);
        }

        /**
         * Resolves the TNT the player wants to arm (fresh right-click first,
         * then a short raycast) and primes it with the requested fuse.
         * Returns {@code true} when a TNT was primed.
         */
        public static boolean primeFromCommand(ServerPlayerEntity player, ServerWorld world, int seconds) {
                Request request = REQUESTS.remove(player.getUuid());
                if (request != null && request.world() == world
                                && world.getBlockState(request.pos()).getBlock() instanceof TntBlock) {
                        return primeTntWithFuse(world, request.pos(), player, seconds);
                }

                HitResult hit = player.raycast(RAYCAST_DISTANCE, 0.0F, false);
                if (hit.getType() == HitResult.Type.BLOCK) {
                        BlockPos pos = BlockPos.ofFloored(hit.getPos());
                        // step down one block as well: the ray often lands on the top face
                        for (BlockPos candidate : new BlockPos[]{pos, pos.down()}) {
                                if (world.getBlockState(candidate).getBlock() instanceof TntBlock) {
                                        return primeTntWithFuse(world, candidate, player, seconds);
                                }
                        }
                }
                return false;
        }

        /**
         * Turns a placed TNT block (vanilla, compressed tier, material or
         * tunnel TNT) into its primed entity with a fuse of {@code seconds}
         * seconds; {@code seconds <= 0} uses the vanilla 4-second fuse.
         */
        public static boolean primeTntWithFuse(ServerWorld world, BlockPos pos, PlayerEntity player, int seconds) {
                BlockState state = world.getBlockState(pos);
                Block block = state.getBlock();
                if (!(block instanceof TntBlock)) {
                        return false;
                }
                int fuseTicks = seconds <= 0 ? 80 : Math.min(Math.max(1, seconds), MAX_SECONDS) * 20;
                double x = pos.getX() + 0.5;
                double y = pos.getY();
                double z = pos.getZ() + 0.5;

                TntEntity entity;
                if (block instanceof CompressedTntBlock compressed) {
                        entity = new CompressedTntEntity(world, x, y, z, compressed.tier, compressed.power(), fuseTicks);
                } else if (block instanceof MaterialTntBlock material) {
                        entity = new MaterialTntEntity(world, x, y, z, material.type, fuseTicks);
                } else if (block instanceof TunnelTntBlock tunnel) {
                        entity = new TunnelTntEntity(world, x, y, z,
                                        tunnel.mode == TunnelTntBlock.Mode.VERTICAL,
                                        tunnel.facing(state), fuseTicks);
                } else {
                        entity = new TntEntity(world, x, y, z, player);
                        entity.setFuse(fuseTicks);
                }

                world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                world.spawnEntity(entity);
                world.playSound(null, pos, SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0f, 1.0f);
                world.emitGameEvent(player, GameEvent.PRIME_FUSE, pos);
                return true;
        }

        /** Server-side receiver for the GUI "start" button. */
        public static void registerServerReceiver() {
                ServerPlayNetworking.registerGlobalReceiver(FuseNet.SetFuse.ID, (payload, context) -> {
                        ServerPlayerEntity player = context.player();
                        MinecraftServer server = player.getServer();
                        if (server == null) {
                                return;
                        }
                        server.execute(() -> {
                                ServerWorld world = player.getServerWorld();
                                BlockPos pos = payload.pos();
                                int seconds = MathHelper.clamp(payload.seconds(), 0, MAX_SECONDS);
                                if (player.isRemoved() || player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > 400.0) {
                                        return; // too far away - reject
                                }
                                if (primeTntWithFuse(world, pos, player, seconds)) {
                                        player.sendMessage(Text.translatable("compressedtnt.fuse_set", seconds)
                                                        .formatted(Formatting.GREEN), true);
                                }
                        });
                });
        }
}
