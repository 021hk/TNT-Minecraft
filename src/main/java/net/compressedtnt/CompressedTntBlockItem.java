package net.compressedtnt;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class CompressedTntBlockItem extends BlockItem {
        public static final int DEFAULT_FUSE_SECONDS = 4;
        public static final int MIN_FUSE_SECONDS = 1;
        public static final int MAX_FUSE_SECONDS = 300;
        private final int tier;

        public CompressedTntBlockItem(Block block, int tier, Item.Settings settings) {
                super(block, settings);
                this.tier = tier;
        }

        /** Fuse seconds stored in the item's custom data, default 4s ("normal"). */
        public static int getFuseSeconds(ItemStack stack) {
                NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
                if (component != null) {
                        NbtCompound nbt = component.copyNbt();
                        if (nbt.contains("TntFuse", NbtElement.INT_TYPE)) {
                                return MathHelper.clamp(nbt.getInt("TntFuse"), MIN_FUSE_SECONDS, MAX_FUSE_SECONDS);
                        }
                }
                return DEFAULT_FUSE_SECONDS;
        }

        public static void setFuseSeconds(ItemStack stack, int seconds) {
                NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
                NbtCompound nbt = component != null ? component.copyNbt() : new NbtCompound();
                nbt.putInt("TntFuse", MathHelper.clamp(seconds, MIN_FUSE_SECONDS, MAX_FUSE_SECONDS));
                stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        }

        @Override
        public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
                super.appendTooltip(stack, context, tooltip, type);
                tooltip.add(Text.translatable("compressedtnt.power_tooltip", CompressedTnt.multiplierOf(this.tier)));
                tooltip.add(Text.translatable("compressedtnt.fuse_item", getFuseSeconds(stack)));
                tooltip.add(Text.translatable("compressedtnt.fuse_hint"));
        }
}
