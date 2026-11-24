package net.compressedtnt;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.TntBlock;

/**
 * A compressed TNT block. Fusing with redstone / flint &amp; steel / fire /
 * explosion chains works exactly like vanilla TNT (all vanilla paths end up
 * spawning the entity through TntBlock.primeTnt or onDestroyedByExplosion,
 * which our mixin redirects), but spawns our own entity carrying the real
 * tier power with the vanilla 4-second fuse.
 *
 * Timer: right-click the placed block - the fuse-timer screen opens
 * (see FuseScreen). Without a timer it behaves like vanilla TNT in every way.
 */
public class CompressedTntBlock extends TntBlock {
        public final int tier;

        public CompressedTntBlock(int tier, AbstractBlock.Settings settings) {
                super(settings);
                this.tier = tier;
        }

        public float power() {
                return CompressedTnt.powerOf(this.tier);
        }
}
