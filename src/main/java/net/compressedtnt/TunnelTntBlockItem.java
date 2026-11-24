package net.compressedtnt;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class TunnelTntBlockItem extends BlockItem {
        private final TunnelTntBlock block;

        public TunnelTntBlockItem(Block block, Item.Settings settings) {
                super(block, settings);
                this.block = (TunnelTntBlock) block;
        }

        @Override
        public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
                super.appendTooltip(stack, context, tooltip, type);
                String key = this.block.mode == TunnelTntBlock.Mode.HORIZONTAL
                                ? "compressedtnt.tunnel_tooltip.h" : "compressedtnt.tunnel_tooltip.v";
                tooltip.add(Text.translatable(key).formatted(Formatting.GRAY));
                tooltip.add(Text.translatable("compressedtnt.fuse_hint").formatted(Formatting.AQUA));
        }
}
