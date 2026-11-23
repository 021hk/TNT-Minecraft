package net.compressedtnt;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CompressedTntBlockItem extends BlockItem {
        private final int tier;

        public CompressedTntBlockItem(Block block, int tier, Item.Settings settings) {
                super(block, settings);
                this.tier = tier;
        }

        @Override
        public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
                super.appendTooltip(stack, context, tooltip, type);
                tooltip.add(Text.translatable("compressedtnt.power_tooltip", CompressedTnt.multiplierOf(this.tier)).formatted(Formatting.GRAY));
                Integer seconds = stack.get(CompressedTnt.TIMER_SECONDS);
                if (seconds != null && seconds > 0) {
                        tooltip.add(Text.translatable("compressedtnt.timer_tooltip", seconds).formatted(Formatting.GOLD));
                } else {
                        tooltip.add(Text.translatable("compressedtnt.timer_hint_key").formatted(Formatting.AQUA));
                }
                tooltip.add(Text.translatable("compressedtnt.fuse_hint").formatted(Formatting.DARK_GRAY));
        }

        @Override
        protected boolean postPlacement(BlockPos pos, World world, PlayerEntity player, ItemStack stack, BlockState state) {
                if (!world.isClient && world.getBlockEntity(pos) instanceof CompressedTntBlockEntity be) {
                        Integer seconds = stack.get(CompressedTnt.TIMER_SECONDS);
                        if (seconds != null && seconds > 0) {
                                // keyboard timer: the countdown starts the moment the block is placed
                                be.startTimer(seconds * 20);
                        }
                }
                return super.postPlacement(pos, world, player, stack, state);
        }
}
