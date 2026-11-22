package net.compressedtnt.client;

import net.compressedtnt.CompressedTnt;
import net.compressedtnt.CompressedTntBlockItem;
import net.compressedtnt.CompressedTntEntity;
import net.compressedtnt.SetFusePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.TntEntityRenderer;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class CompressedTntClient implements ClientModInitializer {
        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public void onInitializeClient() {
                // reuse the vanilla TNT renderer (block model + fuse flashing) for our entity
                EntityRendererRegistry.register(CompressedTnt.PRIMED_COMPRESSED_TNT,
                                (EntityRendererFactory<CompressedTntEntity>)(EntityRendererFactory) ctx -> new TntEntityRenderer(ctx));

                // keyboard fuse timer: hold a compressed TNT item and press +/- (Shift = +/-10)
                KeyBinding fuseUp = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                                "key.compressedtnt.fuse_up", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_EQUAL,
                                "key.category.compressedtnt"));
                KeyBinding fuseDown = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                                "key.compressedtnt.fuse_down", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_MINUS,
                                "key.category.compressedtnt"));

                ClientTickEvents.END_CLIENT_TICK.register(client -> {
                        while (fuseUp.wasPressed()) {
                                sendFuseDelta(client, Screen.hasShiftDown() ? 10 : 1);
                        }
                        while (fuseDown.wasPressed()) {
                                sendFuseDelta(client, Screen.hasShiftDown() ? -10 : -1);
                        }
                });
        }

        private static void sendFuseDelta(MinecraftClient client, int delta) {
                ClientPlayerEntity player = client.player;
                if (player == null) {
                        return;
                }
                boolean holding = player.getMainHandStack().getItem() instanceof CompressedTntBlockItem
                                || player.getOffHandStack().getItem() instanceof CompressedTntBlockItem;
                if (holding) {
                        ClientPlayNetworking.send(new SetFusePayload(delta));
                }
        }
}
