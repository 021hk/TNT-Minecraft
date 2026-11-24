package net.compressedtnt;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TntBlock;
import net.minecraft.registry.tag.BlockTags;

import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * The four "material" TNTs: Dirt TNT, Stone TNT, Crop TNT and Wood TNT.
 *
 * Crafting: 4 of the material + 4 gunpowder (material in the corners,
 * gunpowder at the sides).
 * Explosion: NO damage at all (no hurt entities, no broken terrain), only the
 * explosion sound and particles, and every block of its own material inside a
 * 10x10x10 cube is removed - dropped on the ground as normal item drops.
 *
 * Ignition and the fuse timer behave exactly like vanilla TNT / the
 * compressed tiers (flint &amp; steel, redstone, explosion chains, or
 * right-click to open the fuse-timer screen).
 */
public class MaterialTntBlock extends TntBlock {
        public enum Type {
                DIRT, STONE, CROP, WOOD
        }

        /** the blocks each material TNT clears in its 10x10x10 area */
        public static final Map<Type, Predicate<BlockState>> TARGETS = Map.of(
                        Type.DIRT, of(Set.of(
                                        Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.COARSE_DIRT, Blocks.PODZOL,
                                        Blocks.MYCELIUM, Blocks.ROOTED_DIRT, Blocks.FARMLAND, Blocks.DIRT_PATH,
                                        Blocks.MUD)),
                        Type.STONE, of(Set.of(
                                        Blocks.STONE, Blocks.GRANITE, Blocks.DIORITE, Blocks.ANDESITE,
                                        Blocks.TUFF, Blocks.DEEPSLATE, Blocks.COBBLESTONE,
                                        Blocks.COBBLED_DEEPSLATE, Blocks.CALCITE, Blocks.SMOOTH_BASALT)),
                        Type.CROP, of(Set.of(
                                        Blocks.WHEAT, Blocks.CARROTS, Blocks.POTATOES, Blocks.BEETROOTS,
                                        Blocks.MELON, Blocks.PUMPKIN, Blocks.MELON_STEM, Blocks.PUMPKIN_STEM,
                                        Blocks.NETHER_WART, Blocks.COCOA, Blocks.SUGAR_CANE, Blocks.BAMBOO,
                                        Blocks.KELP, Blocks.CACTUS, Blocks.TORCHFLOWER_CROP, Blocks.PITCHER_CROP)),
                        // EVERY log and plank of every wood type (oak, birch, spruce, ...)
                        Type.WOOD, state -> state.isIn(BlockTags.LOGS) || state.isIn(BlockTags.PLANKS));

        private static Predicate<BlockState> of(Set<Block> blocks) {
                return state -> blocks.contains(state.getBlock());
        }

        public final Type type;

        public MaterialTntBlock(Type type, AbstractBlock.Settings settings) {
                super(settings);
                this.type = type;
        }
}
