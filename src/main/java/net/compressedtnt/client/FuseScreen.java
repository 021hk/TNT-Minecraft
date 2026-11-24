package net.compressedtnt.client;

import net.compressedtnt.FuseNet;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

/**
 * The fuse-timer screen opened by right-clicking ANY placed TNT
 * (modded or vanilla). Enter seconds (0-3600) and press start; the TNT is
 * primed exactly like vanilla but explodes after the chosen time.
 */
public class FuseScreen extends Screen {
        public static final int MAX_SECONDS = 3600;

        private final BlockPos pos;
        private TextFieldWidget secondsField;

        public FuseScreen(BlockPos pos) {
                super(Text.translatable("compressedtnt.gui.title"));
                this.pos = pos;
        }

        @Override
        protected void init() {
                int cx = this.width / 2;

                this.secondsField = new TextFieldWidget(this.textRenderer, cx - 60, 45, 120, 20,
                                Text.translatable("compressedtnt.gui.seconds"));
                this.secondsField.setText("120");
                this.secondsField.setTextPredicate(s -> s.matches("[0-9]{0,4}"));
                this.addDrawableChild(this.secondsField);

                this.addDrawableChild(ButtonWidget.builder(Text.literal("-60"), b -> this.bump(-60))
                                .dimensions(cx - 150, 75, 44, 20).build());
                this.addDrawableChild(ButtonWidget.builder(Text.literal("-10"), b -> this.bump(-10))
                                .dimensions(cx - 102, 75, 44, 20).build());
                this.addDrawableChild(ButtonWidget.builder(Text.literal("+10"), b -> this.bump(10))
                                .dimensions(cx + 58, 75, 44, 20).build());
                this.addDrawableChild(ButtonWidget.builder(Text.literal("+60"), b -> this.bump(60))
                                .dimensions(cx + 106, 75, 44, 20).build());

                this.addDrawableChild(ButtonWidget.builder(Text.translatable("compressedtnt.gui.no_timer"), b -> this.send(0))
                                .dimensions(cx - 100, 105, 200, 20).build());
                this.addDrawableChild(ButtonWidget.builder(Text.translatable("compressedtnt.gui.confirm"), b -> this.send(this.parse()))
                                .dimensions(cx - 100, 133, 98, 20).build());
                this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.cancel"), b -> this.close())
                                .dimensions(cx + 2, 133, 98, 20).build());
        }

        private void bump(int delta) {
                this.secondsField.setText(String.valueOf(MathHelper.clamp(this.parse() + delta, 0, MAX_SECONDS)));
        }

        private int parse() {
                try {
                        return MathHelper.clamp(Integer.parseInt(this.secondsField.getText().trim()), 0, MAX_SECONDS);
                } catch (NumberFormatException e) {
                        return 0;
                }
        }

        private void send(int seconds) {
                ClientPlayNetworking.send(new FuseNet.SetFuse(this.pos, seconds));
                this.close();
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
                super.render(context, mouseX, mouseY, delta);
                context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFF55);
                context.drawCenteredTextWithShadow(this.textRenderer,
                                Text.translatable("compressedtnt.gui.seconds"), this.width / 2, 33, 0xC8C8C8);
                context.drawCenteredTextWithShadow(this.textRenderer,
                                Text.translatable("compressedtnt.gui.hint"), this.width / 2, this.height - 28, 0xAAAAAA);
        }

        @Override
        public boolean shouldPause() {
                return false;
        }
}
