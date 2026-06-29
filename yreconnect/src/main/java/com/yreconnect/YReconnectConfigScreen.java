package com.yreconnect;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.*;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class YReconnectConfigScreen extends Screen {

    private final Screen parent;

    // Live-edit copies of config values (committed on Save)
    private boolean enabled;
    private double  triggerY;
    private boolean triggerAbove;
    private int     reconnectDelayTicks;

    // Widget references so we can read them on save
    private CyclingButtonWidget<Boolean> enabledBtn;
    private CyclingButtonWidget<Boolean> directionBtn;
    private TextFieldWidget triggerYField;
    private SliderWidget delaySlider;
    private TextWidget delayLabel;

    public YReconnectConfigScreen(Screen parent) {
        super(Text.literal("YReconnect Config"));
        this.parent = parent;

        YReconnectConfig cfg = YReconnectConfig.get();
        this.enabled             = cfg.enabled;
        this.triggerY            = cfg.triggerY;
        this.triggerAbove        = cfg.triggerAbove;
        this.reconnectDelayTicks = cfg.reconnectDelayTicks;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int startY = 60;
        int rowH = 28;
        int btnW = 200;

        // ── Enabled toggle ──────────────────────────────────────────────────────
        enabledBtn = CyclingButtonWidget.<Boolean>builder(v -> v
                        ? Text.literal("§aEnabled")
                        : Text.literal("§cDisabled"))
                .values(Boolean.TRUE, Boolean.FALSE)
                .initially(enabled)
                .tooltip(v -> Tooltip.of(Text.literal("Toggle the auto-disconnect/reconnect feature on or off.")))
                .build(cx - btnW / 2, startY, btnW, 20,
                        Text.literal("Status: "),
                        (btn, val) -> enabled = val);
        this.addDrawableChild(enabledBtn);

        // ── Direction (above / below) ──────────────────────────────────────────
        directionBtn = CyclingButtonWidget.<Boolean>builder(v -> v
                        ? Text.literal("§eAbove threshold")
                        : Text.literal("§eBelow threshold"))
                .values(Boolean.FALSE, Boolean.TRUE)
                .initially(triggerAbove)
                .tooltip(v -> Tooltip.of(Text.literal(
                        v ? "Disconnect when your Y is ABOVE the trigger value."
                          : "Disconnect when your Y is BELOW the trigger value.")))
                .build(cx - btnW / 2, startY + rowH, btnW, 20,
                        Text.literal("Trigger: "),
                        (btn, val) -> triggerAbove = val);
        this.addDrawableChild(directionBtn);

        // ── Y value text field ─────────────────────────────────────────────────
        int fieldY = startY + rowH * 2;
        triggerYField = new TextFieldWidget(
                this.textRenderer,
                cx - btnW / 2, fieldY, btnW, 20,
                Text.literal("Trigger Y"));
        triggerYField.setMaxLength(10);
        triggerYField.setText(String.valueOf(triggerY));
        triggerYField.setPlaceholder(Text.literal("e.g. -64 or 320"));
        triggerYField.setTooltip(Tooltip.of(Text.literal(
                "The Y coordinate threshold. Use negative values for below void (e.g. -64).")));
        triggerYField.setChangedListener(text -> {
            try {
                triggerY = Double.parseDouble(text);
                triggerYField.setEditableColor(0xFFFFFF);
            } catch (NumberFormatException e) {
                triggerYField.setEditableColor(0xFF4444);
            }
        });
        this.addDrawableChild(triggerYField);

        // ── Reconnect delay slider ─────────────────────────────────────────────
        int sliderY = startY + rowH * 3;
        delaySlider = new DelaySliderWidget(cx - btnW / 2, sliderY, btnW, 20, reconnectDelayTicks);
        this.addDrawableChild(delaySlider);

        // ── Preset buttons ─────────────────────────────────────────────────────
        int presetY = startY + rowH * 4 + 8;
        int pW = 60;
        int gap = 5;
        int totalPresets = 4;
        int presetsTotalW = totalPresets * pW + (totalPresets - 1) * gap;
        int px = cx - presetsTotalW / 2;

        // Void (below -64)
        ButtonWidget voidBtn = ButtonWidget.builder(Text.literal("Void"), btn -> applyPreset(-64, false))
                .dimensions(px, presetY, pW, 18).build();
        voidBtn.setTooltip(Tooltip.of(Text.literal("Disconnect below Y = -64 (Minecraft void)")));
        this.addDrawableChild(voidBtn);

        // Below 0
        ButtonWidget zeroBtn = ButtonWidget.builder(Text.literal("Y < 0"), btn -> applyPreset(0, false))
                .dimensions(px + pW + gap, presetY, pW, 18).build();
        zeroBtn.setTooltip(Tooltip.of(Text.literal("Disconnect below Y = 0")));
        this.addDrawableChild(zeroBtn);

        // Above 256
        ButtonWidget highBtn = ButtonWidget.builder(Text.literal("Y > 256"), btn -> applyPreset(256, true))
                .dimensions(px + (pW + gap) * 2, presetY, pW, 18).build();
        highBtn.setTooltip(Tooltip.of(Text.literal("Disconnect above Y = 256")));
        this.addDrawableChild(highBtn);

        // Above 320 (build limit)
        ButtonWidget topBtn = ButtonWidget.builder(Text.literal("Y > 320"), btn -> applyPreset(320, true))
                .dimensions(px + (pW + gap) * 3, presetY, pW, 18).build();
        topBtn.setTooltip(Tooltip.of(Text.literal("Disconnect above Y = 320 (build limit)")));
        this.addDrawableChild(topBtn);

        // ── Save / Cancel ──────────────────────────────────────────────────────
        int bottomY = this.height - 32;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Save & Close"), btn -> saveAndClose())
                .dimensions(cx - 105, bottomY, 100, 20).build());
        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, btn -> closeWithoutSave())
                .dimensions(cx + 5, bottomY, 100, 20).build());
    }

    private void applyPreset(double y, boolean above) {
        triggerY = y;
        triggerAbove = above;
        triggerYField.setText(String.valueOf(y));
        triggerYField.setEditableColor(0xFFFFFF);
        // Update the direction button visual
        directionBtn.setValue(above);
    }

    private void saveAndClose() {
        // Parse Y one more time in case user didn't trigger listener
        try {
            triggerY = Double.parseDouble(triggerYField.getText());
        } catch (NumberFormatException ignored) {}

        YReconnectConfig cfg = YReconnectConfig.get();
        cfg.enabled             = enabledBtn.getValue();
        cfg.triggerY            = triggerY;
        cfg.triggerAbove        = directionBtn.getValue();
        cfg.reconnectDelayTicks = ((DelaySliderWidget) delaySlider).getTickValue();
        YReconnectConfig.save();

        YReconnect.LOGGER.info("[YReconnect] Config saved: enabled={} Y={} above={} delay={}",
                cfg.enabled, cfg.triggerY, cfg.triggerAbove, cfg.reconnectDelayTicks);

        if (this.client != null) this.client.setScreen(parent);
    }

    private void closeWithoutSave() {
        if (this.client != null) this.client.setScreen(parent);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Dim background
        this.renderBackground(context, mouseX, mouseY, delta);

        // Panel background
        int cx = this.width / 2;
        int panelW = 240;
        int panelH = 210;
        int panelX = cx - panelW / 2 - 10;
        int panelY = 45;
        context.fill(panelX, panelY, panelX + panelW + 20, panelY + panelH, 0xAA000000);
        context.drawBorder(panelX, panelY, panelW + 20, panelH, 0xFF555555);

        // Title
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("§b⚡ §fYReconnect §7Config §b⚡"),
                cx, 20, 0xFFFFFF);

        // Section labels
        context.drawTextWithShadow(this.textRenderer, Text.literal("§7─ General ─"), panelX + 6, 52, 0x888888);
        context.drawTextWithShadow(this.textRenderer, Text.literal("§7─ Threshold ─"), panelX + 6, 52 + 28, 0x888888);
        context.drawTextWithShadow(this.textRenderer,
                Text.literal("§7Trigger Y value:"), cx - 100, 52 + 56 - 10, 0xAAAAAA);
        context.drawTextWithShadow(this.textRenderer,
                Text.literal("§7Reconnect delay:"), cx - 100, 52 + 84 - 10, 0xAAAAAA);
        context.drawTextWithShadow(this.textRenderer,
                Text.literal("§7Presets:"), panelX + 6, 52 + 28 * 4 + 2, 0xAAAAAA);

        // Keybind hint at bottom
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("§8Press §7K §8to open this screen in-game"),
                cx, this.height - 14, 0x666666);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false; // Don't pause game while config is open
    }

    // ── Inner slider widget ────────────────────────────────────────────────────

    private static class DelaySliderWidget extends SliderWidget {
        // Range: 0–200 ticks (0–10 seconds)
        private static final int MIN = 0;
        private static final int MAX = 200;

        DelaySliderWidget(int x, int y, int width, int height, int initialTicks) {
            super(x, y, width, height, Text.empty(), (double)(initialTicks - MIN) / (MAX - MIN));
            this.updateMessage();
        }

        int getTickValue() {
            return MIN + (int) Math.round(this.value * (MAX - MIN));
        }

        @Override
        protected void updateMessage() {
            int ticks = getTickValue();
            double seconds = ticks / 20.0;
            this.setMessage(Text.literal(
                    String.format("Delay: §e%d ticks §7(%.1fs)", ticks, seconds)));
        }

        @Override
        protected void applyValue() {
            // value updated by slider internally
        }
    }
}
