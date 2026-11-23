package net.compressedtnt.mixin;

import net.compressedtnt.CompressedTntBlock;
import net.compressedtnt.CompressedTntEntity;
import net.compressedtnt.FuseHoldTracker;
import net.compressedtnt.MaterialTntBlock;
import net.compressedtnt.MaterialTntEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.TntBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Every vanilla ignition path funnels into either
 * {@code TntBlock.primeTnt(World, BlockPos, LivingEntity)} (flint &amp; steel,
 * fire charge, redstone, projectile hits, the 2-arg overload, ...) or the
 * direct construction inside
 * {@code TntBlock.onDestroyedByExplosion(World, BlockPos, Explosion)}
 * (explosion chain reactions). We redirect both {@code new TntEntity(...)}
 * sites so that when the block being primed is one of our TNT blocks, the
 * matching custom entity is spawned instead.
 *
 * Additionally the right-click-hold fuse timer works on VANILLA TNT as well:
 * holding the use button on a vanilla TNT block for ~1 second shows the
 * {@code /tntfuse} prompt, and {@code /tntfuse} primes the vanilla TNT entity
 * with the requested fuse through {@link FuseHoldTracker#primeTntWithFuse}.
 */
@Mixin(TntBlock.class)
public abstract class TntBlockMixin {
        @Redirect(
                        method = "primeTnt(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/LivingEntity;)V",
                        at = @At(
                                        value = "NEW",
                                        target = "(Lnet/minecraft/world/World;DDDLnet/minecraft/entity/LivingEntity;)Lnet/minecraft/entity/TntEntity;"
                        )
        )
        private static TntEntity compressedtnt$spawnCustomPrime(World entityWorld, double x, double y, double z, @Nullable LivingEntity ctorIgniter, World world, BlockPos pos, LivingEntity igniter) {
                return compressedtnt$replace(entityWorld, x, y, z, ctorIgniter, world, pos);
        }

        @Redirect(
                        method = "onDestroyedByExplosion(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/explosion/Explosion;)V",
                        at = @At(
                                        value = "NEW",
                                        target = "(Lnet/minecraft/world/World;DDDLnet/minecraft/entity/LivingEntity;)Lnet/minecraft/entity/TntEntity;"
                        )
        )
        private static TntEntity compressedtnt$spawnCustomChain(World entityWorld, double x, double y, double z, @Nullable LivingEntity ctorIgniter, World world, BlockPos pos, Explosion explosion) {
                return compressedtnt$replace(entityWorld, x, y, z, ctorIgniter, world, pos);
        }

        private static TntEntity compressedtnt$replace(World entityWorld, double x, double y, double z, LivingEntity igniter, World world, BlockPos pos) {
                BlockState state = world.getBlockState(pos);
                if (state.getBlock() instanceof CompressedTntBlock block) {
                        return new CompressedTntEntity(entityWorld, x, y, z, block.tier, block.power(), 80);
                }
                if (state.getBlock() instanceof MaterialTntBlock material) {
                        return new MaterialTntEntity(entityWorld, x, y, z, material.type, 80);
                }
                return new TntEntity(entityWorld, x, y, z, igniter);
        }

        /**
         * Hold right-click ~1 s on a VANILLA TNT block to get the fuse prompt.
         * Sneaking keeps the vanilla behaviour (placing blocks against TNT);
         * flint &amp; steel / fire charge keep igniting it normally.
         */
        @Inject(
                        method = "onUseWithItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ItemActionResult;",
                        at = @At("HEAD"),
                        cancellable = true
        )
        private void compressedtnt$holdToSetFuse(ItemStack stack, BlockState state, World world, BlockPos pos,
                        PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ItemActionResult> cir) {
                if (!player.isSneaking() && !stack.isOf(Items.FLINT_AND_STEEL) && !stack.isOf(Items.FIRE_CHARGE)) {
                        if (world instanceof ServerWorld server && player instanceof ServerPlayerEntity serverPlayer) {
                                FuseHoldTracker.onTntUse(serverPlayer, server, pos);
                        }
                        cir.setReturnValue(ItemActionResult.success(world.isClient));
                }
        }
}
