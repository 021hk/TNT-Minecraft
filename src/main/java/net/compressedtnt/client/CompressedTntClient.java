package net.compressedtnt.client;

import net.compressedtnt.CompressedTnt;
import net.compressedtnt.CompressedTntEntity;
import net.compressedtnt.FuseNet;
import net.compressedtnt.MaterialTntEntity;
import net.compressedtnt.TunnelTntEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.TntBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.TntEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;

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

                // right-click ANY placed TNT (vanilla or modded) -> open the fuse timer
                // screen HERE ON THE CLIENT. A client-side handler is required: if the
                // interaction were consumed on the server only, the vanilla client
                // would first run its own prediction - and returning SUCCESS inside a
                // common UseBlockCallback stops the client from ever sending the
                // interaction packet, so the server could not open the screen.
                // Opening it locally is instant and works exactly the same for every
                // TNT block. Sneak or use flint & steel for the vanilla behaviour.
                UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
                        if (!world.isClient || player.isSpectator() || player.isSneaking()) {
                                return ActionResult.PASS;
                        }
                        if (!(world.getBlockState(hitResult.getBlockPos()).getBlock() instanceof TntBlock)) {
                                return ActionResult.PASS;
                        }
                        ItemStack inHand = player.getStackInHand(hand);
                        if (inHand.isOf(Items.FLINT_AND_STEEL) || inHand.isOf(Items.FIRE_CHARGE)) {
                                return ActionResult.PASS; // normal ignition
                        }
                        MinecraftClient.getInstance().setScreen(new FuseScreen(hitResult.getBlockPos()));
                        return ActionResult.SUCCESS;
                });
        }
}
