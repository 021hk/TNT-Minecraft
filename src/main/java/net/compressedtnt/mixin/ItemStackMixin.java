package net.compressedtnt.mixin;

import net.compressedtnt.CompressedTnt;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/** Shows the "TNT-Infused" tooltip line on every item with the component. */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
        @Inject(
                        method = "getTooltip(Lnet/minecraft/item/Item$TooltipContext;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/tooltip/TooltipType;)Ljava/util/List;",
                        at = @At("RETURN")
        )
        private void compressedtnt$infusedTooltip(Item.TooltipContext context, @Nullable PlayerEntity player,
                        TooltipType type, CallbackInfoReturnable<List<Text>> cir) {
                if (((ItemStack) (Object) this).contains(CompressedTnt.TNT_INFUSED)) {
                        cir.getReturnValue().add(Text.translatable("compressedtnt.infused_line1").formatted(Formatting.GOLD));
                        cir.getReturnValue().add(Text.translatable("compressedtnt.infused_line2").formatted(Formatting.GRAY));
                }
        }
}
