package net.compressedtnt;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * Removes blocks while dropping their normal loot - as if a player had
 * mined them with the correct tool (diamond pickaxe for stone, diamond axe
 * for wood, ...). Used by the material TNTs and the tunnel TNT so that
 * stone actually drops cobblestone, ores drop their items, and so on.
 */
public final class BlockDrops {
        private BlockDrops() {
        }

        /** Removes the block and drops its loot on the ground. */
        public static void breakWithLoot(ServerWorld world, BlockPos pos, BlockState state) {
                try {
                        LootContextParameterSet.Builder params = new LootContextParameterSet.Builder(world)
                                        .add(LootContextParameters.ORIGIN, Vec3d.ofCenter(pos))
                                        .add(LootContextParameters.TOOL, toolFor(state))
                                        .add(LootContextParameters.BLOCK_STATE, state);
                        state.getDroppedStacks(params).forEach(stack -> Block.dropStack(world, pos, stack));
                } catch (Exception e) {
                        // a broken / exotic loot table must never stop the world tick:
                        // drop the plain block item instead (best effort)
                        try {
                                Block.dropStack(world, pos, new ItemStack(state.getBlock()));
                        } catch (Exception ignored) {
                        }
                }
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
        }

        /** A diamond tool matching the block type (empty for tool-agnostic blocks). */
        public static ItemStack toolFor(BlockState state) {
                if (state.isIn(BlockTags.PICKAXE_MINEABLE)) {
                        return new ItemStack(Items.DIAMOND_PICKAXE);
                }
                if (state.isIn(BlockTags.AXE_MINEABLE)) {
                        return new ItemStack(Items.DIAMOND_AXE);
                }
                if (state.isIn(BlockTags.SHOVEL_MINEABLE)) {
                        return new ItemStack(Items.DIAMOND_SHOVEL);
                }
                if (state.isIn(BlockTags.HOE_MINEABLE)) {
                        return new ItemStack(Items.DIAMOND_HOE);
                }
                return ItemStack.EMPTY;
        }
}
