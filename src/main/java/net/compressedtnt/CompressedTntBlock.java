package net.compressedtnt;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.TntBlock;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
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
 * tier power.
 *
 * Timer systems:
 *  - Clock right-click: starts / adjusts the live countdown (+1 s per click,
 *    Shift: -1 s, at 1 s Shift cancels; up to 3600 s).
 *  - The item's keyboard timer (G key) starts counting automatically when the
 *    block is placed (see CompressedTntBlockItem.postPlacement).
 *  - No timer set: behaves like vanilla TNT (normal 4 s fuse after ignition).
 */
public class CompressedTntBlock extends TntBlock implements BlockEntityProvider {
        public static final IntProperty FUSE = IntProperty.of("fuse", 1, 30);
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
                return this.getDefaultState().with(FUSE, 4);
        }

        @Override
        protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
                if (stack.isOf(Items.CLOCK)) {
                        if (!world.isClient && world.getBlockEntity(pos) instanceof CompressedTntBlockEntity be) {
                                if (player.isSneaking()) {
                                        int seconds = be.isArmed() ? be.getTimerSeconds() - 1 : 0;
                                        if (seconds < 1) {
                                                be.cancelTimer();
                                                player.sendMessage(Text.translatable("compressedtnt.timer_cancelled"), true);
                                        } else {
                                                be.startTimer(seconds * 20);
                                                player.sendMessage(Text.translatable("compressedtnt.timer_set", seconds), true);
                                        }
                                } else {
                                        int seconds = be.isArmed() ? Math.min(3600, be.getTimerSeconds() + 1) : 5;
                                        be.startTimer(seconds * 20);
                                        player.sendMessage(Text.translatable("compressedtnt.timer_set", seconds), true);
                                }
                        }
                        return ItemActionResult.success(world.isClient);
                }
                return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
        }

        @Override
        public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
                return new CompressedTntBlockEntity(pos, state);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
                if (type != CompressedTnt.BLOCK_ENTITY_TYPE || world.isClient) {
                        return null;
                }
                return (BlockEntityTicker<T>) (BlockEntityTicker<CompressedTntBlockEntity>) CompressedTntBlockEntity::tick;
        }

        public float power() {
                return CompressedTnt.powerOf(this.tier);
        }
}
