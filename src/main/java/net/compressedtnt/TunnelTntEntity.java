package net.compressedtnt;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.TntEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

/**
 * Primed tunnel TNT: when the fuse runs out it does NOT explode as a sphere.
 * Instead it bores a straight tunnel in the direction shown by the block's
 * arrow:
 * <ul>
 *  <li>horizontal: 12 blocks long, 1 block wide x 2 high (a walkable tunnel);</li>
 *  <li>vertical: 12 blocks long, 1x1 shaft.</li>
 * </ul>
 * No entity is hurt. Every destroyed block drops its normal loot on the
 * ground (as if mined with the right tool). Other TNT in the path is chained
 * (primed), bedrock / obsidian / barriers are left alone.
 */
public class TunnelTntEntity extends TntEntity {
        public static final int LENGTH = 12;

        private int remainingTicks = 80;
        private boolean vertical = false;
        private Direction direction = Direction.NORTH;

        public TunnelTntEntity(EntityType<? extends TunnelTntEntity> type, World world) {
                super(type, world);
        }

        public TunnelTntEntity(World world, double x, double y, double z, boolean vertical, Direction direction, int fuseTicks) {
                super(CompressedTnt.TUNNEL_TNT, world);
                this.setPosition(x, y, z);
                this.vertical = vertical;
                this.direction = normalize(vertical, direction);
                this.remainingTicks = Math.max(1, fuseTicks);
                super.setFuse(this.remainingTicks);
        }

        /** vertical tunnel TNT must always bore UP or DOWN, never sideways */
        private static Direction normalize(boolean vertical, Direction direction) {
                if (vertical && direction.getAxis().isHorizontal()) {
                        return Direction.DOWN;
                }
                if (!vertical && !direction.getAxis().isHorizontal()) {
                        return Direction.NORTH;
                }
                return direction;
        }

        @Override
        public void tick() {
                World world = this.getWorld();
                if (world.isClient) {
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
                        this.discard();
                        this.bore((ServerWorld) world);
                }
        }

        private void bore(ServerWorld server) {
                BlockPos center = BlockPos.ofFloored(this.getX(), this.getY(), this.getZ());
                Direction dir = normalize(this.vertical, this.direction);
                int cleared = 0;

                for (int i = 0; i < LENGTH; i++) {
                        BlockPos base = center.offset(dir, i);
                        int levels = this.vertical ? 1 : 2; // 1x1 shaft, or 1 wide x 2 high
                        for (int h = 0; h < levels; h++) {
                                BlockPos pos = this.vertical ? base : base.up(h);
                                try {
                                        cleared += boreOne(server, pos);
                                } catch (Exception e) {
                                        // one weird block must never kill the world tick -
                                        // log it and force the block out of the way
                                        CompressedTnt.LOGGER.warn("Tunnel TNT: could not bore {} at {}: {}",
                                                        server.getBlockState(pos).getBlock(), pos.toShortString(), e.toString());
                                        try {
                                                server.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                                        } catch (Exception ignored) {
                                        }
                                }
                        }
                }

                BlockPos end = center.offset(dir, LENGTH - 1);
                server.playSound(null, center, SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.BLOCKS, 2.5f, 0.8f);
                server.playSound(null, end, SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.BLOCKS, 2.5f, 0.7f);
                for (int i = 0; i < LENGTH; i += 2) {
                        BlockPos p = center.offset(dir, i);
                        server.spawnParticles(ParticleTypes.EXPLOSION, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 1, 0.1, 0.1, 0.1, 0.0);
                        server.spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 1, 0.0, 0.05, 0.0, 0.01);
                }
                CompressedTnt.LOGGER.info("Tunnel TNT bored {} blocks {} at {} (vertical={})", cleared, dir, center.toShortString(), this.vertical);
        }

        /** Bores one block position; returns 1 when a block was removed. */
        private int boreOne(ServerWorld server, BlockPos pos) {
                BlockState state = server.getBlockState(pos);
                if (state.isAir() || state.isOf(Blocks.BEDROCK) || state.isOf(Blocks.BARRIER)
                                || state.isOf(Blocks.OBSIDIAN) || state.isOf(Blocks.CRYING_OBSIDIAN)
                                || state.isOf(Blocks.REINFORCED_DEEPSLATE)) {
                        return 0;
                }
                Block block = state.getBlock();
                if (block instanceof CompressedTntBlock compressed) {
                        server.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                        server.spawnEntity(new CompressedTntEntity(server, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                                        compressed.tier, compressed.power(), 80));
                } else if (block instanceof MaterialTntBlock material) {
                        server.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                        server.spawnEntity(new MaterialTntEntity(server, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                                        material.type, 80));
                } else if (block instanceof TunnelTntBlock) {
                        server.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                        // chained tunnel TNT continues the same tunnel
                        server.spawnEntity(new TunnelTntEntity(server, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                                        this.vertical, normalize(this.vertical, this.direction), 40));
                } else if (state.isOf(Blocks.TNT)) {
                        server.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                        server.spawnEntity(new TntEntity(server, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, null));
                } else {
                        // drop the block's loot on the ground (right tool simulation)
                        BlockDrops.breakWithLoot(server, pos, state);
                }
                return 1;
        }

        @Override
        public void writeCustomDataToNbt(NbtCompound nbt) {
                super.writeCustomDataToNbt(nbt);
                nbt.putInt("CompressedFuse", this.remainingTicks);
                nbt.putBoolean("TunnelVertical", this.vertical);
                nbt.putString("TunnelDirection", this.direction.name());
        }

        @Override
        public void readCustomDataFromNbt(NbtCompound nbt) {
                super.readCustomDataFromNbt(nbt);
                this.remainingTicks = Math.max(1, nbt.contains("CompressedFuse", NbtElement.INT_TYPE)
                                ? nbt.getInt("CompressedFuse") : 80);
                super.setFuse(this.remainingTicks);
                this.vertical = nbt.getBoolean("TunnelVertical");
                try {
                        this.direction = normalize(this.vertical, Direction.valueOf(nbt.getString("TunnelDirection")));
                } catch (IllegalArgumentException e) {
                        this.direction = this.vertical ? Direction.DOWN : Direction.NORTH;
                }
        }
}
