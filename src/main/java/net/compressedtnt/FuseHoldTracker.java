package net.compressedtnt;

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
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The unified fuse timer for EVERY TNT in the mod (vanilla TNT, all 10
 * compressed tiers and the 3 material TNTs):
 *
 * <pre>
 *  1. hold the right mouse button on a placed TNT block for about 1 second;
 *  2. a chat prompt appears: type {@code /tntfuse <seconds>} (1-600);
 *  3. the TNT starts blinking immediately and explodes exactly that many
 *     seconds after you send the command;
 *  4. if you never set a timer the block simply behaves like vanilla TNT
 *     (ignite with flint &amp; steel / redstone / explosion - normal 4 s fuse).
 * </pre>
 *
 * The client repeats the "use block" packet every 4 ticks while the right
 * button is held down, so the server just counts consecutive interactions on
 * the same block. After {@link #HOLD_TICKS} (16 ticks) of continuous holding
 * the prompt is sent once and the block position is remembered for the
 * {@code /tntfuse} command.
 */
public final class FuseHoldTracker {
        /** how many ticks (of continuous holding) until the fuse prompt shows */
        private static final int HOLD_TICKS = 16;
        /** missed interaction repeats allowed before a hold is considered broken */
        private static final int GAP_TICKS = 8;
        /** how long (in ticks) a prompted position stays valid for /tntfuse */
        private static final int REQUEST_TTL = 600;
        /** how far the command raycasts when there is no fresh hold request */
        private static final double RAYCAST_DISTANCE = 6.0;

        private record Hold(ServerWorld world, BlockPos pos, long firstTick, long lastTick, boolean prompted) {
                Hold continued(long now) {
                        return new Hold(this.world, this.pos, this.firstTick, now, this.prompted);
                }
        }

        private record Request(ServerWorld world, BlockPos pos, long tick) {
        }

        private static final Map<UUID, Hold> HOLDS = new HashMap<>();
        private static final Map<UUID, Request> REQUESTS = new HashMap<>();

        private FuseHoldTracker() {
        }

        /** Called from every TNT block's onUseWithItem while holding right-click. */
        public static void onTntUse(ServerPlayerEntity player, ServerWorld world, BlockPos pos) {
                long now = world.getTime();
                Hold h = HOLDS.get(player.getUuid());
                if (h == null || h.world() != world || !h.pos().equals(pos) || now - h.lastTick() > GAP_TICKS) {
                        h = new Hold(world, pos, now, now, false);
                } else {
                        h = h.continued(now);
                }
                HOLDS.put(player.getUuid(), h);

                if (!h.prompted() && now - h.firstTick() >= HOLD_TICKS) {
                        HOLDS.put(player.getUuid(), new Hold(world, pos, h.firstTick(), now, true));
                        REQUESTS.put(player.getUuid(), new Request(world, pos, now));
                        player.sendMessage(Text.translatable("compressedtnt.fuse_prompt").formatted(Formatting.GOLD), false);
                        world.playSound(null, pos, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.BLOCKS, 1.0f, 1.6f);
                }
        }

        /** Removes stale holds / requests; called once per server tick. */
        public static void endServerTick(MinecraftServer server) {
                HOLDS.values().removeIf(h -> h.world().getServer() == null
                                || server.getTicks() - h.lastTick() > 200);
                REQUESTS.entrySet().removeIf(e -> {
                        Request r = e.getValue();
                        return server.getTicks() - r.tick() > REQUEST_TTL;
                });
        }

        /**
         * Resolves the TNT the player wants to arm (fresh hold request first,
         * then a short raycast) and primes it with the requested fuse. Returns
         * {@code true} when a TNT was primed.
         */
        public static boolean primeFromCommand(ServerPlayerEntity player, ServerWorld world, int seconds) {
                Request request = REQUESTS.remove(player.getUuid());
                if (request != null && request.world() == world
                                && isTntBlock(world.getBlockState(request.pos()))) {
                        return primeTntWithFuse(world, request.pos(), player, seconds);
                }

                HitResult hit = player.raycast(RAYCAST_DISTANCE, 0.0F, false);
                if (hit.getType() == HitResult.Type.BLOCK) {
                        BlockPos pos = BlockPos.ofFloored(hit.getPos());
                        // step down one block as well: the ray often lands on the top face
                        for (BlockPos candidate : new BlockPos[]{pos, pos.down()}) {
                                if (isTntBlock(world.getBlockState(candidate))) {
                                        return primeTntWithFuse(world, candidate, player, seconds);
                                }
                        }
                }
                return false;
        }

        private static boolean isTntBlock(BlockState state) {
                return state.getBlock() instanceof TntBlock;
        }

        /**
         * Turns a placed TNT block (vanilla, compressed tier or material TNT)
         * into its primed entity with a custom fuse of {@code seconds} seconds.
         */
        public static boolean primeTntWithFuse(ServerWorld world, BlockPos pos, ServerPlayerEntity player, int seconds) {
                BlockState state = world.getBlockState(pos);
                Block block = state.getBlock();
                if (!(block instanceof TntBlock)) {
                        return false;
                }
                int fuseTicks = Math.max(1, seconds) * 20;
                double x = pos.getX() + 0.5;
                double y = pos.getY();
                double z = pos.getZ() + 0.5;

                TntEntity entity;
                if (block instanceof CompressedTntBlock compressed) {
                        entity = new CompressedTntEntity(world, x, y, z, compressed.tier, compressed.power(), fuseTicks);
                } else if (block instanceof MaterialTntBlock material) {
                        entity = new MaterialTntEntity(world, x, y, z, material.type, fuseTicks);
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
}


