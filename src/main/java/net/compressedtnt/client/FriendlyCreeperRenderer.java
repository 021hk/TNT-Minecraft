package net.compressedtnt.client;

import net.compressedtnt.CompressedTnt;
import net.compressedtnt.FriendlyCreeperEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.CreeperEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

/**
 * Renders the friendly creepers with the vanilla creeper model (plus a
 * bow-tie painted into the texture). Babies are rendered half size, and the
 * model swells up while the fuse burns (like a vanilla creeper about to
 * explode).
 */
public class FriendlyCreeperRenderer extends MobEntityRenderer<FriendlyCreeperEntity, CreeperEntityModel<FriendlyCreeperEntity>> {
        private static final Identifier PINK =
                        Identifier.of(CompressedTnt.MOD_ID, "textures/entity/friendly_creeper_pink.png");
        private static final Identifier BLUE =
                        Identifier.of(CompressedTnt.MOD_ID, "textures/entity/friendly_creeper_blue.png");

        public FriendlyCreeperRenderer(EntityRendererFactory.Context context) {
                super(context, new CreeperEntityModel<>(context.getPart(EntityModelLayers.CREEPER)), 0.5f);
        }

        @Override
        public Identifier getTexture(FriendlyCreeperEntity entity) {
                return entity.variant() == FriendlyCreeperEntity.Variant.PINK ? PINK : BLUE;
        }

        @Override
        protected void scale(FriendlyCreeperEntity entity, MatrixStack matrices, float tickDelta) {
                float f = entity.isBaby() ? 0.5f : 1.0f;
                if (entity.isPrimed()) {
                        f *= 1.0f + 0.25f * entity.getPrimedProgress(tickDelta);
                }
                matrices.scale(f, f, f);
        }
}
