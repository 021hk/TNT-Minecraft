package net.compressedtnt;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TntBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;
import java.util.Set;

/**
 * The three "material" TNTs: Dirt TNT, Stone TNT and Crop TNT.
 *
 * Crafting: 4 of the material + 4 gunpowder (gunpowder in the corners).
 * Explosion: NO damage at all (no hurt entities, no broken terrain), only the
 * explosion sound and particles, and every block of its own material inside a
 * 10x10x10 cube is removed - dropped on the ground as normal item drops.
 *
 * Ignition and the fuse timer behave exactly like vanilla TNT / the
 * compressed tiers (flint &amp; steel, redstone, explosion chains, or hold
 * right-click for 1 s and type /tntfuse &lt;seconds&gt;).
 */
public class MaterialTntBlock extends TntBlock {
        public enum Type {
                DIRT, STONE, CROP
        }

        /** the blocks each material TNT clears in its 10x10x10 area */
        public static final Map<Type, Set<Block>> TARGETS = Map.of(
                        Type.DIRT, Set.of(
                                        Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.COARSE_DIRT, Blocks.PODZOL,
                                        Blocks.MYCELIUM, Blocks.ROOTED_DIRT, Blocks.FARMLAND, Blocks.DIRT_PATH,
                                        Blocks.MUD),
                        Type.STONE, Set.of(
                                        Blocks.STONE, Blocks.GRANITE, Blocks.DIORITE, Blocks.ANDESITE,
                                        Blocks.TUFF, Blocks.DEEPSLATE, Blocks.COBBLESTONE,
                                        Blocks.COBBLED_DEEPSLATE, Blocks.CALCITE, Blocks.SMOOTH_BASALT),
                        Type.CROP, Set.of(
                                        Blocks.WHEAT, Blocks.CARROTS, Blocks.POTATOES, Blocks.BEETROOTS,
                                        Blocks.MELON, Blocks.PUMPKIN, Blocks.MELON_STEM, Blocks.PUMPKIN_STEM,
                                        Blocks.NETHER_WART, Blocks.COCOA, Blocks.SUGAR_CANE, Blocks.BAMBOO,
                                        Blocks.KELP, Blocks.CACTUS, Blocks.TORCHFLOWER_CROP, Blocks.PITCHER_CROP));

        public final Type type;

        public MaterialTntBlock(Type type, AbstractBlock.Settings settings) {
                super(settings);
                this.type = type;
        }

        @Override
        protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos,
                        PlayerEntity player, Hand hand, BlockHitResult hit) {
                if (!stack.isOf(Items.FLINT_AND_STEEL) && !stack.isOf(Items.FIRE_CHARGE) && !player.isSneaking()) {
                        if (world instanceof ServerWorld server && player instanceof ServerPlayerEntity serverPlayer) {
                                FuseHoldTracker.onTntUse(serverPlayer, server, pos);
                        }
                        return ItemActionResult.success(world.isClient);
                }
                return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
        }
}
