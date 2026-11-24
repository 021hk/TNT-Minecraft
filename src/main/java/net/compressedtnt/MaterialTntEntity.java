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
import net.minecraft.world.World;

/**
 * Primed Dirt/Stone/Crop TNT. Blinking and physics are exactly like vanilla
 * primed TNT, but when the fuse runs out there is no real explosion:
 *
 * <ul>
 *  <li>no entity is hurt, no block other than the material itself is touched;</li>
 *  <li>the explosion sound + particles play;</li>
 *  <li>every block of the material inside a 10x10x10 cube around the TNT is
 *      removed and dropped on the ground as normal item drops (a harvest /
 *      quarry without any destruction).</li>
 * </ul>
 */
public class MaterialTntEntity extends TntEntity {
        /** half width of the clear cube: x/z in [c-5, c+4], y in [c-4, c+5] */
        public static final int RADIUS = 5;

        private int remainingTicks = 80;
        private MaterialTntBlock.Type type = MaterialTntBlock.Type.DIRT;

        public MaterialTntEntity(EntityType<? extends MaterialTntEntity> entityType, World world) {
                super(entityType, world);
        }

        public MaterialTntEntity(World world, double x, double y, double z, MaterialTntBlock.Type type, int fuseTicks) {
                super(CompressedTnt.MATERIAL_TNT, world);
                this.setPosition(x, y, z);
                this.type = type;
                this.remainingTicks = Math.max(1, fuseTicks);
                super.setFuse(this.remainingTicks);
        }

        @Override
        public void tick() {
                World world = this.getWorld();
                if (world.isClient) {
                        // the fuse value is synced through the vanilla data tracker,
                        // so long custom fuses blink correctly on the client too
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
                        this.clearArea((ServerWorld) world);
                }
        }

        private void clearArea(ServerWorld server) {
                BlockPos center = BlockPos.ofFloored(this.getX(), this.getY(), this.getZ());
                server.playSound(null, center, SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.BLOCKS, 2.5f, 1.0f);
                server.spawnParticles(ParticleTypes.EXPLOSION_EMITTER,
                                center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5, 4, 1.0, 1.0, 1.0, 0.0);
                server.spawnParticles(ParticleTypes.FLASH,
                                center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5, 2, 0.0, 0.0, 0.0, 0.0);

                var targets = MaterialTntBlock.TARGETS.get(this.type);
                int cleared = 0;
                for (int x = center.getX() - RADIUS; x <= center.getX() + RADIUS - 1; x++) {
                        for (int y = center.getY() - RADIUS + 1; y <= center.getY() + RADIUS; y++) {
                                for (int z = center.getZ() - RADIUS; z <= center.getZ() + RADIUS - 1; z++) {
                                        BlockPos pos = new BlockPos(x, y, z);
                                        BlockState state;
                                        try {
                                                state = server.getBlockState(pos);
                                        } catch (Exception e) {
                                                continue;
                                        }
                                        Block block = state.getBlock();
                                        if (state.isOf(Blocks.BEDROCK) || !targets.test(state)) {
                                                continue;
                                        }
                                        // drop the block's normal loot (items) on the ground, as if
                                        // mined with the right tool - stone drops cobblestone etc.
                                        try {
                                                BlockDrops.breakWithLoot(server, pos, state);
                                        } catch (Exception e) {
                                                CompressedTnt.LOGGER.warn("Material TNT: could not clear {} at {}: {}",
                                                                block, pos.toShortString(), e.toString());
                                                try {
                                                        server.setBlockState(pos, Blocks.AIR.getDefaultState());
                                                } catch (Exception ignored) {
                                                }
                                        }
                                        cleared++;
                                }
                        }
                }
                CompressedTnt.LOGGER.info("Material TNT ({}) cleared {} blocks at {}", this.type, cleared, center.toShortString());
        }

        @Override
        public void writeCustomDataToNbt(NbtCompound nbt) {
                super.writeCustomDataToNbt(nbt);
                nbt.putInt("CompressedFuse", this.remainingTicks);
                nbt.putString("MaterialType", this.type.name());
        }

        @Override
        public void readCustomDataFromNbt(NbtCompound nbt) {
                super.readCustomDataFromNbt(nbt);
                this.remainingTicks = Math.max(1, nbt.contains("CompressedFuse", NbtElement.INT_TYPE)
                                ? nbt.getInt("CompressedFuse") : 80);
                super.setFuse(this.remainingTicks);
                try {
                        this.type = MaterialTntBlock.Type.valueOf(nbt.getString("MaterialType"));
                } catch (IllegalArgumentException e) {
                        this.type = MaterialTntBlock.Type.DIRT;
                }
        }
}
