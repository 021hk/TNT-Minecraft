package net.compressedtnt;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.explosion.Explosion;

/**
 * Primed compressed TNT. Blinking, physics and ignition paths are exactly
 * like vanilla primed TNT; the detonation is a nuke: an expanding spherical
 * shockwave that vaporizes every block inside the tier radius.
 *
 * v1.3.0 power model - strictly relative, 9x per tier:
 * the DESTROYED VOLUME grows by a factor of 9 with each tier, so the radius
 * grows by 9^(1/3) = 2.08 per tier: 8 -&gt; 17 -&gt; 36 -&gt; 75 blocks.
 * Tier 5 would need radius 156; the engine caps it at 128 (bigger waves are
 * not feasible in a Minecraft world), so tiers 5-10 share the max radius and
 * gain extra power instead:
 *   tier 6+: also vaporizes obsidian / crying obsidian / reinforced deepslate
 *   tier 7+: also vaporizes bedrock (except the bottom-most layer of the world)
 *
 * "Not all blocks explode" fix: the wave enumerates the sphere in 1-block
 * thick shells (no gaps at all) and AFTER the wave a second, complete sweep
 * re-scans the whole sphere and destroys anything that is left over (sand
 * that fell in, chunks that were not loaded during the wave, ...). The sweep
 * even loads still-unloaded chunks in small batches.
 */
public class CompressedTntEntity extends TntEntity {
        /** shell thickness of the shockwave (1.0 = gapless, no leftover shells) */
        private static final double SHELL_STEP = 1.0;
        /** max blocks vaporized per tick - keeps the server responsive */
        private static final int DESTROY_BUDGET = 100_000;
        /** max blocks scanned per tick during the final sweep */
        private static final int SWEEP_BUDGET = 200_000;
        /** max chunks generated per tick during the final sweep */
        private static final int SWEEP_CHUNK_BUDGET = 4;
        /**
         * destruction radius per tier - strictly 9x volume per tier up to the
         * engine cap of 128 (4 * 9^(tier/3), capped).
         */
        private static final double[] RADII = {8.0, 17.0, 36.0, 75.0, 128.0, 128.0, 128.0, 128.0, 128.0, 128.0};

        private int tier = 1;
        private float blastPower = 36.0f;
        private int remainingTicks = 80;
        private boolean nuking = false;
        private boolean sweeping = false;
        private double nukeMaxRadius = 8.0;
        private int nukeShell = 0;
        private int nukeColumn = -1; // next column index in the current shell; -1 = shell done
        private Vec3d center = Vec3d.ZERO;

        public CompressedTntEntity(EntityType<? extends CompressedTntEntity> type, World world) {
                super(type, world);
        }

        public CompressedTntEntity(World world, double x, double y, double z, int tier, float power, int fuseTicks) {
                super(CompressedTnt.PRIMED_COMPRESSED_TNT, world);
                this.setPosition(x, y, z);
                this.tier = tier;
                this.blastPower = power;
                this.remainingTicks = Math.max(1, fuseTicks);
                this.nukeMaxRadius = radiusOf(this.tier);
                // the fuse stored in the vanilla data tracker is only used by the
                // renderer for the flashing effect; the real countdown is remainingTicks
                super.setFuse(this.remainingTicks);
        }

        /** destruction radius (in blocks) for a tier */
        public static double radiusOf(int tier) {
                int idx = MathHelper.clamp(tier, 1, RADII.length) - 1;
                return RADII[idx];
        }

        private boolean destroysObsidian() {
                return this.tier >= 6;
        }

        private boolean destroysBedrock() {
                return this.tier >= 7;
        }

        @Override
        public void tick() {
                World world = this.getWorld();
                if (world.isClient) {
                        // fuse is synced through the vanilla data tracker, so long
                        // custom fuses (e.g. /tntfuse 120) blink correctly as well
                        int fuse = this.getFuse();
                        if (fuse <= 0) {
                                this.discard();
                                return;
                        }
                        if (this.random.nextInt(2) == 0) {
                                world.addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.0, 0.0);
                        }
                        return;
                }

                if (this.nuking) {
                        this.tickNuke((ServerWorld) world);
                        return;
                }

                this.remainingTicks--;
                super.setFuse(Math.max(this.remainingTicks, 0));

                if (!this.hasNoGravity()) {
                        this.setVelocity(this.getVelocity().add(0.0, -0.04, 0.0));
                }
                this.move(MovementType.SELF, this.getVelocity());
                this.setVelocity(this.getVelocity().multiply(0.98));
                if (this.isOnGround()) {
                        this.setVelocity(this.getVelocity().multiply(0.7, -0.5, 0.7));
                }

