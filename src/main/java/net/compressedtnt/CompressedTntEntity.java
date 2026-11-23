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
 * Primed compressed TNT. Behaves like vanilla primed TNT while counting down,
 * but instead of a regular explosion it detonates as a "nuke": a spherical
 * shockwave that expands over several ticks, vaporizing blocks ring by ring,
 * priming other (compressed) TNT blocks it reaches, throwing and killing
 * entities, with continuous explosion sounds and particles.
 *
 * v1.2.0 fixes:
 *  - each tier now has its own, clearly bigger destruction radius
 *  - the ring geometry no longer leaves leftover blocks (the previous code
 *    skipped the block layer right at the inner shell boundary)
 *  - destroyed blocks are set to air WITH neighbor updates (flag 3), so
 *    water/lava flow again and players can normally mine the area
 *  - destruction is paced per tick (budget) so even tier 10 does not freeze
 *    the server; the wave rolls outward over several seconds
 */
public class CompressedTntEntity extends TntEntity {
        private static final double SHELL_STEP = 4.0;
        /** max blocks vaporized per tick - keeps the server responsive */
        private static final int DESTROY_BUDGET = 50_000;
        /** one clearly different radius per tier: 8 + 8*(tier-1) blocks */
        private static final double[] RADII = {8, 16, 24, 32, 40, 48, 56, 64, 72, 80};

        private int tier = 1;
        private float blastPower = 36.0f;
        private int remainingTicks = 80;
        private boolean nuking = false;
        private double nukeMaxRadius = 14.0;
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

        @Override
        public void tick() {
                World world = this.getWorld();
                if (this.nuking) {
                        this.tickNuke();
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

                if (world.isClient && this.random.nextInt(2) == 0) {
                        world.addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.0, 0.0);
                }

                if (this.remainingTicks <= 0) {
                        if (world.isClient) {
                                this.discard();
                        } else {
                                // server keeps the (now invisible) entity alive to run the
                                // expanding shockwave over the next ticks
                                this.startNuke();
                        }
                }
        }

