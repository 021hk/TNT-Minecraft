package net.compressedtnt;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Lives inside every placed compressed TNT block and runs the fuse timer.
 *
 * The timer can be armed two ways:
 *  - the item had a timer set with the G key (keyboard) -> counting starts
 *    the moment the block is placed;
 *  - right-clicking the placed block with a Clock (each click +1 s,
 *    Shift + click -1 s; at 1 s Shift cancels).
 *
 * While armed it puffs smoke so the player can see it is live, and when the
 * countdown reaches 0 the block primes and detonates immediately. If no timer
 * was set the block simply behaves like vanilla TNT (needs redstone / fire /
 * flint & steel, then a normal 4 second fuse).
 */
public class CompressedTntBlockEntity extends BlockEntity {
        public static final int OFF = -1;

        private int timerTicks = OFF;

        public CompressedTntBlockEntity(BlockPos pos, BlockState state) {
                super(CompressedTnt.BLOCK_ENTITY_TYPE, pos, state);
        }

        public boolean isArmed() {
                return this.timerTicks >= 0;
        }

        public int getIgniteRemaining() {
                return this.timerTicks;
        }

        /** seconds left, rounded up (for messages) */
        public int getTimerSeconds() {
                return (this.timerTicks + 19) / 20;
        }

        public void startTimer(int ticks) {
                this.timerTicks = Math.max(1, ticks);
                this.markDirty();
        }

        public void cancelTimer() {
                this.timerTicks = OFF;
                this.markDirty();
        }

        public static void tick(World world, BlockPos pos, BlockState state, CompressedTntBlockEntity be) {
                if (world.isClient || !be.isArmed()) {
                        return;
                }
                be.timerTicks--;
                be.markDirty();
                if (be.timerTicks % 10 == 0 && world instanceof ServerWorld server && be.timerTicks > 0) {
                        server.spawnParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 3, 0.15, 0.15, 0.15, 0.004);
                }
                if (be.timerTicks <= 0) {
                        be.timerTicks = OFF;
                        if (state.getBlock() instanceof CompressedTntBlock block) {
                                world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                                world.spawnEntity(new CompressedTntEntity(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                                                block.tier, block.power(), 1));
                        }
                }
        }

        @Override
        protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
                super.writeNbt(nbt, registries);
                nbt.putInt("IgniteRemaining", this.timerTicks);
        }

        @Override
        public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
                super.readNbt(nbt, registries);
                this.timerTicks = nbt.contains("IgniteRemaining", NbtElement.INT_TYPE) ? nbt.getInt("IgniteRemaining") : OFF;
        }
}
