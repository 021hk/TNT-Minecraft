package net.compressedtnt;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.TntBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldView;

/**
 * The two tunnel TNT blocks:
 * <ul>
 *  <li>{@link Horizontal}: 3 vanilla TNT in the middle row - bores a straight
 *      12-block tunnel, 1 block wide x 2 high (walkable), in the direction the
 *      player faced when placing it;</li>
 *  <li>{@link Vertical}: 3 vanilla TNT in the middle column - bores a straight
 *      12-block 1x1 shaft up or down (looking up while placing = up, otherwise
 *      down).</li>
 * </ul>
 * The face with the white arrow is ALWAYS the face that is bored through.
 */
public abstract class TunnelTntBlock extends TntBlock {
        public enum Mode {
                HORIZONTAL, VERTICAL
        }

        public static final DirectionProperty V_FACING = DirectionProperty.of("facing", Direction.UP, Direction.DOWN);

        public final Mode mode;

        protected TunnelTntBlock(Mode mode, AbstractBlock.Settings settings) {
                super(settings);
                this.mode = mode;
        }

        /** The direction the tunnel is bored in. */
        public Direction facing(BlockState state) {
                return facing(state, this.mode);
        }

        public static Direction facing(BlockState state, Mode mode) {
                return mode == Mode.HORIZONTAL ? state.get(HorizontalFacingBlock.FACING) : state.get(V_FACING);
        }

        /** horizontal tunnel TNT: facing = the 4 horizontal directions */
        public static class Horizontal extends TunnelTntBlock {
                public Horizontal(AbstractBlock.Settings settings) {
                        super(Mode.HORIZONTAL, settings);
                }

                @Override
                protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
                        builder.add(TntBlock.UNSTABLE);
                        builder.add(HorizontalFacingBlock.FACING);
                }

                @Override
                public BlockState getPlacementState(ItemPlacementContext ctx) {
                        return this.getDefaultState().with(HorizontalFacingBlock.FACING, ctx.getPlayerLookDirection());
                }
        }

        /** vertical tunnel TNT: facing = up or down */
        public static class Vertical extends TunnelTntBlock {
                public Vertical(AbstractBlock.Settings settings) {
                        super(Mode.VERTICAL, settings);
                }

                @Override
                protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
                        builder.add(TntBlock.UNSTABLE);
                        builder.add(V_FACING);
                }

                @Override
                public BlockState getPlacementState(ItemPlacementContext ctx) {
                        Direction dir = ctx.getPlayer() != null && ctx.getPlayer().getPitch() > 0.0f
                                        ? Direction.DOWN : Direction.UP;
                        return this.getDefaultState().with(V_FACING, dir);
                }
        }
}
