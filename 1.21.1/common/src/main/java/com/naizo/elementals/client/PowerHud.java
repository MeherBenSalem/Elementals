package com.naizo.elementals.client;

import com.naizo.elementals.core.AbilityCatalog;
import com.naizo.elementals.core.AbilityDef;
import com.naizo.elementals.core.Keystone;
import com.naizo.elementals.core.PowerProfile;
import com.naizo.elementals.core.Progression;

import java.util.function.IntFunction;

/** The in-game HUD: ability bar to the right of the hotbar, Essence, Power XP and level-up banners. */
public final class PowerHud {
    private static final int GOLD = 0xE8C872;
    private static final int ESSENCE = 0x5FC8FF;

    private PowerHud() {
    }

    public static void render(Canvas c, long gameTime, float partialTick, IntFunction<String> castKey, String treeKey, boolean hudHidden) {
        if (!ClientPowers.ready || hudHidden) {
            return;
        }
        PowerProfile profile = ClientPowers.profile;
        boolean any = false;
        for (String slot : profile.slots) {
            any |= !slot.isEmpty();
        }
        int w = c.width();
        int h = c.height();
        float size = 20;
        float gap = 2;
        float total = PowerProfile.SLOTS * size + (PowerProfile.SLOTS - 1) * gap;
        float x0 = w / 2f + 91 + 8;
        float y0 = h - 22;
        if (x0 + total > w - 4) {
            x0 = w / 2f - total / 2f;
            y0 = h - 22 - 44;
        }
        if (any) {
            boolean bloodPact = ClientPowers.sheet.has(Keystone.BLOOD_PACT);
            for (int i = 0; i < PowerProfile.SLOTS; i++) {
                float x = x0 + i * (size + gap);
                String id = profile.slots[i];
                AbilityDef def = AbilityCatalog.get(id);
                c.fill(x, y0, x + size, y0 + size, 0xB0101320);
                c.rect(x, y0, x + size, y0 + size, def == null ? 0x803A3F55 : Canvas.alpha(def.school.color, 0.9f));
                if (def == null) {
                    continue;
                }
                c.item(def.icon, x + 2, y0 + 2, 16);
            }
            c.raise();
            for (int i = 0; i < PowerProfile.SLOTS; i++) {
                float x = x0 + i * (size + gap);
                AbilityDef def = AbilityCatalog.get(profile.slots[i]);
                String key = castKey.apply(i);
                if (def != null) {
                    long ready = profile.cooldownReady(def.id);
                    double cooldownTicks = ClientPowers.config.param(def.id, "cooldown") * 20
                            * ClientPowers.sheet.cooldownMultiplier(ClientPowers.config.maxCooldownReduction);
                    double remaining = ready - (gameTime + partialTick);
                    if (remaining > 0) {
                        float fraction = (float) Math.min(1, remaining / Math.max(1, cooldownTicks));
                        c.fill(x + 1, y0 + 1 + (size - 2) * (1 - fraction), x + size - 1, y0 + size - 1, 0xB0000000);
                        String seconds = remaining >= 200 ? String.valueOf((int) Math.ceil(remaining / 20)) : String.format(java.util.Locale.ROOT, "%.1f", remaining / 20);
                        c.centered(seconds, x + size / 2f, y0 + 7, 0.7f, 0xFFFFFFFF, true);
                    } else if (!bloodPact && ClientPowers.essence < ClientPowers.config.param(def.id, "cost")) {
                        c.fill(x + 1, y0 + 1, x + size - 1, y0 + size - 1, 0x80103060);
                    }
                }
                c.text(key, x + 1, y0 + size - 6, 0.55f, 0xFFD8D4C8, true);
            }
        }
        // Essence and Power XP above the ability bar.
        float barX = x0;
        float barW = total;
        float barY = y0 - 6;
        double max = Math.max(1, ClientPowers.maxEssence);
        float fill = (float) (barW * Math.max(0, Math.min(1, ClientPowers.essence / max)));
        if (any || ClientPowers.essence < max) {
            c.fill(barX, barY, barX + barW, barY + 4, 0xC0101320);
            c.gradient(barX, barY, barX + fill, barY + 4, Canvas.alpha(0x8FE2FF, 1), Canvas.alpha(ESSENCE, 1));
            c.rect(barX - 1, barY - 1, barX + barW + 1, barY + 5, 0x60000000);
            String text = (int) Math.floor(ClientPowers.essence) + "";
            c.text(text, barX + barW + 3, barY - 1, 0.55f, Canvas.alpha(0x8FE2FF, 1), true);
        }
        if (!ClientPowers.replaceMode() && (any || profile.level > 0)) {
            double need = Progression.xpToNext(ClientPowers.config, profile.level);
            float xpFill = profile.level >= ClientPowers.config.maxLevel ? barW : (float) (barW * Math.min(1, profile.xp / need));
            c.fill(barX, barY - 3, barX + barW, barY - 1, 0xA0101320);
            c.fill(barX, barY - 3, barX + xpFill, barY - 1, Canvas.alpha(GOLD, 0.9f));
            c.text(String.valueOf(profile.level), barX - c.textWidth(String.valueOf(profile.level)) * 0.6f - 3, barY - 2, 0.6f, Canvas.alpha(GOLD, 1), true);
        }
        int available = ClientPowers.available();
        if (available > 0) {
            float pulse = 0.6f + 0.4f * (float) Math.sin(System.currentTimeMillis() / 250.0);
            String text = "+" + available + " skill point" + (available == 1 ? "" : "s") + "  [" + treeKey + "]";
            c.text(text, barX, barY - 13, 0.65f, Canvas.alpha(GOLD, pulse), true);
        }
        drawNotices(c, w);
    }

    private static void drawNotices(Canvas c, int w) {
        long now = System.currentTimeMillis();
        float y = 34;
        for (ClientPowers.Notice notice : ClientPowers.NOTICES) {
            long age = now - notice.created();
            if (age > 4200 || notice.kind().equals("error")) {
                continue;
            }
            float alpha = Math.min(1, Math.min(age / 200f, (4200 - age) / 700f));
            boolean big = notice.kind().equals("level") || notice.kind().equals("points") || notice.kind().equals("origin");
            float scale = big ? 1.1f : 0.85f;
            float width = c.textWidth(notice.text()) * scale;
            if (big) {
                float reveal = Math.min(1, age / 400f);
                float lineW = (width / 2 + 30) * reveal;
                c.fill(w / 2f - lineW, y + 13 * scale, w / 2f + lineW, y + 13 * scale + 1, Canvas.alpha(GOLD, alpha * 0.8f));
                c.fill(w / 2f - lineW, y - 3, w / 2f + lineW, y - 2, Canvas.alpha(GOLD, alpha * 0.5f));
            }
            c.centered(notice.text(), w / 2f, y, scale, Canvas.alpha(notice.kind().equals("warning") ? 0xFF6A5A : GOLD, alpha), true);
            y += 18 * scale;
        }
    }
}