        private void startNuke() {
                World world = this.getWorld();
                this.nuking = true;
                this.center = new Vec3d(this.getX(), this.getY() + 0.49, this.getZ());
                this.nukeShell = 0;
                this.nukeColumn = 0;
                this.setNoGravity(true);
                this.setInvisible(true);
                world.playSound(null, BlockPos.ofFloored(this.center), SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.BLOCKS, 4.0f, 0.5f);
                if (world instanceof ServerWorld server) {
                        server.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, this.center.x, this.center.y, this.center.z, 8, 2.0, 2.0, 2.0, 0.0);
                        server.spawnParticles(ParticleTypes.FLASH, this.center.x, this.center.y, this.center.z, 4, 0.0, 0.0, 0.0, 0.0);
                }
        }

        private void tickNuke() {
                World world = this.getWorld();
                if (!(world instanceof ServerWorld server)) {
                        return; // client side: effects are synced from the server
                }

                double rPrev = Math.min(this.nukeShell * SHELL_STEP, this.nukeMaxRadius);
                double rCur = Math.min((this.nukeShell + 1) * SHELL_STEP, this.nukeMaxRadius);
                int destroyed = 0;
                while (destroyed < DESTROY_BUDGET && rPrev < this.nukeMaxRadius) {
                        destroyed += this.destroyShellBudgeted(server, rPrev, rCur);
                        if (this.nukeColumn < 0) {
                                // shell finished, move to the next one
                                this.nukeShell++;
                                this.nukeColumn = 0;
                                rPrev = Math.min(this.nukeShell * SHELL_STEP, this.nukeMaxRadius);
                                rCur = Math.min((this.nukeShell + 1) * SHELL_STEP, this.nukeMaxRadius);
                        }
                }

                // shockwave effects + damage every tick
                world.playSound(null, BlockPos.ofFloored(this.center), SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.BLOCKS, 3.0f, 0.4f + this.random.nextFloat() * 0.4f);
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
                        world.playSound(null, BlockPos.ofFloored(this.center), SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.BLOCKS, 5.0f, 0.3f);
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
         * Destroys the spherical shell [rPrev, r], starting from {@code nukeColumn}
         * and stopping when the per-tick budget is used up (the cursor is stored so
         * the shell continues on the next tick). Every block whose CENTER lies
         * strictly between the two spheres is destroyed - the enumeration covers
         * the full bounding box, so no block can be missed.
         */
        private int destroyShellBudgeted(ServerWorld world, double rPrev, double r) {
                int minX = MathHelper.floor(this.center.x - r);
                int maxX = MathHelper.floor(this.center.x + r);
                int minZ = MathHelper.floor(this.center.z - r);
                int maxZ = MathHelper.floor(this.center.z + r);
                int zSpan = maxZ - minZ + 1;
                int total = (maxX - minX + 1) * zSpan;
                int idx = Math.max(this.nukeColumn, 0);
                if (idx >= total) {
                        this.nukeColumn = -1;
                        return 0;
                }

                int yFloor = world.getBottomY();
                int yCeil = world.getTopY() - 1;
                double r2 = r * r;
                double rp2 = rPrev * rPrev;
                int destroyedTotal = 0;

                for (; idx < total; idx++) {
                        int x = minX + idx / zSpan;
                        int z = minZ + idx % zSpan;
                        if (world.getChunk(x >> 4, z >> 4, ChunkStatus.FULL, false) == null) {
                                continue; // skip unloaded chunks so we never trigger worldgen lag
                        }
                        double dx = x + 0.5 - this.center.x;
                        double dz = z + 0.5 - this.center.z;
                        double dXZ2 = dx * dx + dz * dz;
                        if (dXZ2 > r2) {
                                continue;
                        }
                        double outerHalf = Math.sqrt(r2 - dXZ2);
                        int outerTop = Math.min(yCeil, MathHelper.floor(this.center.y + outerHalf));
                        int outerBottom = Math.max(yFloor, MathHelper.ceil(this.center.y - outerHalf));
                        if (dXZ2 <= rp2) {
                                double innerHalf = Math.sqrt(rp2 - dXZ2);
                                int innerTop = MathHelper.floor(this.center.y + innerHalf);
                                int innerBottom = MathHelper.ceil(this.center.y - innerHalf);
                                // NOTE: the ranges INCLUDE the inner boundary layers; the exact
                                // distance check inside vaporizeColumn skips anything that is
                                // still inside the inner sphere. Widening the range by one layer
                                // is what fixes the "some blocks never explode" bug.
                                destroyedTotal += this.vaporizeColumn(world, x, z, innerTop, outerTop, r2, rp2);
                                destroyedTotal += this.vaporizeColumn(world, x, z, outerBottom, innerBottom, r2, rp2);
                        } else {
                                destroyedTotal += this.vaporizeColumn(world, x, z, outerBottom, outerTop, r2, rp2);
                        }
                        if (destroyedTotal >= DESTROY_BUDGET) {
                                this.nukeColumn = idx + 1; // continue with the next column next tick
                                return destroyedTotal;
                        }
                }
                this.nukeColumn = -1;
                return destroyedTotal;
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
                        if (state.isOf(Blocks.BEDROCK) || state.isOf(Blocks.BARRIER)) {
                                continue;
                        }
                        Block block = state.getBlock();
                        if (block instanceof CompressedTntBlock compressed) {
                                // chain reaction: prime it instead of vaporizing it
                                world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                                world.spawnEntity(new CompressedTntEntity(world, x + 0.5, y, z,
                                                compressed.tier, compressed.power(), state.get(CompressedTntBlock.FUSE) * 20));
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
                        // flowing, falling blocks fall, players can mine normally
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
                this.remainingTicks = Math.max(1, nbt.getInt("CompressedFuse"));
                super.setFuse(this.remainingTicks);
                this.nuking = nbt.getBoolean("CompressedNuking");
                this.nukeMaxRadius = nbt.contains("CompressedNukeMax", NbtElement.DOUBLE_TYPE)
                                ? nbt.getDouble("CompressedNukeMax") : radiusOf(this.tier);
                this.nukeShell = Math.max(0, nbt.getInt("CompressedNukeShell"));
                this.nukeColumn = nuking ? Math.max(-1, nbt.getInt("CompressedNukeCol")) : -1;
                this.center = new Vec3d(nbt.getDouble("CompressedCx"), nbt.getDouble("CompressedCy"), nbt.getDouble("CompressedCz"));
                if (this.center.equals(Vec3d.ZERO)) {
                        this.center = new Vec3d(this.getX(), this.getY() + 0.49, this.getZ());
                }
        }
}
