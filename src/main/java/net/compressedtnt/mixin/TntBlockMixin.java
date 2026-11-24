package net.compressedtnt.mixin;

import net.compressedtnt.CompressedTntBlock;
import net.compressedtnt.CompressedTntEntity;
import net.compressedtnt.MaterialTntBlock;
import net.compressedtnt.MaterialTntEntity;
import net.compressedtnt.TunnelTntBlock;
import net.compressedtnt.TunnelTntEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.TntBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

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
 * The right-click fuse-timer screen works on VANILLA TNT as well - the
 * UseBlockCallback registered in {@link net.compressedtnt.CompressedTnt}
 * intercepts the interaction for every TntBlock, vanilla or modded.
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
                if (state.getBlock() instanceof TunnelTntBlock tunnel) {
                        boolean vertical = tunnel.mode == TunnelTntBlock.Mode.VERTICAL;
                        Direction direction = TunnelTntBlock.facing(state, tunnel.mode);
                        return new TunnelTntEntity(entityWorld, x, y, z, vertical, direction, 80);
                }
                return new TntEntity(entityWorld, x, y, z, igniter);
        }
}
