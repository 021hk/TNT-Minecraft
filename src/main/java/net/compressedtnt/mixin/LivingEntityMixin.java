package net.compressedtnt.mixin;

import net.compressedtnt.CompressedTnt;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * TNT-infused armor: every piece with the {@code tnt_infused} component
 * absorbs 22.5% of explosion damage, so a full set (4 pieces) reduces it
 * by 90%.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
        @ModifyVariable(
                        method = "damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
                        at = @At("HEAD"),
                        ordinal = 0,
                        argsOnly = true
        )
        private float compressedtnt$blastResist(float amount, DamageSource source, float originalAmount) {
                if (source.isIn(DamageTypeTags.IS_EXPLOSION)) {
                        float reduction = 0.0f;
                        for (ItemStack armor : ((LivingEntity) (Object) this).getArmorItems()) {
                                if (armor.contains(CompressedTnt.TNT_INFUSED)) {
                                        reduction += 0.225f;
                                }
                        }
                        if (reduction > 0.0f) {
                                return amount * (1.0f - Math.min(reduction, 0.9f));
                        }
                }
                return amount;
        }
}