                if (this.remainingTicks <= 0) {
                        this.startNuke((ServerWorld) world);
                }
        }

        private void startNuke(ServerWorld server) {
                this.nuking = true;
                this.center = new Vec3d(this.getX(), this.getY() + 0.49, this.getZ());
                this.nukeShell = 0;
                this.nukeColumn = 0;
                this.setNoGravity(true);
                this.setInvisible(true);
                CompressedTnt.LOGGER.info("Nuke start tier={} center=({}, {}, {}) radius={}", this.tier,
                                Math.round(this.center.x * 10.0) / 10.0, Math.round(this.center.y * 10.0) / 10.0,
                                Math.round(this.center.z * 10.0) / 10.0, (int) this.nukeMaxRadius);
                // keep every chunk of the blast area loaded for the whole nuke:
                // without this the sweep would generate and re-generate border
                // chunks every tick (they unload again immediately) and could
                // never finish
                this.forceNukeChunks(server, true);
                server.playSound(null, BlockPos.ofFloored(this.center), SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.BLOCKS, 4.0f, 0.5f);
                server.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, this.center.x, this.center.y, this.center.z, 8, 2.0, 2.0, 2.0, 0.0);
                server.spawnParticles(ParticleTypes.FLASH, this.center.x, this.center.y, this.center.z, 4, 0.0, 0.0, 0.0, 0.0);
        }

        private void forceNukeChunks(ServerWorld server, boolean forced) {
                int r = (int) Math.ceil(this.nukeMaxRadius);
                if (r < 12) {
                        return; // small blasts only touch chunks near the igniter anyway
                }
                int minX = MathHelper.floor(this.center.x - r) >> 4;
                int maxX = MathHelper.floor(this.center.x + r) >> 4;
                int minZ = MathHelper.floor(this.center.z - r) >> 4;
                int maxZ = MathHelper.floor(this.center.z + r) >> 4;
                for (int cx = minX; cx <= maxX; cx++) {
                        for (int cz = minZ; cz <= maxZ; cz++) {
                                server.setChunkForced(cx, cz, forced);
                        }
                }
        }

        @Override
        public void onRemoved() {
                super.onRemoved();
                World world = this.getWorld();
                if (world instanceof ServerWorld server) {
                        this.forceNukeChunks(server, false); // release our /forceload-style tickets
                }
        }

        private void tickNuke(ServerWorld server) {
                if (this.sweeping) {
                        this.tickSweep(server);
                        return;
                }

                this.tickWork = 0;
                this.tickDestroyed = 0;
                double rPrev = Math.min(this.nukeShell * SHELL_STEP, this.nukeMaxRadius);
                double rCur = Math.min((this.nukeShell + 1) * SHELL_STEP, this.nukeMaxRadius);
                while ((this.tickDestroyed < DESTROY_BUDGET && this.tickWork < WAVE_SCAN_BUDGET)
                                && rPrev < this.nukeMaxRadius) {
                        int shellBefore = this.nukeShell;
                        int colBefore = this.nukeColumn;
                        this.processShell(server, rPrev, rCur, false);
                        if (this.nukeColumn < 0) {
                                // shell finished, move to the next one
                                this.nukeShell++;
                                this.nukeColumn = 0;
                                rPrev = Math.min(this.nukeShell * SHELL_STEP, this.nukeMaxRadius);
                                rCur = Math.min((this.nukeShell + 1) * SHELL_STEP, this.nukeMaxRadius);
                        } else if (this.nukeShell == shellBefore && this.nukeColumn == colBefore) {
                                break; // defensive: no progress this call
                        }
                }

                // shockwave effects + damage every tick
                server.playSound(null, BlockPos.ofFloored(this.center), SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.BLOCKS, 3.0f, 0.4f + this.random.nextFloat() * 0.4f);
                for (int i = 0; i < 3; i++) {
                        Vec3d p = this.randomShellPoint(rPrev, rCur);
                        server.spawnParticles(ParticleTypes.EXPLOSION, p.x, p.y, p.z, 1, 0.0, 0.0, 0.0, 0.0);
                }
                if (this.random.nextInt(3) == 0) {
                        Vec3d p = this.randomShellPoint(rPrev, rCur);
                        server.spawnParticles(ParticleTypes.FLASH, p.x, p.y, p.z, 1, 0.0, 0.0, 0.0, 0.0);
                }

                DamageSource damage = server.getDamageSources().explosion((Explosion) null);
                Box box = Box.of(this.center, rCur * 2.0, rCur * 2.0, rCur * 2.0);
                for (Entity entity : server.getOtherEntities(this, box)) {
                        if (entity instanceof PlayerEntity player && (player.getAbilities().creativeMode || player.isSpectator())) {
                                continue;
                        }
                        if (entity.isInvulnerable() || !entity.isAlive()) {
                                continue;
                        }
                        entity.damage(damage, this.blastPower * 2.0f);
                        Vec3d away = entity.getPos().subtract(this.center);
                        if (away.lengthSquared() > 1.0e-4) {
                                Vec3d dir = away.normalize();
                                entity.addVelocity(dir.x * 3.0, 1.2, dir.z * 3.0);
                                entity.velocityModified = true;
                        }
                }

                if (rPrev >= this.nukeMaxRadius) {
                        // wave complete - start the no-leftover sweep
                        CompressedTnt.LOGGER.info("Nuke wave done tier={} (sweeping now)", this.tier);
                        this.sweeping = true;
                        this.nukeShell = 0;
                        this.nukeColumn = 0;
                        this.sweepTicks = 0;
                }
        }

        private int sweepTicks = 0;
        private int chunksLoadedThisTick = 0;
        private int tickWork = 0;
        private int tickDestroyed = 0;
        private static final int MAX_SWEEP_TICKS = 900;
        private static final int SWEEP_CHUNKS_PER_TICK = 4;
        private static final int WAVE_SCAN_BUDGET = 400_000;

        /**
         * Second, complete pass over the same sphere: destroys anything the
         * wave missed (sand that fell into the crater mid-wave, chunks that
         * were not loaded yet, edge rounding) and loads still-unloaded chunks
         * in small batches so nothing can hide outside the loaded area.
         */
        private void tickSweep(ServerWorld server) {
                this.sweepTicks++;
                this.chunksLoadedThisTick = 0;
                this.tickWork = 0;
                this.tickDestroyed = 0;
                double r = this.nukeMaxRadius;
                double rPrev = Math.min(this.nukeShell * SHELL_STEP, r);
                double rCur = Math.min((this.nukeShell + 1) * SHELL_STEP, r);
                while (this.tickWork < SWEEP_BUDGET && rPrev < r) {
                        int shellBefore = this.nukeShell;
                        int colBefore = this.nukeColumn;
                        this.processShell(server, rPrev, rCur, true);
                        if (this.nukeColumn < 0) {
                                this.nukeShell++;
                                this.nukeColumn = 0;
                                rPrev = Math.min(this.nukeShell * SHELL_STEP, r);
                                rCur = Math.min((this.nukeShell + 1) * SHELL_STEP, r);
                        } else if (this.nukeShell == shellBefore && this.nukeColumn == colBefore) {
                                // no progress made (chunk generation budget exhausted) -
                                // stop for this tick and resume on the next one
                                break;
                        }
                }

                if (this.sweepTicks % 60 == 0 && rPrev < r) {
                        CompressedTnt.LOGGER.info("Nuke sweep tier={} shell={}/{} work={}/{} chunks={}",
                                        this.tier, this.nukeShell, (int) r, this.tickWork, SWEEP_BUDGET, this.chunksLoadedThisTick);
                        server.playSound(null, BlockPos.ofFloored(this.center), SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.BLOCKS, 2.0f, 0.6f);
                }

                if (rPrev >= r || this.sweepTicks > MAX_SWEEP_TICKS) {
                        CompressedTnt.LOGGER.info("Nuke sweep finished tier={} sweepTicks={} (timeout={})",
                                        this.tier, this.sweepTicks, this.sweepTicks > MAX_SWEEP_TICKS);
                        server.playSound(null, BlockPos.ofFloored(this.center), SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.BLOCKS, 5.0f, 0.3f);
                        this.discard();
                }
        }

        private Vec3d randomShellPoint(double rPrev, double r) {
                double angle = this.random.nextDouble() * Math.PI * 2.0;
                double pitch = (this.random.nextDouble() - 0.5) * Math.PI;
                double radius = rPrev + this.random.nextDouble() * Math.max(r - rPrev, 1.0);
                return new Vec3d(
                                this.center.x + radius * Math.cos(pitch) * Math.cos(angle),
                                this.center.y + radius * Math.sin(pitch),
                                this.center.z + radius * Math.cos(pitch) * Math.sin(angle));
        }

        /**
         * Enumerates the spherical shell [rPrev, r] column by column with a
         * budget cursor. Every block whose center lies in the shell is checked
         * exactly once - gapless by construction. Progress is accumulated into
         * {@link #tickWork} (blocks examined) and {@link #tickDestroyed}
         * (blocks removed); {@code sweep=true} paces by examined blocks,
         * {@code sweep=false} by destroyed blocks.
         */
        private void processShell(ServerWorld world, double rPrev, double r, boolean sweep) {
                int minX = MathHelper.floor(this.center.x - r);
                int maxX = MathHelper.floor(this.center.x + r);
                int minZ = MathHelper.floor(this.center.z - r);
                int maxZ = MathHelper.floor(this.center.z + r);
                int zSpan = maxZ - minZ + 1;
                int total = (maxX - minX + 1) * zSpan;
                int idx = Math.max(this.nukeColumn, 0);
                if (idx >= total) {
                        this.nukeColumn = -1;
                        return;
                }

                int yFloor = world.getBottomY();
                int yCeil = world.getTopY() - 1;
                double r2 = r * r;
                double rp2 = rPrev * rPrev;

                for (; idx < total; idx++) {
                        int x = minX + idx / zSpan;
                        int z = minZ + idx % zSpan;
                        if (sweep) {
                                // the sweep loads missing chunks (small batch per tick) so
                                // unloaded areas cannot hide leftover blocks
                                if (world.getChunk(x >> 4, z >> 4, ChunkStatus.FULL, false) == null) {
                                        if (this.chunksLoadedThisTick >= SWEEP_CHUNKS_PER_TICK) {
                                                this.nukeColumn = idx; // continue (and load) next tick
                                                return;
                                        }
                                        this.chunksLoadedThisTick++;
                                        if (world.getChunk(x >> 4, z >> 4, ChunkStatus.FULL, true) == null) {
                                                continue;
                                        }
                                }
                        } else if (world.getChunk(x >> 4, z >> 4, ChunkStatus.FULL, false) == null) {
                                continue; // never trigger worldgen lag during the wave itself
                        }
                        double dx = x + 0.5 - this.center.x;
                        double dz = z + 0.5 - this.center.z;
                        double dXZ2 = dx * dx + dz * dz;
                        if (dXZ2 > r2) {
                                continue;
                        }
                        double outerHalf = Math.sqrt(r2 - dXZ2);
                        // the y ranges must OVER-approximate the sphere slice: a block
                        // counts as inside when its CENTER is within the radius, so the
                        // bounds have to be widened by half a block. The previous formula
                        // (ceil(cy - outerHalf)) dropped the bottom boundary block of
                        // every shell whenever the boundary landed mid-block - and the
                        // next shell then skipped it again because d <= rPrev. Those
                        // dropped blocks were exactly the "leftover blocks" the player
                        // reported after every explosion.
                        int outerTop = Math.min(yCeil, MathHelper.floor(this.center.y + outerHalf + 0.5));
                        int outerBottom = Math.max(yFloor, MathHelper.ceil(this.center.y - outerHalf - 0.5));
                        if (dXZ2 <= rp2) {
                                double innerHalf = Math.sqrt(rp2 - dXZ2);
                                int innerTop = MathHelper.floor(this.center.y + innerHalf - 0.5);
                                int innerBottom = MathHelper.ceil(this.center.y - innerHalf + 0.5);
                                // the ranges INCLUDE the inner boundary layers; the exact
                                // distance check inside vaporizeColumn skips anything that
                                // is still inside the inner sphere
                                this.tickDestroyed += this.vaporizeColumn(world, x, z, innerTop, outerTop, r2, rp2);
                                this.tickDestroyed += this.vaporizeColumn(world, x, z, outerBottom, innerBottom, r2, rp2);
                                // count only the y ranges actually examined (thin shell slices),
                                // NOT the full column - otherwise the pacing budget is wildly
                                // overestimated and the sweep would take thousands of ticks
                                this.tickWork += Math.max(0, outerTop - innerTop + 1)
                                                + Math.max(0, innerBottom - outerBottom + 1);
                        } else {
                                this.tickDestroyed += this.vaporizeColumn(world, x, z, outerBottom, outerTop, r2, rp2);
                                this.tickWork += Math.max(1, outerTop - outerBottom + 1);
                        }
                        boolean budgetHit = sweep ? this.tickWork >= SWEEP_BUDGET
                                        : (this.tickDestroyed >= DESTROY_BUDGET || this.tickWork >= WAVE_SCAN_BUDGET);
                        if (budgetHit) {
                                this.nukeColumn = idx + 1; // continue with the next column next tick
                                return;
                        }
                }
                this.nukeColumn = -1;
        }

        private int vaporizeColumn(ServerWorld world, int x, int z, int yFrom, int yTo, double r2, double rp2) {
                int destroyed = 0;
                for (int y = yFrom; y <= yTo; y++) {
                        double dx = x + 0.5 - this.center.x;
                        double dy = y + 0.5 - this.center.y;
                        double dz = z + 0.5 - this.center.z;
                        double d2 = dx * dx + dy * dy + dz * dz;
                        if (d2 > r2 || d2 <= rp2) {
                                continue;
                        }
                        BlockPos pos = new BlockPos(x, y, z);
                        BlockState state = world.getBlockState(pos);
                        if (state.isAir()) {
                                continue;
                        }
                        if (state.isOf(Blocks.BARRIER)) {
                                continue;
                        }
                        if (state.isOf(Blocks.BEDROCK)) {
                                // tier 7+ opens the floor, but never the bottom-most layer
                                if (!this.destroysBedrock() || y <= world.getBottomY()) {
                                        continue;
                                }
                        }
                        if (!this.destroysObsidian() && (state.isOf(Blocks.OBSIDIAN) || state.isOf(Blocks.CRYING_OBSIDIAN)
                                        || state.isOf(Blocks.REINFORCED_DEEPSLATE))) {
                                continue;
                        }
                        Block block = state.getBlock();
                        if (block instanceof CompressedTntBlock compressed) {
                                // chain reaction: prime it instead of vaporizing it
                                world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                                world.spawnEntity(new CompressedTntEntity(world, x + 0.5, y, z,
                                                compressed.tier, compressed.power(), 80));
                                destroyed++;
                                continue;
                        }
                        if (block instanceof MaterialTntBlock material) {
                                world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                                world.spawnEntity(new MaterialTntEntity(world, x + 0.5, y, z, material.type, 80));
                                destroyed++;
                                continue;
                        }
                        if (state.isOf(Blocks.TNT)) {
                                world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                                world.spawnEntity(new TntEntity(world, x + 0.5, y, z, null));
                                destroyed++;
                                continue;
                        }
                        // flag 3 = notify neighbors + sync to clients: water/lava start
                        // flowing back, falling blocks fall, players can mine normally
                        world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                        destroyed++;
                }
                return destroyed;
        }

        @Override
        public void writeCustomDataToNbt(NbtCompound nbt) {
                super.writeCustomDataToNbt(nbt);
                nbt.putInt("CompressedTier", this.tier);
                nbt.putFloat("CompressedPower", this.blastPower);
                nbt.putInt("CompressedFuse", this.remainingTicks);
                nbt.putBoolean("CompressedNuking", this.nuking);
                nbt.putBoolean("CompressedSweeping", this.sweeping);
                nbt.putDouble("CompressedNukeMax", this.nukeMaxRadius);
                nbt.putInt("CompressedNukeShell", this.nukeShell);
                nbt.putInt("CompressedNukeCol", this.nukeColumn);
                nbt.putDouble("CompressedCx", this.center.x);
                nbt.putDouble("CompressedCy", this.center.y);
                nbt.putDouble("CompressedCz", this.center.z);
        }

        @Override
        public void readCustomDataFromNbt(NbtCompound nbt) {
                super.readCustomDataFromNbt(nbt);
                this.tier = Math.max(1, nbt.getInt("CompressedTier"));
                if (nbt.contains("CompressedPower", NbtElement.FLOAT_TYPE)) {
                        this.blastPower = nbt.getFloat("CompressedPower");
                } else {
                        this.blastPower = CompressedTnt.powerOf(this.tier);
                }
                this.remainingTicks = Math.max(1, nbt.contains("CompressedFuse", NbtElement.INT_TYPE)
                                ? nbt.getInt("CompressedFuse") : 80);
                super.setFuse(this.remainingTicks);
                this.nuking = nbt.getBoolean("CompressedNuking");
                this.sweeping = nbt.getBoolean("CompressedSweeping");
                this.nukeMaxRadius = nbt.contains("CompressedNukeMax", NbtElement.DOUBLE_TYPE)
                                ? nbt.getDouble("CompressedNukeMax") : radiusOf(this.tier);
                this.nukeShell = Math.max(0, nbt.getInt("CompressedNukeShell"));
                this.nukeColumn = (nuking || sweeping) ? Math.max(-1, nbt.getInt("CompressedNukeCol")) : -1;
                this.center = new Vec3d(nbt.getDouble("CompressedCx"), nbt.getDouble("CompressedCy"), nbt.getDouble("CompressedCz"));
                if (this.center.equals(Vec3d.ZERO)) {
                        this.center = new Vec3d(this.getX(), this.getY() + 0.49, this.getZ());
                }
        }
}
