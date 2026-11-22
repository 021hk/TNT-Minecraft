package net.compressedtnt;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.TntBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
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
 * tier power. The fuse timer (1-300 seconds, default 4) can be set:
 * - in advance: hold the item and press the +/- keybindings (Shift = +/-10s)
 * - in world: right-click the placed block with a Clock (+1s, Shift -1s)
 */
public class CompressedTntBlock extends TntBlock {
        public static final IntProperty FUSE = IntProperty.of("fuse", 1, CompressedTntBlockItem.MAX_FUSE_SECONDS);
        public final int tier;

        public CompressedTntBlock(int tier, AbstractBlock.Settings settings) {
                super(settings);
                this.tier = tier;
                this.setDefaultState(this.stateManager.getDefaultState().with(FUSE, 4));
        }

        @Override
        protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
                super.appendProperties(builder); // keeps vanilla TntBlock.UNSTABLE
                builder.add(FUSE);
        }

        @Override
        public BlockState getPlacementState(ItemPlacementContext ctx) {
                return this.getDefaultState().with(FUSE, CompressedTntBlockItem.getFuseSeconds(ctx.getStack()));
        }

        @Override
        protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
                if (stack.isOf(Items.CLOCK)) {
                        if (!world.isClient) {
                                int current = state.get(FUSE);
                                int next = player.isSneaking()
                                                ? Math.max(CompressedTntBlockItem.MIN_FUSE_SECONDS, current - 1)
                                                : Math.min(CompressedTntBlockItem.MAX_FUSE_SECONDS, current + 1);
                                if (next != current) {
                                        world.setBlockState(pos, state.with(FUSE, next), Block.NOTIFY_ALL);
                                }
                                player.sendMessage(Text.translatable("compressedtnt.fuse_set", next), true);
                        }
                        return ItemActionResult.success(world.isClient);
                }
                return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
        }

        public float power() {
                return CompressedTnt.powerOf(this.tier);
        }
}
