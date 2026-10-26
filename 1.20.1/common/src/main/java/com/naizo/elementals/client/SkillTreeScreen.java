package com.naizo.elementals.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

/** Hosts the shared TreeView. Minecraft 1.20.1 edition. */
public class SkillTreeScreen extends Screen implements TreeView.Host {
    private final TreeView view = new TreeView(this);

    public SkillTreeScreen() {
        super(Component.translatable("screen.elementals.tree"));
    }

    @Override
    protected void init() {
        view.init(width, height);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        GuiCanvas canvas = new GuiCanvas(graphics);
        view.render(canvas, mouseX, mouseY);
        canvas.finish();
    }

    @Override
    public void renderBackground(GuiGraphics graphics) {
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return view.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return view.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return view.mouseMoved(mouseX, mouseY);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        view.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return view.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!view.typing() && PowerKeys.OPEN_TREE.matches(keyCode, scanCode)) {
            view.keyPressed(256);
            return true;
        }
        return view.keyPressed(keyCode) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return view.charTyped(codePoint);
    }

    @Override
    public long gameTime() {
        return minecraft != null && minecraft.level != null ? minecraft.level.getGameTime() : 0;
    }

    @Override
    public void setClipboard(String text) {
        if (minecraft != null) {
            minecraft.keyboardHandler.setClipboard(text);
        }
    }

    @Override
    public String clipboard() {
        return minecraft == null ? "" : minecraft.keyboardHandler.getClipboard();
    }

    @Override
    public void click() {
        if (minecraft != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }

    @Override
    public boolean shift() {
        return hasShiftDown();
    }

    @Override
    public boolean control() {
        return hasControlDown();
    }

    @Override
    public String castKey(int slot) {
        return PowerKeys.castKey(slot);
    }

    @Override
    public void close() {
        onClose();
    }
}
