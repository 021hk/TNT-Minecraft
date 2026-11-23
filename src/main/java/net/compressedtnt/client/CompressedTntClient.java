package net.compressedtnt.client;

import net.compressedtnt.CompressedTnt;
import net.compressedtnt.CompressedTntBlockItem;
import net.compressedtnt.CompressedTntEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.TntEntityRenderer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public class CompressedTntClient implements ClientModInitializer {
        public static KeyBinding timerKey;

        @Override
        public void onInitializeClient() {
                // reuse the vanilla TNT renderer (block model + fuse flashing) for our entity
                EntityRendererRegistry.register(CompressedTnt.PRIMED_COMPRESSED_TNT,
                                (EntityRendererFactory<CompressedTntEntity>)(EntityRendererFactory) ctx -> new TntEntityRenderer(ctx));

                // G while holding a compressed TNT item opens the timer input screen
                timerKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                                "key.compressedtnt.timer", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, "category.compressedtnt"));

                ClientTickEvents.END_CLIENT_TICK.register(client -> {
                        while (timerKey.wasPressed()) {
                                if (client.player == null) {
                                        continue;
                                }
                                ItemStack main = client.player.getMainHandStack();
                                ItemStack off = client.player.getOffHandStack();
                                if (main.getItem() instanceof CompressedTntBlockItem || off.getItem() instanceof CompressedTntBlockItem) {
                                        client.setScreen(new TimerInputScreen());
                                }
                        }
                });
        }
}
