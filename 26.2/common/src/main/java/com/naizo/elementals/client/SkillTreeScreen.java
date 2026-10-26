package com.naizo.elementals.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

/** Hosts the shared TreeView. Minecraft 26.2 and 26.3 edition. */
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
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        GuiCanvas canvas = new GuiCanvas(graphics);
        view.render(canvas, mouseX, mouseY);
        canvas.finish();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
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
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return view.mouseClicked(event.x(), event.y(), event.button());
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        return view.mouseReleased(event.x(), event.y(), event.button());
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        return view.mouseMoved(event.x(), event.y());
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        view.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return view.mouseScrolled(mouseX, mouseY, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (!view.typing() && PowerKeys.OPEN_TREE.matches(event)) {
            view.keyPressed(256);
            return true;
        }
        return view.keyPressed(event.key()) || super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        boolean handled = false;
        for (char c : event.codepointAsString().toCharArray()) {
            handled |= view.charTyped(c);
        }
        return handled;
    }

    @Override
    public long gameTime() {
        return minecraft.level != null ? minecraft.level.getGameTime() : 0;
    }

    @Override
    public void setClipboard(String text) {
        minecraft.keyboardHandler.setClipboard(text);
    }

    @Override
    public String clipboard() {
        return minecraft.keyboardHandler.getClipboard();
    }

    @Override
    public void click() {
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    public boolean shift() {
        return minecraft.hasShiftDown();
    }

    @Override
    public boolean control() {
        return minecraft.hasControlDown();
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
