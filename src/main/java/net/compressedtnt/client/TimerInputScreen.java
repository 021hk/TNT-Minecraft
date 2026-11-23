package net.compressedtnt.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.compressedtnt.SetTimerPayload;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/**
 * Small screen opened with the G key while holding a compressed TNT item.
 * Type the fuse in seconds (1-3600), press Enter / "Set", then place the
 * block: the countdown starts immediately and it explodes exactly on time.
 */
public class TimerInputScreen extends Screen {
        private TextFieldWidget field;

        public TimerInputScreen() {
                super(Text.translatable("compressedtnt.timer_screen_title"));
        }

        @Override
        protected void init() {
                this.field = new TextFieldWidget(this.textRenderer, this.width / 2 - 60, this.height / 2 - 15, 120, 20, this.title);
                this.field.setMaxLength(4);
                this.field.setTextPredicate(s -> s.matches("\\d{0,4}"));
                this.addSelectableChild(this.field);
                this.setInitialFocus(this.field);
                this.addDrawableChild(ButtonWidget.builder(Text.translatable("compressedtnt.timer_ok"), b -> this.confirm())
                                .dimensions(this.width / 2 - 62, this.height / 2 + 15, 60, 20).build());
                this.addDrawableChild(ButtonWidget.builder(Text.translatable("compressedtnt.timer_cancel"), b -> this.close())
                                .dimensions(this.width / 2 + 2, this.height / 2 + 15, 60, 20).build());
        }

        private void confirm() {
                String s = this.field.getText().trim();
                int seconds = 0;
                if (!s.isEmpty()) {
                        try {
                                seconds = Integer.parseInt(s);
                        } catch (NumberFormatException e) {
                                seconds = 0;
                        }
                }
                seconds = Math.max(0, Math.min(3600, seconds));
                ClientPlayNetworking.send(new SetTimerPayload(seconds));
                this.close();
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
                this.renderBackground(context, mouseX, mouseY, delta);
                context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.height / 2 - 40, 0xFFFFFF);
                super.render(context, mouseX, mouseY, delta);
                this.field.render(context, mouseX, mouseY, delta);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                        this.confirm();
                        return true;
                }
                return super.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean shouldPause() {
                return false;
        }
}
