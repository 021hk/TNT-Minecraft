package net.compressedtnt;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class MaterialTntBlockItem extends BlockItem {
        private final MaterialTntBlock block;

        public MaterialTntBlockItem(Block block, Item.Settings settings) {
                super(block, settings);
                this.block = (MaterialTntBlock) block;
        }

        @Override
        public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
                super.appendTooltip(stack, context, tooltip, type);
                tooltip.add(Text.translatable("compressedtnt.material_tooltip." + this.block.type.name().toLowerCase()).formatted(Formatting.GRAY));
                tooltip.add(Text.translatable("compressedtnt.fuse_hint").formatted(Formatting.AQUA));
        }
}
