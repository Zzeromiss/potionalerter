package com.example.potionalert;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class PotionAlertConfigScreen extends Screen {

    private final Screen parent;

    public PotionAlertConfigScreen(Screen parent) {
        super(Text.literal("Potion Alert"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int w = 220;
        int x = this.width / 2 - w / 2;
        int y = this.height / 4;

        // When to alert
        this.addDrawableChild(new SecondsSlider(x, y, w, 20));

        // Which sound (click to cycle, plays a preview)
        this.addDrawableChild(ButtonWidget.builder(soundLabel(), button -> {
            PotionAlertConfig.nextSound();
            button.setMessage(soundLabel());
            PotionAlertConfig.play(this.client);
        }).dimensions(x, y + 28, w, 20).build());

        // Volume
        this.addDrawableChild(new VolumeSlider(x, y + 56, w, 20));

        // Test
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Test Sound"),
                button -> PotionAlertConfig.play(this.client)).dimensions(x, y + 84, w, 20).build());

        // Done
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"),
                button -> this.close()).dimensions(x, y + 120, w, 20).build());
    }

    private static Text soundLabel() {
        return Text.literal("Sound: " + PotionAlertConfig.soundName() + " (click to change)");
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFFFF);
    }

    @Override
    public void close() {
        PotionAlertConfig.save();
        this.client.setScreen(this.parent);
    }

    // ---- sliders ----

    private static class SecondsSlider extends SliderWidget {
        SecondsSlider(int x, int y, int width, int height) {
            super(x, y, width, height, Text.empty(),
                    (PotionAlertConfig.alertSeconds - PotionAlertConfig.MIN_SECONDS)
                            / (double) (PotionAlertConfig.MAX_SECONDS - PotionAlertConfig.MIN_SECONDS));
            this.updateMessage();
        }

        private int seconds() {
            return (int) Math.round(PotionAlertConfig.MIN_SECONDS
                    + this.value * (PotionAlertConfig.MAX_SECONDS - PotionAlertConfig.MIN_SECONDS));
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Text.literal("Alert when: " + seconds() + "s left"));
        }

        @Override
        protected void applyValue() {
            PotionAlertConfig.alertSeconds = seconds();
        }
    }

    private static class VolumeSlider extends SliderWidget {
        VolumeSlider(int x, int y, int width, int height) {
            super(x, y, width, height, Text.empty(), PotionAlertConfig.volume);
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Text.literal("Volume: " + Math.round(this.value * 100) + "%"));
        }

        @Override
        protected void applyValue() {
            PotionAlertConfig.volume = (float) this.value;
        }
    }
}
