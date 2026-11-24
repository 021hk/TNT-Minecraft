package net.compressedtnt.client;

import net.compressedtnt.CompressedTnt;
import net.compressedtnt.CompressedTntEntity;
import net.compressedtnt.FuseNet;
import net.compressedtnt.MaterialTntEntity;
import net.compressedtnt.TunnelTntEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.TntEntityRenderer;

public class CompressedTntClient implements ClientModInitializer {
        @Override
        public void onInitializeClient() {
                // reuse the vanilla TNT renderer (block model + fuse flashing) for our entities
                EntityRendererRegistry.register(CompressedTnt.PRIMED_COMPRESSED_TNT,
                                (EntityRendererFactory<CompressedTntEntity>) (EntityRendererFactory) ctx -> new TntEntityRenderer(ctx));
                EntityRendererRegistry.register(CompressedTnt.MATERIAL_TNT,
                                (EntityRendererFactory<MaterialTntEntity>) (EntityRendererFactory) ctx -> new TntEntityRenderer(ctx));
                EntityRendererRegistry.register(CompressedTnt.TUNNEL_TNT,
                                (EntityRendererFactory<TunnelTntEntity>) (EntityRendererFactory) ctx -> new TntEntityRenderer(ctx));
                EntityRendererRegistry.register(CompressedTnt.FRIENDLY_CREEPER_PINK, FriendlyCreeperRenderer::new);
                EntityRendererRegistry.register(CompressedTnt.FRIENDLY_CREEPER_BLUE, FriendlyCreeperRenderer::new);

                // server tells the client to open the fuse-timer screen for a TNT block
                ClientPlayNetworking.registerGlobalReceiver(FuseNet.OpenFuseScreen.ID, (payload, context) ->
                                context.client().execute(() -> {
                                        if (context.client().player != null) {
                                                context.client().setScreen(new FuseScreen(payload.pos()));
                                        }
                                }));
        }
}
