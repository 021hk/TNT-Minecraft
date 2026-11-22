package net.compressedtnt;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

public class CompressedTntBlockItem extends BlockItem {
	private final int tier;

	public CompressedTntBlockItem(Block block, int tier, Item.Settings settings) {
		super(block, settings);
		this.tier = tier;
	}

	@Override
	public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
		super.appendTooltip(stack, context, tooltip, type);
		tooltip.add(Text.translatable("compressedtnt.power_tooltip", CompressedTnt.multiplierOf(this.tier)));
		tooltip.add(Text.translatable("compressedtnt.fuse_hint"));
	}
}
