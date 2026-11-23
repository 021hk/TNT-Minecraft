package net.compressedtnt;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.TntBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * A compressed TNT block. Fusing with redstone / flint &amp; steel / fire /
 * explosion chains works exactly like vanilla TNT (all vanilla paths end up
 * spawning the entity through TntBlock.primeTnt or onDestroyedByExplosion,
 * which our mixin redirects), but spawns our own entity carrying the real
 * tier power with the vanilla 4-second fuse.
 *
 * Timer: hold the right mouse button on the placed block for about one
 * second, then type {@code /tntfuse <seconds>} in chat - the block starts
 * blinking immediately and explodes exactly on time. Without a timer it
 * behaves like vanilla TNT in every way.
 */
public class CompressedTntBlock extends TntBlock {
        public final int tier;

        public CompressedTntBlock(int tier, AbstractBlock.Settings settings) {
                super(settings);
                this.tier = tier;
        }

        @Override
        protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos,
                        PlayerEntity player, Hand hand, BlockHitResult hit) {
                if (!stack.isOf(Items.FLINT_AND_STEEL) && !stack.isOf(Items.FIRE_CHARGE) && !player.isSneaking()) {
                        if (world instanceof ServerWorld server && player instanceof ServerPlayerEntity serverPlayer) {
                                FuseHoldTracker.onTntUse(serverPlayer, server, pos);
                        }
                        return ItemActionResult.success(world.isClient);
                }
                return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
        }

        public float power() {
                return CompressedTnt.powerOf(this.tier);
        }
}
