package com.naizo.elementals.client;

import com.google.gson.JsonObject;
import com.naizo.elementals.core.AbilityCatalog;
import com.naizo.elementals.core.AbilityDef;
import com.naizo.elementals.core.BuildCode;
import com.naizo.elementals.core.Keystone;
import com.naizo.elementals.core.NodeType;
import com.naizo.elementals.core.PowerProfile;
import com.naizo.elementals.core.School;
import com.naizo.elementals.core.SkillNode;
import com.naizo.elementals.core.SkillTree;
import com.naizo.elementals.core.Stat;
import com.naizo.elementals.core.StatSheet;
import com.naizo.elementals.net.Net;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * The skill tree screen: pan, zoom, search, path allocation, staged commits, refunds, a planner,
 * build codes, the ability loadout and a stat summary. Drawing goes through {@link Canvas} and input
 * arrives as plain numbers, so this class is the same in every Minecraft version.
 */
public final class TreeView {
    /** What the screen needs from the game. */
    public interface Host {
        long gameTime();

        void setClipboard(String text);

        String clipboard();

        void click();

        boolean shift();

        boolean control();

        String castKey(int slot);

        void close();
    }

    // GLFW key codes (identical in every version)
    private static final int KEY_ESCAPE = 256, KEY_ENTER = 257, KEY_TAB = 258, KEY_BACKSPACE = 259, KEY_KP_ENTER = 335;
    private static final int KEY_RIGHT = 262, KEY_LEFT = 263, KEY_DOWN = 264, KEY_UP = 265, KEY_HOME = 268;
    private static final int KEY_A = 65, KEY_C = 67, KEY_D = 68, KEY_F = 70, KEY_H = 72, KEY_S = 83, KEY_V = 86, KEY_W = 87;
    private static final int KEY_1 = 49, KEY_EQUAL = 61, KEY_MINUS = 45, KEY_KP_ADD = 334, KEY_KP_SUB = 333;

    static final int GOLD = 0xE8C872, PENDING = 0x8CF08C, REFUND = 0xFF6A5A, PLAN = 0x7FB2FF, LOCKED = 0x59607A,
            LINK = 0x2A2F44, TEXT = 0xFFE8E6F0, DIM = 0xFF8C90A6, STAT = 0xFF9CC4FF;

    private static double camX, camY, zoom = 0.55;
    private static boolean cameraSet;
    private static final double MIN_ZOOM = 0.18, MAX_ZOOM = 2.2;

    private final Host host;
    private int w, h;
    private double targetZoom = zoom;
    private double zoomAnchorX, zoomAnchorY;
    private Double flyX, flyY;
    private final Set<String> pending = new LinkedHashSet<>();
    private final Set<String> refunds = new LinkedHashSet<>();
    private final Set<String> plan = new LinkedHashSet<>();
    private Set<String> effectiveCache = Set.of();
    private StatSheet effectiveSheet = new StatSheet();
    private int effectiveVersion = -1;
    private int changeCounter;
    private String hovered;
    private List<String> hoverPath = List.of();
    private final TextField search = new TextField(40, "Search nodes  (Ctrl+F)");
    private final TextField code = new TextField(400, "Paste a build code");
    private final List<String> matches = new ArrayList<>();
    private String lastQuery = "";
    private int matchIndex = -1;
    private boolean statsOpen;
    private float statsScroll;
    private boolean shareOpen;
    private int pickerSlot = -1;
    private boolean dragging;
    private boolean dragMoved;
    private double pressX, pressY, lastX, lastY;
    private String status = "";
    private int statusColor = TEXT;
    private long statusAt;
    private long resetArmedAt;
    private boolean discardArmed;
    private final List<Button> buttons = new ArrayList<>();
    private final float[] stars = new float[260 * 4];
    private double minX, minY, maxX, maxY;
    private SkillTree boundsFor;
    // Screen layout, recomputed every frame so every GUI scale fits (down to 426x240: 720p at "Auto").
    private boolean compact;
    private float panelW, miniSize, btnW, btnH, btnGap, slotSize, slotGap, loadX, loadY;

    private record Button(float x0, float y0, float x1, float y1, Runnable action, String tooltip) {
        boolean contains(double x, double y) {
            return x >= x0 && x < x1 && y >= y0 && y < y1;
        }
    }

    public TreeView(Host host) {
        this.host = host;
        java.util.Random random = new java.util.Random(4242);
        for (int i = 0; i < stars.length; i += 4) {
            stars[i] = random.nextFloat();
            stars[i + 1] = random.nextFloat();
            stars[i + 2] = 0.4f + random.nextFloat() * 1.3f;
            stars[i + 3] = random.nextFloat() * 6.28f;
        }
    }

    public void init(int width, int height) {
        this.w = width;
        this.h = height;
        if (!cameraSet) {
            centerOnBuild();
            zoom = targetZoom = 0.55;
            cameraSet = true;
        }
        targetZoom = zoom;
    }

    // ------------------------------------------------------------------ state

    private SkillTree tree() {
        return ClientPowers.tree;
    }

    private Set<String> effective() {
        if (effectiveVersion != ClientPowers.revision * 31 + changeCounter) {
            Set<String> set = new LinkedHashSet<>(ClientPowers.profile.allocated);
            set.removeAll(refunds);
            set.addAll(pending);
            effectiveCache = set;
            effectiveSheet = StatSheet.of(tree(), set);
            effectiveVersion = ClientPowers.revision * 31 + changeCounter;
        }
        return effectiveCache;
    }

    private void changed() {
        changeCounter++;
        discardArmed = false;
    }

    private int availableAfter() {
        SkillTree tree = tree();
        return tree == null ? 0 : ClientPowers.earned() - tree.cost(effective());
    }

    private boolean hasChanges() {
        return !pending.isEmpty() || !refunds.isEmpty();
    }

    private enum State { ALLOCATED, PENDING, REFUND, PLANNED, AVAILABLE, LOCKED }

    private State state(SkillNode node) {
        Set<String> allocated = ClientPowers.profile.allocated;
        if (refunds.contains(node.id)) {
            return State.REFUND;
        }
        if (pending.contains(node.id)) {
            return State.PENDING;
        }
        if (allocated.contains(node.id)) {
            return State.ALLOCATED;
        }
        if (plan.contains(node.id)) {
            return State.PLANNED;
        }
        return tree().canAllocateDirectly(effective(), node.id) ? State.AVAILABLE : State.LOCKED;
    }

    private void say(String text, int color) {
        status = text;
        statusColor = color;
        statusAt = System.currentTimeMillis();
    }

    // ------------------------------------------------------------------ geometry

    private float sx(double x) {
        return (float) (w / 2.0 + (x - camX) * zoom);
    }

    private float sy(double y) {
        return (float) (h / 2.0 + (y - camY) * zoom);
    }

    private double tx(double screenX) {
        return camX + (screenX - w / 2.0) / zoom;
    }

    private double ty(double screenY) {
        return camY + (screenY - h / 2.0) / zoom;
    }

    private void centerOnBuild() {
        SkillTree tree = tree();
        camX = 0;
        camY = 0;
        if (tree == null) {
            return;
        }
        Set<String> allocated = ClientPowers.profile.allocated;
        if (allocated.isEmpty()) {
            return;
        }
        double x = 0, y = 0;
        for (String id : allocated) {
            SkillNode node = tree.get(id);
            if (node != null) {
                x += node.x;
                y += node.y;
            }
        }
        camX = x / allocated.size();
        camY = y / allocated.size();
    }

    private void flyTo(double x, double y) {
        flyX = x;
        flyY = y;
    }

    private SkillNode nodeAt(double mouseX, double mouseY) {
        SkillTree tree = tree();
        if (tree == null) {
            return null;
        }
        SkillNode best = null;
        double bestDistance = Double.MAX_VALUE;
        for (SkillNode node : tree.nodes()) {
            double dx = sx(node.x) - mouseX;
            double dy = sy(node.y) - mouseY;
            double r = Math.max(5, node.type.radius * zoom) + 2;
            double d = dx * dx + dy * dy;
            if (d <= r * r && d < bestDistance) {
                best = node;
                bestDistance = d;
            }
        }
        return best;
    }

    private void computeBounds() {
        SkillTree tree = tree();
        if (tree == boundsFor || tree == null) {
            return;
        }
        boundsFor = tree;
        minX = minY = Double.MAX_VALUE;
        maxX = maxY = -Double.MAX_VALUE;
        for (SkillNode node : tree.nodes()) {
            minX = Math.min(minX, node.x);
            minY = Math.min(minY, node.y);
            maxX = Math.max(maxX, node.x);
            maxY = Math.max(maxY, node.y);
        }
    }

    // ------------------------------------------------------------------ actions

    private void allocate(SkillNode node) {
        SkillTree tree = tree();
        Set<String> current = effective();
        List<String> path = tree.pathTo(current, node.id);
        if (path.isEmpty()) {
            say("That node can't be reached.", REFUND);
            return;
        }
        int cost = tree.cost(path);
        if (cost > availableAfter()) {
            plan.addAll(path);
            changed();
            say("Not enough points (" + cost + " needed). Added to your plan instead.", PLAN);
            return;
        }
        for (String id : path) {
            if (refunds.remove(id)) {
                continue;
            }
            pending.add(id);
            plan.remove(id);
        }
        changed();
        host.click();
        say("Staged " + path.size() + (path.size() == 1 ? " node" : " nodes") + ". Press Apply to confirm.", PENDING);
    }

    private void unstage(SkillNode node) {
        Set<String> working = new LinkedHashSet<>(effective());
        List<String> removed = tree().removeWithOrphans(working, node.id);
        for (String id : removed) {
            if (pending.remove(id)) {
                continue;
            }
            if (ClientPowers.profile.allocated.contains(id)) {
                refunds.add(id);
            }
        }
        changed();
        host.click();
    }

    private void toggleRefund(SkillNode node) {
        if (refunds.remove(node.id)) {
            if (!tree().isValidAllocation(effectiveRaw())) {
                refunds.add(node.id);
                say("Refund it last: other nodes depend on it.", REFUND);
                return;
            }
            changed();
            host.click();
            return;
        }
        Set<String> current = new LinkedHashSet<>(effective());
        if (!tree().canRemove(current, node.id)) {
            say("Other nodes depend on this one. Refund the outer nodes first.", REFUND);
            return;
        }
        refunds.add(node.id);
        changed();
        host.click();
        say("Refund staged. Costs " + ClientPowers.config.refundCostLevels + " XP level(s) per node.", REFUND);
    }

    private Set<String> effectiveRaw() {
        Set<String> set = new LinkedHashSet<>(ClientPowers.profile.allocated);
        set.removeAll(refunds);
        set.addAll(pending);
        return set;
    }

    private void apply() {
        if (!hasChanges()) {
            return;
        }
        JsonObject message = Net.message(Net.COMMIT);
        message.add("nodes", ids(effective()));
        ClientPowers.send(message);
        pending.clear();
        refunds.clear();
        changed();
        host.click();
        say("Build sent to the server.", GOLD);
    }

    private static com.google.gson.JsonArray ids(Set<String> set) {
        com.google.gson.JsonArray array = new com.google.gson.JsonArray();
        set.forEach(array::add);
        return array;
    }

    private void cancel() {
        pending.clear();
        refunds.clear();
        changed();
        host.click();
        say("Changes discarded.", DIM);
    }

    private void applyPlan() {
        SkillTree tree = tree();
        int budget = availableAfter();
        boolean progress = true;
        int added = 0;
        while (progress && budget > 0) {
            progress = false;
            for (String id : new ArrayList<>(plan)) {
                SkillNode node = tree.get(id);
                if (node == null) {
                    plan.remove(id);
                    continue;
                }
                if (node.cost <= budget && tree.canAllocateDirectly(effective(), id)) {
                    pending.add(id);
                    plan.remove(id);
                    budget -= node.cost;
                    added++;
                    changed();
                    progress = true;
                }
            }
        }
        host.click();
        say(added == 0 ? "No planned node can be afforded yet." : "Staged " + added + " planned nodes.", added == 0 ? DIM : PENDING);
    }

    private void resetTree() {
        long now = System.currentTimeMillis();
        if (now - resetArmedAt > 3000) {
            resetArmedAt = now;
            say("Click Reset again to refund everything for " + ClientPowers.config.fullResetCostLevels + " XP levels.", REFUND);
            return;
        }
        resetArmedAt = 0;
        ClientPowers.send(Net.message(Net.RESET));
        pending.clear();
        refunds.clear();
        changed();
    }

    private void assign(int slot, String ability) {
        JsonObject message = Net.message(Net.SLOT);
        message.addProperty("slot", slot);
        message.addProperty("ability", ability);
        ClientPowers.send(message);
        host.click();
    }

    private void importCode() {
        try {
            BuildCode.Decoded decoded = BuildCode.decode(tree(), code.text);
            plan.clear();
            for (String id : decoded.nodes()) {
                if (!effective().contains(id)) {
                    plan.add(id);
                }
            }
            changed();
            say((decoded.sameTree() ? "" : "This code was made for a different tree. ") + "Imported " + plan.size() + " nodes as a plan.",
                    decoded.sameTree() ? PLAN : REFUND);
            code.text = "";
            code.focused = false;
        } catch (IllegalArgumentException ex) {
            say(ex.getMessage(), REFUND);
        }
    }

    private void updateSearch() {
        String query = search.text.trim().toLowerCase(Locale.ROOT);
        if (query.equals(lastQuery)) {
            return;
        }
        lastQuery = query;
        matches.clear();
        matchIndex = -1;
        if (query.isEmpty() || tree() == null) {
            return;
        }
        for (SkillNode node : tree().nodes()) {
            if (matchesQuery(node, query)) {
                matches.add(node.id);
            }
        }
    }

    private static boolean matchesQuery(SkillNode node, String query) {
        if (node.name.toLowerCase(Locale.ROOT).contains(query) || node.type.id().contains(query) || node.school.id.contains(query)
                || node.school.element.toLowerCase(Locale.ROOT).contains(query)) {
            return true;
        }
        for (String line : node.statLines()) {
            if (line.toLowerCase(Locale.ROOT).contains(query)) {
                return true;
            }
        }
        AbilityDef ability = AbilityCatalog.get(node.ability);
        if (ability != null && (ability.name.toLowerCase(Locale.ROOT).contains(query) || ability.description.toLowerCase(Locale.ROOT).contains(query))) {
            return true;
        }
        Keystone keystone = Keystone.get(node.keystone);
        return keystone != null && String.join(" ", keystone.lines).toLowerCase(Locale.ROOT).contains(query);
    }

    private void nextMatch() {
        if (matches.isEmpty()) {
            return;
        }
        matchIndex = (matchIndex + 1) % matches.size();
        SkillNode node = tree().get(matches.get(matchIndex));
        flyTo(node.x, node.y);
        targetZoom = Math.max(targetZoom, 0.9);
        zoomAnchorX = w / 2.0;
        zoomAnchorY = h / 2.0;
    }

    // ------------------------------------------------------------------ input

    public boolean mouseClicked(double x, double y, int button) {
        if (tree() == null) {
            return false;
        }
        search.focused = false;
        code.focused = false;
        for (Button b : new ArrayList<>(buttons)) {
            if (b.contains(x, y)) {
                if (button == 0) {
                    b.action().run();
                } else if (button == 1 && b.tooltip() != null && b.tooltip().startsWith("slot:")) {
                    assign(Integer.parseInt(b.tooltip().substring(5)), "");
                }
                return true;
            }
        }
        if (pickerSlot >= 0) {
            pickerSlot = -1;
            return true;
        }
        if (minimapContains(x, y)) {
            jumpMinimap(x, y);
            dragging = false;
            return true;
        }
        if (overUi(x, y)) {
            dragging = false;
            return true;
        }
        dragging = button == 0;
        dragMoved = false;
        pressX = lastX = x;
        pressY = lastY = y;
        if (button == 1) {
            SkillNode node = nodeAt(x, y);
            if (node != null) {
                State state = state(node);
                if (state == State.PENDING) {
                    unstage(node);
                } else if (state == State.ALLOCATED || state == State.REFUND) {
                    toggleRefund(node);
                } else if (state == State.PLANNED) {
                    plan.remove(node.id);
                    changed();
                }
            }
            return true;
        }
        return true;
    }

    public boolean mouseReleased(double x, double y, int button) {
        if (button == 0 && dragging && !dragMoved && tree() != null) {
            SkillNode node = nodeAt(x, y);
            if (node != null) {
                State state = state(node);
                if (host.shift()) {
                    if (plan.remove(node.id)) {
                        changed();
                    } else if (state == State.AVAILABLE || state == State.LOCKED) {
                        List<String> path = tree().pathTo(effective(), node.id);
                        for (String id : path) {
                            plan.add(id);
                        }
                        changed();
                        say("Planned " + path.size() + " nodes (" + tree().cost(path) + " points).", PLAN);
                    }
                } else if (state == State.PENDING) {
                    unstage(node);
                } else if (state == State.REFUND) {
                    toggleRefund(node);
                } else if (state == State.AVAILABLE || state == State.LOCKED || state == State.PLANNED) {
                    allocate(node);
                }
            }
        }
        dragging = false;
        return true;
    }

    public boolean mouseMoved(double x, double y) {
        if (dragging) {
            if (Math.abs(x - pressX) + Math.abs(y - pressY) > 3) {
                dragMoved = true;
            }
            if (dragMoved) {
                camX -= (x - lastX) / zoom;
                camY -= (y - lastY) / zoom;
                flyX = null;
            }
        }
        lastX = x;
        lastY = y;
        return true;
    }

    public boolean mouseScrolled(double x, double y, double delta) {
        if (delta == 0) {
            return false;
        }
        float[] stats = statsRect();
        if (statsOpen && x >= stats[0] && x < stats[2] && y >= stats[1] && y < stats[3]) {
            statsScroll -= (float) (delta * 18);
            return true;
        }
        targetZoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, targetZoom * (delta > 0 ? 1.18 : 1 / 1.18)));
        zoomAnchorX = x;
        zoomAnchorY = y;
        return true;
    }

    public boolean keyPressed(int key) {
        TextField field = search.focused ? search : code.focused ? code : null;
        if (field != null) {
            if (key == KEY_ESCAPE) {
                field.focused = false;
                return true;
            }
            if (key == KEY_BACKSPACE) {
                field.backspace(host.control());
                return true;
            }
            if (key == KEY_V && host.control()) {
                field.paste(host.clipboard());
                return true;
            }
            if (key == KEY_ENTER || key == KEY_KP_ENTER || key == KEY_TAB) {
                if (field == search) {
                    nextMatch();
                } else {
                    importCode();
                }
                return true;
            }
            return true;
        }
        if (key == KEY_ESCAPE) {
            // Overlays close first, so Esc never throws away the screen behind them.
            if (pickerSlot >= 0 || shareOpen) {
                pickerSlot = -1;
                shareOpen = false;
                return true;
            }
            if (hasChanges() && !discardArmed) {
                discardArmed = true;
                say("You have staged changes. Press Esc again to discard them, or Enter to apply.", REFUND);
                return true;
            }
            host.close();
            return true;
        }
        if (key == KEY_F && host.control()) {
            search.focused = true;
            return true;
        }
        if (key == KEY_ENTER || key == KEY_KP_ENTER) {
            apply();
            return true;
        }
        if (key == KEY_HOME || key == KEY_H) {
            centerOnBuild();
            flyX = null;
            return true;
        }
        if (key == KEY_S) {
            statsOpen = !statsOpen;
            return true;
        }
        double step = 60 / zoom;
        switch (key) {
            case KEY_LEFT, KEY_A -> camX -= step;
            case KEY_RIGHT, KEY_D -> camX += step;
            case KEY_UP, KEY_W -> camY -= step;
            case KEY_DOWN -> camY += step;
            case KEY_EQUAL, KEY_KP_ADD -> mouseScrolled(w / 2.0, h / 2.0, 1);
            case KEY_MINUS, KEY_KP_SUB -> mouseScrolled(w / 2.0, h / 2.0, -1);
            default -> {
                if (key >= KEY_1 && key < KEY_1 + PowerProfile.SLOTS && hovered != null) {
                    SkillNode node = tree().get(hovered);
                    if (node != null && node.ability != null && ClientPowers.sheet.abilities.contains(node.ability)) {
                        assign(key - KEY_1, node.ability);
                        say(AbilityCatalog.get(node.ability).name + " set to slot " + (key - KEY_1 + 1) + ".", GOLD);
                    }
                }
                return false;
            }
        }
        flyX = null;
        return true;
    }

    public boolean charTyped(char c) {
        if (search.type(c) || code.type(c)) {
            return true;
        }
        return search.focused || code.focused;
    }

    /** True while a text field has focus, so letter keys type instead of acting. */
    public boolean typing() {
        return search.focused || code.focused;
    }

    // ------------------------------------------------------------------ render

    public void render(Canvas c, int mouseX, int mouseY) {
        this.w = c.width();
        this.h = c.height();
        buttons.clear();
        long millis = System.currentTimeMillis();
        animate();
        updateSearch();
        drawBackground(c, millis);
        if (tree() == null) {
            c.centered("Waiting for the server to send the skill tree...", w / 2f, h / 2f, 1, TEXT, true);
            c.centered("Remnants | Skills and Powers must be installed on the server.", w / 2f, h / 2f + 14, 1, DIM, true);
            return;
        }
        computeBounds();
        layout();
        SkillNode hover = (pickerSlot < 0 && !overUi(mouseX, mouseY)) ? nodeAt(mouseX, mouseY) : null;
        hovered = hover == null ? null : hover.id;
        hoverPath = hover == null ? List.of() : tree().pathTo(effective(), hover.id);
        drawTree(c, millis);
        c.raise();
        drawTopBar(c, millis);
        drawBottomLeft(c);
        drawLoadout(c, mouseX, mouseY);
        drawButtons(c, mouseX, mouseY);
        drawMinimap(c);
        if (statsOpen) {
            drawStats(c);
        }
        if (shareOpen) {
            drawShare(c, millis);
        }
        drawStatus(c, millis);
        drawNotices(c, millis);
        c.raise();
        if (hover != null) {
            drawNodeTooltip(c, hover, mouseX, mouseY);
        } else {
            for (Button b : buttons) {
                if (b.contains(mouseX, mouseY) && b.tooltip() != null && !b.tooltip().startsWith("slot:")) {
                    drawTextTooltip(c, List.of(new Line(b.tooltip(), TEXT, 1f)), mouseX, mouseY);
                }
            }
        }
    }

    private boolean overUi(double x, double y) {
        float[] stats = statsRect();
        if (y < 30 || (statsOpen && x >= stats[0] && y < stats[3]) || minimapContains(x, y)) {
            return true;
        }
        if (shareOpen && x >= w / 2.0 - 130 && x < w / 2.0 + 130 && y >= h / 2.0 - 60 && y < h / 2.0 + 56) {
            return true;
        }
        if (x < panelW + 8 && y > h - 46) {
            return true;
        }
        if (x > loadX - 4 && x < loadX + loadoutWidth() + 4 && y > loadY - 12 && y < loadY + slotSize + 10) {
            return true;
        }
        for (Button b : buttons) {
            if (b.contains(x, y)) {
                return true;
            }
        }
        return false;
    }

    private void layout() {
        compact = w < 640 || h < 330;
        panelW = compact ? 150 : 172;
        miniSize = compact ? Math.min(84, h * 0.32f) : 118;
        btnW = compact ? 52 : 58;
        btnH = compact ? 14 : 16;
        btnGap = compact ? 3 : 4;
        slotSize = compact ? 20 : 24;
        slotGap = compact ? 4 : 6;
        float total = loadoutWidth();
        float right = buttonsX() - 8 - (plan.isEmpty() ? 0 : btnW + 6);
        float left = panelW + 14;
        loadY = h - slotSize - 16;
        loadX = Math.max(left, Math.min(right - total, w / 2f - total / 2f));
        if (loadX + total > right) {
            // No room on the bottom row: the loadout sits above the level panel instead.
            loadX = 8;
            loadY = h - 46 - slotSize - 14;
        }
    }

    private float loadoutWidth() {
        return PowerProfile.SLOTS * slotSize + (PowerProfile.SLOTS - 1) * slotGap;
    }

    private float buttonsX() {
        return w - miniSize - 8 - 6 - btnW;
    }

    private float[] statsRect() {
        float buttonsTop = h - 8 - btnH - 5 * (btnH + btnGap);
        return new float[]{w - Math.min(200, w * 0.45f), 36, w - 4, buttonsTop - 6};
    }

    private void animate() {
        if (Math.abs(targetZoom - zoom) > 1e-4) {
            double beforeX = tx(zoomAnchorX);
            double beforeY = ty(zoomAnchorY);
            zoom += (targetZoom - zoom) * 0.35;
            if (Math.abs(targetZoom - zoom) < 1e-3) {
                zoom = targetZoom;
            }
            if (flyX == null) {
                camX += beforeX - tx(zoomAnchorX);
                camY += beforeY - ty(zoomAnchorY);
            }
        }
        if (flyX != null) {
            camX += (flyX - camX) * 0.22;
            camY += (flyY - camY) * 0.22;
            if (Math.abs(flyX - camX) + Math.abs(flyY - camY) < 0.5) {
                flyX = null;
            }
        }
        computeBounds();
        if (tree() != null) {
            camX = Math.max(minX - 200, Math.min(maxX + 200, camX));
            camY = Math.max(minY - 200, Math.min(maxY + 200, camY));
        }
    }

    private void drawBackground(Canvas c, long millis) {
        c.gradient(0, 0, w, h, 0xFF05060C, 0xFF0C0E1B);
        float t = millis / 1000f;
        for (int i = 0; i < stars.length; i += 4) {
            float depth = stars[i + 2];
            float x = (float) (((stars[i] * w - camX * zoom * 0.06 * depth) % w + w) % w);
            float y = (float) (((stars[i + 1] * h - camY * zoom * 0.06 * depth) % h + h) % h);
            float twinkle = 0.45f + 0.55f * (float) Math.sin(t * (0.6 + depth) + stars[i + 3]);
            int a = (int) (40 + 140 * twinkle * (depth / 1.7f));
            float s = depth > 1.3f ? 1.5f : 1f;
            c.fill(x, y, x + s, y + s, (a << 24) | 0xDDE4FF);
        }
        if (tree() == null) {
            return;
        }
        for (School school : School.playable()) {
            double a = Math.toRadians(school.angle());
            double nx = Math.sin(a) * 600;
            double ny = -Math.cos(a) * 600;
            float size = (float) (1300 * zoom);
            c.texture("glow", sx(nx) - size / 2, sy(ny) - size / 2, size, size, Canvas.alpha(school.color, 0.10f));
        }
        float core = (float) (520 * zoom);
        c.texture("glow", sx(0) - core / 2, sy(0) - core / 2, core, core, Canvas.alpha(GOLD, 0.16f));
        float ring = (float) (340 * zoom);
        c.texture("ring", sx(0) - ring / 2, sy(0) - ring / 2, ring, ring, Canvas.alpha(GOLD, 0.35f));
        float inner = (float) (180 * zoom);
        c.texture("ring", sx(0) - inner / 2, sy(0) - inner / 2, inner, inner, Canvas.alpha(GOLD, 0.22f));
        if (zoom > 0.25) {
            float scale = (float) Math.max(0.8, Math.min(2.4, zoom * 2.6));
            c.centered("REMNANTS", sx(0), sy(0) - 6 * scale, scale, Canvas.alpha(GOLD, 0.8f), true);
        }
        for (School school : School.playable()) {
            double a = Math.toRadians(school.angle());
            float x = sx(Math.sin(a) * 1060);
            float y = sy(-Math.cos(a) * 1060);
            float scale = (float) Math.max(1, Math.min(3, zoom * 3.2));
            c.centered(school.displayName.toUpperCase(Locale.ROOT), x, y - 4 * scale, scale, Canvas.alpha(school.color, 0.85f), true);
        }
    }

    private int linkColor(State a, State b, boolean onPath) {
        if (onPath) {
            return 0xF0FFFFFF;
        }
        boolean aOn = a == State.ALLOCATED || a == State.PENDING;
        boolean bOn = b == State.ALLOCATED || b == State.PENDING;
        if (a == State.REFUND || b == State.REFUND) {
            return Canvas.alpha(REFUND, aOn || bOn || (a == State.REFUND && b == State.REFUND) ? 0.85f : 0.35f);
        }
        if (aOn && bOn) {
            return (a == State.PENDING || b == State.PENDING) ? Canvas.alpha(PENDING, 0.95f) : Canvas.alpha(GOLD, 1f);
        }
        if ((a == State.PLANNED || aOn) && (b == State.PLANNED || bOn)) {
            return Canvas.alpha(PLAN, 0.7f);
        }
        if (aOn || bOn) {
            return 0xC0707898;
        }
        return Canvas.alpha(LINK, 1f) | 0xFF000000;
    }

    private void drawTree(Canvas c, long millis) {
        SkillTree tree = tree();
        Map<String, State> states = new LinkedHashMap<>();
        for (SkillNode node : tree.nodes()) {
            states.put(node.id, state(node));
        }
        Set<String> path = new LinkedHashSet<>(hoverPath);
        float pad = 40;
        float thickness = (float) Math.max(1.2, Math.min(4, 3 * zoom));
        for (String[] edge : tree.edges()) {
            SkillNode a = tree.get(edge[0]);
            SkillNode b = tree.get(edge[1]);
            float ax = sx(a.x), ay = sy(a.y), bx = sx(b.x), by = sy(b.y);
            if ((ax < -pad && bx < -pad) || (ax > w + pad && bx > w + pad) || (ay < -pad && by < -pad) || (ay > h + pad && by > h + pad)) {
                continue;
            }
            State sa = states.get(a.id);
            State sb = states.get(b.id);
            boolean onPath = (path.contains(a.id) || effective().contains(a.id)) && (path.contains(b.id) || effective().contains(b.id))
                    && (path.contains(a.id) || path.contains(b.id));
            int color = linkColor(sa, sb, onPath);
            boolean lit = (sa == State.ALLOCATED || sa == State.PENDING) && (sb == State.ALLOCATED || sb == State.PENDING);
            if (lit) {
                c.line(ax, ay, bx, by, thickness * 3, Canvas.alpha(color, 0.18f));
            }
            c.line(ax, ay, bx, by, lit || onPath ? thickness : thickness * 0.7f, color);
        }
        float pulse = 0.5f + 0.5f * (float) Math.sin(millis / 300.0);
        boolean icons = zoom >= 0.32;
        Set<String> matchSet = new LinkedHashSet<>(matches);
        for (SkillNode node : tree.nodes()) {
            float x = sx(node.x);
            float y = sy(node.y);
            float r = (float) (node.type.radius * zoom);
            if (x < -r - 10 || x > w + r + 10 || y < -r - 10 || y > h + r + 10) {
                continue;
            }
            State state = states.get(node.id);
            boolean hover = node.id.equals(hovered);
            if (hover) {
                r *= 1.12f;
            }
            int schoolColor = node.school == School.NONE ? 0xD8CFB8 : node.school.color;
            int frame = switch (state) {
                case ALLOCATED -> node.type == NodeType.KEYSTONE ? 0xFFE7A0 : GOLD;
                case PENDING -> PENDING;
                case REFUND -> REFUND;
                case PLANNED -> PLAN;
                case AVAILABLE -> Canvas.mix(schoolColor, 0xFFFFFF, 0.25f);
                case LOCKED -> Canvas.mix(LOCKED, schoolColor, 0.25f);
            };
            if (state == State.ALLOCATED || state == State.PENDING) {
                float g = r * 4.2f;
                c.texture("glow", x - g / 2, y - g / 2, g, g, Canvas.alpha(state == State.PENDING ? PENDING : schoolColor, 0.55f));
            } else if (state == State.AVAILABLE) {
                float g = r * 3.2f;
                c.texture("glow", x - g / 2, y - g / 2, g, g, Canvas.alpha(schoolColor, 0.18f + 0.12f * pulse));
            }
            if (matchSet.contains(node.id)) {
                float g = r * 5f;
                c.texture("glow", x - g / 2, y - g / 2, g, g, Canvas.alpha(0xFFFFFF, 0.35f + 0.35f * pulse));
            }
            if (hover) {
                float g = r * 4.6f;
                c.texture("glow", x - g / 2, y - g / 2, g, g, Canvas.alpha(0xFFFFFF, 0.3f));
            }
            if (r < 3.2f) {
                c.fill(x - 1.5f, y - 1.5f, x + 1.5f, y + 1.5f, Canvas.alpha(frame, 1));
                continue;
            }
            c.texture("node_fill", x - r, y - r, r * 2, r * 2, state == State.LOCKED ? 0xFF0B0C12 : 0xFF141725);
            String frameTexture = switch (node.type) {
                case START -> "node_start";
                case NOTABLE -> "node_notable";
                case KEYSTONE -> "node_keystone";
                case ABILITY -> "node_ability";
                default -> "node_minor";
            };
            float fr = node.type == NodeType.KEYSTONE ? r * 1.25f : node.type == NodeType.START ? r * 1.2f : node.type == NodeType.ABILITY ? r * 1.18f : r * 1.08f;
            c.texture(frameTexture, x - fr, y - fr, fr * 2, fr * 2, Canvas.alpha(frame, state == State.LOCKED ? 0.85f : 1f));
            if (node.ability != null && AbilityCatalog.get(node.ability) != null && AbilityCatalog.get(node.ability).ultimate) {
                float ur = fr * 1.25f;
                c.texture("ring", x - ur, y - ur, ur * 2, ur * 2, Canvas.alpha(frame, 0.6f + 0.3f * pulse));
            }
            if (icons) {
                float size = r * (node.type == NodeType.MINOR ? 1.15f : 1.25f);
                c.item(node.icon, x - size / 2, y - size / 2, size);
            } else {
                c.fill(x - r * 0.35f, y - r * 0.35f, x + r * 0.35f, y + r * 0.35f, Canvas.alpha(frame, 0.9f));
            }
        }
        if (icons) {
            // Darken locked icons after the item pass so they read as unavailable.
            c.raise();
            for (SkillNode node : tree.nodes()) {
                if (states.get(node.id) != State.LOCKED) {
                    continue;
                }
                float x = sx(node.x);
                float y = sy(node.y);
                float r = (float) (node.type.radius * zoom);
                if (x < -r || x > w + r || y < -r || y > h + r || node.id.equals(hovered)) {
                    continue;
                }
                c.texture("node_fill", x - r * 0.82f, y - r * 0.82f, r * 1.64f, r * 1.64f, 0x99080910);
            }
        }
    }

    private void drawTopBar(Canvas c, long millis) {
        c.gradient(0, 0, w, 30, 0xF00A0B13, 0xC00A0B13);
        c.fill(0, 30, w, 31, Canvas.alpha(GOLD, 0.45f));
        float sw = Math.min(170, w / 4f);
        float searchX = w - sw - 8;
        int available = availableAfter();
        int staged = pending.isEmpty() ? 0 : tree().cost(pending);
        String points = available + (available == 1 ? " SKILL POINT" : " SKILL POINTS");
        float pointsW = c.textWidth(points) * 1.25f;
        float titleEnd = 8 + c.textWidth("REMNANTS") * 1.2f;
        c.text("REMNANTS", 8, 5, 1.2f, Canvas.alpha(GOLD, 1), true);
        String subtitle = "SKILLS AND POWERS";
        if (titleEnd + 6 + c.textWidth(subtitle) * 0.85f + 8 < w / 2f - pointsW / 2) {
            c.text(subtitle, titleEnd + 6, 7, 0.85f, 0xFFC9C2AE, true);
            titleEnd += 6 + c.textWidth(subtitle) * 0.85f;
        }
        School origin = ClientPowers.profile.originSchool();
        String originText = origin == School.NONE
                ? (compact ? "Pick an origin: click a glowing start" : "Choose an origin: click any glowing start node")
                : "Origin: " + origin.displayName;
        c.text(originText, 8, 18, 0.75f, origin == School.NONE ? Canvas.alpha(GOLD, 0.9f) : Canvas.alpha(origin.color, 1), false);
        int color = available > 0 ? Canvas.alpha(GOLD, 1) : 0xFFB8B2A2;
        c.centered(points, fitCenter(w / 2f, pointsW, titleEnd + 8, searchX - 8), 5, 1.25f, color, true);
        boolean changes = staged > 0 || !refunds.isEmpty();
        String[] hints = changes
                ? new String[]{
                        (staged > 0 ? staged + " staged" : "") + (staged > 0 && !refunds.isEmpty() ? " | " : "") + (!refunds.isEmpty() ? refunds.size() + " to refund" : "") + "  -  Enter to apply",
                        "Enter to apply"}
                : new String[]{
                        "Click: allocate path   Right-click: refund   Shift-click: plan   Scroll: zoom",
                        "Click: allocate   Right-click: refund   Shift: plan",
                        "Right-click: refund"};
        float hintMin = 8 + c.textWidth(originText) * 0.75f + 10;
        for (String hint : hints) {
            float hw = c.textWidth(hint) * 0.7f;
            if (hw <= searchX - 10 - hintMin) {
                c.centered(hint, fitCenter(w / 2f, hw, hintMin, searchX - 10), 19, 0.7f, changes ? Canvas.alpha(PENDING, 1) : DIM, false);
                break;
            }
        }
        search.draw(c, searchX, 6, w - 8, 24, millis, GOLD);
        buttons.add(new Button(searchX, 6, w - 8, 24, () -> {
            search.focused = true;
            code.focused = false;
        }, null));
        if (!matches.isEmpty() || !search.text.isEmpty()) {
            String count = matches.isEmpty() ? "no match" : (matchIndex + 1) + "/" + matches.size() + "  Enter: next";
            c.text(count, w - 8 - c.textWidth(count) * 0.6f, 25.5f, 0.6f, matches.isEmpty() ? 0xFFFF8070 : DIM, false);
        }
    }

    /** Centres a block of the given width on {@code ideal}, nudged to stay inside [min, max]. */
    private static float fitCenter(float ideal, float width, float min, float max) {
        return Math.max(min + width / 2, Math.min(max - width / 2, ideal));
    }

    private void drawBottomLeft(Canvas c) {
        PowerProfile profile = ClientPowers.profile;
        float x = 8;
        float y = h - 40;
        c.panel(x - 2, y - 4, x + panelW - 2, h - 6, 0x663A3F55);
        if (ClientPowers.replaceMode()) {
            c.text("RPG Level " + profile.rasLevel, x + 2, y, 1, Canvas.alpha(GOLD, 1), true);
            c.text(compact ? "Points from RPG Player Skills" : "Skill points from Remnants | RPG Player Skills", x + 2, y + 12, 0.6f, DIM, false);
        } else {
            c.text("Power Level " + profile.level, x + 2, y, 1, Canvas.alpha(GOLD, 1), true);
            double need = com.naizo.elementals.core.Progression.xpToNext(ClientPowers.config, profile.level);
            float bar = panelW - 12;
            float fill = profile.level >= ClientPowers.config.maxLevel ? bar : (float) (bar * Math.min(1, profile.xp / need));
            c.fill(x + 2, y + 13, x + 2 + bar, y + 16, 0xFF1A1D2C);
            c.fill(x + 2, y + 13, x + 2 + fill, y + 16, Canvas.alpha(GOLD, 0.95f));
            String xp = profile.level >= ClientPowers.config.maxLevel ? "MAX" : Math.round(profile.xp) + " / " + Math.round(need) + " XP";
            c.text(xp, x + 2, y + 19, 0.6f, DIM, false);
        }
        double ras = ClientPowers.rasBonus();
        if (ClientPowers.rasLoaded && ras > 0) {
            c.text("+" + Stat.trim(ras, 1) + (compact ? "% ability dmg (RPG level)" : "% ability damage from RPG level"), x + 2, y + 26, 0.6f, Canvas.alpha(PENDING, 1), false);
        }
    }

    private void drawLoadout(Canvas c, int mouseX, int mouseY) {
        PowerProfile profile = ClientPowers.profile;
        int slots = PowerProfile.SLOTS;
        float size = slotSize;
        float gap = slotGap;
        float total = loadoutWidth();
        float x0 = loadX;
        float y0 = loadY;
        c.text("ABILITIES", x0 + total / 2f - c.textWidth("ABILITIES") * 0.35f, y0 - 10, 0.7f, Canvas.alpha(GOLD, 0.8f), false);
        for (int i = 0; i < slots; i++) {
            float x = x0 + i * (size + gap);
            String ability = profile.slots[i];
            AbilityDef def = AbilityCatalog.get(ability);
            boolean hover = mouseX >= x && mouseX < x + size && mouseY >= y0 && mouseY < y0 + size;
            c.fill(x, y0, x + size, y0 + size, hover ? 0xF0242838 : 0xE0141725);
            c.rect(x, y0, x + size, y0 + size, def == null ? 0xFF3A3F55 : Canvas.alpha(def.school.color, 1));
            if (def != null) {
                c.item(def.icon, x + (size - 16) / 2f, y0 + (size - 16) / 2f, 16);
            }
            String key = host.castKey(i);
            c.text(key, x + size - c.textWidth(key) * 0.6f - 1, y0 + size + 2, 0.6f, DIM, false);
            final int slot = i;
            buttons.add(new Button(x, y0, x + size, y0 + size, () -> pickerSlot = pickerSlot == slot ? -1 : slot, "slot:" + i));
        }
        if (pickerSlot >= 0) {
            List<String> unlocked = new ArrayList<>(ClientPowers.sheet.abilities);
            float ps = 22;
            float width = Math.max(1, unlocked.size()) * (ps + 3) + 8;
            float px = Math.max(4, Math.min(w - width - 4, w / 2f - width / 2f));
            float py = Math.max(44, y0 - ps - 24);
            c.raise();
            c.panel(px, py - 12, px + width, py + ps + 6, Canvas.alpha(GOLD, 0.7f));
            c.text(unlocked.isEmpty() ? "No abilities unlocked yet" : "Choose an ability for slot " + (pickerSlot + 1), px + 4, py - 9, 0.7f, TEXT, false);
            for (int i = 0; i < unlocked.size(); i++) {
                AbilityDef def = AbilityCatalog.get(unlocked.get(i));
                float x = px + 4 + i * (ps + 3);
                boolean hover = mouseX >= x && mouseX < x + ps && mouseY >= py && mouseY < py + ps;
                c.fill(x, py, x + ps, py + ps, hover ? 0xFF2A2F44 : 0xFF161925);
                c.rect(x, py, x + ps, py + ps, Canvas.alpha(def.school.color, hover ? 1 : 0.7f));
                c.item(def.icon, x + 3, py + 3, 16);
                final String id = def.id;
                final int slot = pickerSlot;
                buttons.add(new Button(x, py, x + ps, py + ps, () -> {
                    assign(slot, id);
                    pickerSlot = -1;
                }, def.name));
            }
        }
    }

    private void button(Canvas c, float x0, float y0, float x1, float y1, String label, int color, boolean enabled, Runnable action, String tooltip, int mouseX, int mouseY) {
        boolean hover = enabled && mouseX >= x0 && mouseX < x1 && mouseY >= y0 && mouseY < y1;
        c.fill(x0, y0, x1, y1, hover ? 0xF0262A3C : 0xE0131622);
        c.rect(x0, y0, x1, y1, enabled ? Canvas.alpha(color, hover ? 1f : 0.75f) : 0xFF2A2E40);
        c.centered(label, (x0 + x1) / 2f, y0 + (y1 - y0 - 7) / 2f, 0.8f, enabled ? Canvas.alpha(color, 1) : 0xFF555A70, false);
        if (enabled) {
            buttons.add(new Button(x0, y0, x1, y1, action, tooltip));
        }
    }

    private void drawButtons(Canvas c, int mouseX, int mouseY) {
        float bw = btnW;
        float bh = btnH;
        float step = btnH + btnGap;
        float x = buttonsX();
        float y = h - 8 - bh;
        boolean changes = hasChanges();
        button(c, x, y, x + bw, y + bh, "Apply", PENDING, changes, this::apply, "Confirm every staged change (Enter)", mouseX, mouseY);
        button(c, x, y - step, x + bw, y - step + bh, "Undo", 0xC9C2AE, changes, this::cancel, "Discard staged changes", mouseX, mouseY);
        button(c, x, y - step * 2, x + bw, y - step * 2 + bh, statsOpen ? "Hide Stats" : "Stats", STAT & 0xFFFFFF, true, () -> statsOpen = !statsOpen, "Totals of every allocated node (S)", mouseX, mouseY);
        button(c, x, y - step * 3, x + bw, y - step * 3 + bh, "Share", PLAN, true, () -> shareOpen = !shareOpen, "Build codes and saved builds", mouseX, mouseY);
        button(c, x, y - step * 4, x + bw, y - step * 4 + bh, "Center", 0xC9C2AE, true, () -> {
            centerOnBuild();
            flyX = null;
        }, "Center on your build (H)", mouseX, mouseY);
        button(c, x, y - step * 5, x + bw, y - step * 5 + bh, "Reset Tree", REFUND, !ClientPowers.profile.allocated.isEmpty(), this::resetTree,
                "Refund every node for " + ClientPowers.config.fullResetCostLevels + " XP levels", mouseX, mouseY);
        if (!plan.isEmpty()) {
            button(c, x - bw - 6, y, x - 6, y + bh, "Use Plan", PLAN, true, this::applyPlan, "Stage as much of your plan as you can afford", mouseX, mouseY);
            button(c, x - bw - 6, y - step, x - 6, y - step + bh, "Clear Plan", 0xC9C2AE, true, () -> {
                plan.clear();
                changed();
            }, "Remove every planned node", mouseX, mouseY);
            c.text(plan.size() + " planned (" + tree().cost(plan) + " pts)", x - bw - 6, y - step * 2 + 6, 0.7f, Canvas.alpha(PLAN, 1), false);
        }
    }

    private float[] minimapRect() {
        return new float[]{w - miniSize - 8, h - miniSize - 8, w - 8, h - 8};
    }

    private boolean minimapContains(double x, double y) {
        float[] r = minimapRect();
        return x >= r[0] && x < r[2] && y >= r[1] && y < r[3];
    }

    private void jumpMinimap(double x, double y) {
        float[] r = minimapRect();
        double span = Math.max(maxX - minX, maxY - minY) + 120;
        double scale = (r[2] - r[0] - 8) / span;
        double cx = (r[0] + r[2]) / 2.0;
        double cy = (r[1] + r[3]) / 2.0;
        flyTo((minX + maxX) / 2 + (x - cx) / scale, (minY + maxY) / 2 + (y - cy) / scale);
    }

    private void drawMinimap(Canvas c) {
        float[] r = minimapRect();
        c.panel(r[0], r[1], r[2], r[3], 0x883A3F55);
        double span = Math.max(maxX - minX, maxY - minY) + 120;
        double scale = (r[2] - r[0] - 8) / span;
        float cx = (r[0] + r[2]) / 2f;
        float cy = (r[1] + r[3]) / 2f;
        double midX = (minX + maxX) / 2;
        double midY = (minY + maxY) / 2;
        Set<String> eff = effective();
        for (SkillNode node : tree().nodes()) {
            float x = (float) (cx + (node.x - midX) * scale);
            float y = (float) (cy + (node.y - midY) * scale);
            int color;
            float s;
            if (eff.contains(node.id)) {
                color = pending.contains(node.id) ? Canvas.alpha(PENDING, 1) : Canvas.alpha(GOLD, 1);
                s = 1.5f;
            } else if (plan.contains(node.id)) {
                color = Canvas.alpha(PLAN, 1);
                s = 1.2f;
            } else {
                color = Canvas.alpha(node.school == School.NONE ? 0x8A8370 : node.school.color, 0.45f);
                s = node.type == NodeType.MINOR ? 0.8f : 1.3f;
            }
            c.fill(x - s / 2, y - s / 2, x + s / 2, y + s / 2, color);
        }
        float vx0 = (float) (cx + (tx(0) - midX) * scale);
        float vy0 = (float) (cy + (ty(0) - midY) * scale);
        float vx1 = (float) (cx + (tx(w) - midX) * scale);
        float vy1 = (float) (cy + (ty(h) - midY) * scale);
        c.scissor((int) r[0] + 1, (int) r[1] + 1, (int) r[2] - 1, (int) r[3] - 1);
        c.rect(vx0, vy0, vx1, vy1, 0xCCFFFFFF);
        c.endScissor();
    }

    private record Line(String text, int color, float scale) {
    }

    private void drawStats(Canvas c) {
        float[] rect = statsRect();
        float x0 = rect[0];
        float y0 = rect[1];
        float x1 = rect[2];
        float y1 = rect[3];
        c.panel(x0, y0, x1, y1, Canvas.alpha(STAT, 0.6f));
        c.text("BUILD SUMMARY", x0 + 6, y0 + 5, 0.9f, Canvas.alpha(GOLD, 1), true);
        StatSheet now = ClientPowers.sheet;
        effective();
        StatSheet after = effectiveSheet;
        List<Line> lines = new ArrayList<>();
        lines.add(new Line(effective().size() + " nodes   " + after.abilities.size() + " abilities   " + after.keystones.size() + " keystones", DIM, 0.7f));
        Set<String> keys = new LinkedHashSet<>(now.totals.keySet());
        keys.addAll(after.totals.keySet());
        List<String> ordered = new ArrayList<>();
        for (String key : Stat.all().keySet()) {
            if (keys.contains(key)) {
                ordered.add(key);
            }
        }
        for (String key : keys) {
            if (!ordered.contains(key)) {
                ordered.add(key);
            }
        }
        for (String key : ordered) {
            double before = now.get(key);
            double next = after.get(key);
            if (Math.abs(before - next) < 1e-6) {
                lines.add(new Line(Stat.describe(key, next), STAT, 0.75f));
            } else {
                lines.add(new Line(Stat.describe(key, before) + "  ->  " + Stat.signed(next, Stat.get(key) == null ? Stat.Kind.FLAT : Stat.get(key).kind), Canvas.alpha(PENDING, 1), 0.75f));
            }
        }
        if (!after.keystones.isEmpty()) {
            lines.add(new Line("", TEXT, 0.5f));
            for (String id : after.keystones) {
                Keystone keystone = Keystone.get(id);
                lines.add(new Line(keystone.name, Canvas.alpha(GOLD, 1), 0.8f));
            }
        }
        lines.add(new Line("", TEXT, 0.5f));
        lines.add(new Line("Max Essence " + Stat.trim(after.maxEssence(ClientPowers.config.baseMaxEssence), 0)
                + "   Regen " + Stat.trim(after.essenceRegenPerSecond(ClientPowers.config.baseEssenceRegen), 1) + "/s", 0xFF7FD8FF, 0.75f));
        lines.add(new Line("Cooldowns x" + Stat.trim(after.cooldownMultiplier(ClientPowers.config.maxCooldownReduction), 2)
                + "   Crit " + Stat.trim(after.critChance() * 100, 1) + "% x" + Stat.trim(after.critMultiplier(), 2), 0xFF7FD8FF, 0.75f));
        float content = 0;
        for (Line line : lines) {
            content += c.wrap(line.text(), (int) ((x1 - x0 - 14) / line.scale())).size() * (10 * line.scale() + 1);
        }
        float visible = y1 - 2 - (y0 + 18);
        statsScroll = Math.max(0, Math.min(Math.max(0, content - visible), statsScroll));
        float y = y0 + 18 - statsScroll;
        c.scissor((int) x0, (int) y0 + 16, (int) x1, (int) y1 - 2);
        for (Line line : lines) {
            for (String part : c.wrap(line.text(), (int) ((x1 - x0 - 14) / line.scale()))) {
                c.text(part, x0 + 6, y, line.scale(), line.color(), false);
                y += 10 * line.scale() + 1;
            }
        }
        c.endScissor();
        if (content > visible) {
            // Scrollbar: the list is longer than the panel on small screens.
            float track = visible;
            float thumb = Math.max(10, track * visible / content);
            float top = y0 + 18 + (track - thumb) * (statsScroll / (content - visible));
            c.fill(x1 - 4, y0 + 18, x1 - 2, y0 + 18 + track, 0x40FFFFFF);
            c.fill(x1 - 4, top, x1 - 2, top + thumb, Canvas.alpha(STAT, 0.9f));
        }
    }

    private void drawShare(Canvas c, long millis) {
        float pw = 260;
        float x0 = w / 2f - pw / 2;
        float y0 = h / 2f - 60;
        float x1 = x0 + pw;
        float y1 = y0 + 116;
        c.panel(x0, y0, x1, y1, Canvas.alpha(PLAN, 0.9f));
        c.text("SHARE YOUR BUILD", x0 + 8, y0 + 6, 0.9f, Canvas.alpha(PLAN, 1), true);
        String current = BuildCode.encode(tree(), effective());
        c.text(current.length() > 42 ? current.substring(0, 40) + "..." : current, x0 + 8, y0 + 22, 0.75f, TEXT, false);
        int mx = (int) lastX, my = (int) lastY;
        button(c, x0 + 8, y0 + 34, x0 + 120, y0 + 50, "Copy build code", PLAN, true, () -> {
            host.setClipboard(current);
            say("Build code copied to the clipboard.", PLAN);
        }, null, mx, my);
        button(c, x0 + 126, y0 + 34, x1 - 8, y0 + 50, "Copy plan as code", PLAN, !plan.isEmpty(), () -> {
            Set<String> all = new LinkedHashSet<>(effective());
            all.addAll(plan);
            host.setClipboard(BuildCode.encode(tree(), all));
            say("Build and plan copied as one code.", PLAN);
        }, null, mx, my);
        code.draw(c, x0 + 8, y0 + 58, x1 - 8, y0 + 74, millis, PLAN);
        buttons.add(new Button(x0 + 8, y0 + 58, x1 - 8, y0 + 74, () -> {
            code.focused = true;
            search.focused = false;
        }, null));
        button(c, x0 + 8, y0 + 80, x0 + 120, y0 + 96, "Import as plan", PLAN, true, () -> {
            if (code.text.isEmpty()) {
                code.text = host.clipboard() == null ? "" : host.clipboard().trim();
            }
            importCode();
        }, "Imported nodes become a plan: nothing is spent", mx, my);
        button(c, x0 + 126, y0 + 80, x1 - 8, y0 + 96, "Close", 0xC9C2AE, true, () -> shareOpen = false, null, mx, my);
        c.text("Imports never spend points. Use Plan stages what you can afford.", x0 + 8, y0 + 102, 0.6f, DIM, false);
    }

    private void drawStatus(Canvas c, long millis) {
        if (status.isEmpty() || millis - statusAt > 4500) {
            return;
        }
        float alpha = Math.min(1, (4500 - (millis - statusAt)) / 600f);
        float width = c.textWidth(status) * 0.85f + 16;
        float y = 38;
        c.fill(w / 2f - width / 2, y, w / 2f + width / 2, y + 14, Canvas.alpha(0x0B0C14, 0.85f * alpha));
        c.centered(status, w / 2f, y + 4, 0.85f, Canvas.alpha(statusColor, alpha), false);
    }

    private void drawNotices(Canvas c, long millis) {
        float y = 56;
        for (ClientPowers.Notice notice : ClientPowers.NOTICES) {
            long age = millis - notice.created();
            if (age > 5000) {
                continue;
            }
            float alpha = Math.min(1, Math.min(age / 250f, (5000 - age) / 600f));
            int color = notice.kind().equals("error") || notice.kind().equals("warning") ? REFUND : GOLD;
            c.centered(notice.text(), w / 2f, y, 0.8f, Canvas.alpha(color, alpha), true);
            y += 11;
        }
    }

    private void drawNodeTooltip(Canvas c, SkillNode node, int mouseX, int mouseY) {
        List<Line> lines = new ArrayList<>();
        int schoolColor = node.school == School.NONE ? 0xD8CFB8 : node.school.color;
        lines.add(new Line(node.name, Canvas.alpha(node.type == NodeType.KEYSTONE ? 0xFFE7A0 : schoolColor, 1), 1.15f));
        String kind = switch (node.type) {
            case START -> "Origin";
            case NOTABLE -> "Notable";
            case KEYSTONE -> "Keystone";
            case ABILITY -> AbilityCatalog.get(node.ability) != null && AbilityCatalog.get(node.ability).ultimate ? "Ultimate Ability" : "Ability";
            default -> "Passive";
        };
        lines.add(new Line(kind + (node.school == School.NONE ? "" : "  -  " + node.school.displayName) + (node.cost != 1 ? "  -  " + node.cost + " points" : ""), DIM, 0.75f));
        for (String stat : node.statLines()) {
            lines.add(new Line(stat, STAT, 0.9f));
        }
        Keystone keystone = Keystone.get(node.keystone);
        if (keystone != null) {
            for (String text : keystone.lines) {
                lines.add(new Line(text, 0xFFFFE08A, 0.9f));
            }
        }
        AbilityDef ability = AbilityCatalog.get(node.ability);
        if (ability != null) {
            Map<String, Double> params = new LinkedHashMap<>(ClientPowers.config.params(ability.id));
            lines.add(new Line(ability.describe(params), 0xFFD6D2C6, 0.85f));
            StatSheet sheet = effectiveSheet;
            double multiplier = sheet.damageMultiplier(ability.school, ClientPowers.rasBonus());
            List<String> numbers = new ArrayList<>();
            if (params.containsKey("damage")) {
                numbers.add("Damage " + Stat.trim(params.get("damage") * multiplier, 1));
            }
            if (params.containsKey("heal")) {
                numbers.add("Heal " + Stat.trim(params.get("heal") * sheet.healMultiplier(), 1));
            }
            numbers.add("Cooldown " + Stat.trim(params.getOrDefault("cooldown", 0.0) * sheet.cooldownMultiplier(ClientPowers.config.maxCooldownReduction), 1) + "s");
            numbers.add("Cost " + Stat.trim(params.getOrDefault("cost", 0.0), 0) + (sheet.has(Keystone.BLOOD_PACT) ? " (as Health /10)" : " Essence"));
            lines.add(new Line(String.join("   ", numbers), 0xFF7FD8FF, 0.8f));
            if (!ClientPowers.config.enabled(ability.id)) {
                lines.add(new Line("Disabled on this server", REFUND | 0xFF000000, 0.8f));
            }
        }
        if (node.requiredRasLevel > 0) {
            boolean met = !ClientPowers.rasLoaded || ClientPowers.profile.rasLevel >= node.requiredRasLevel;
            lines.add(new Line("Requires RPG Level " + node.requiredRasLevel, met ? DIM : 0xFFFF6A5A, 0.8f));
        }
        if (node.requiredPowerLevel > 0) {
            int level = ClientPowers.replaceMode() ? ClientPowers.profile.rasLevel : ClientPowers.profile.level;
            lines.add(new Line("Requires level " + node.requiredPowerLevel, level >= node.requiredPowerLevel ? DIM : 0xFFFF6A5A, 0.8f));
        }
        if (node.flavor != null && !node.flavor.isEmpty()) {
            lines.add(new Line(node.flavor, 0xFF8F8670, 0.75f));
        }
        State state = state(node);
        String hint = switch (state) {
            case ALLOCATED -> ability != null ? "Allocated. Press 1-" + PowerProfile.SLOTS + " to put it on a slot. Right-click to refund." : "Allocated. Right-click to refund.";
            case PENDING -> "Staged. Click again to unstage.";
            case REFUND -> "Marked for refund. Click to keep it.";
            case PLANNED, AVAILABLE, LOCKED -> {
                int cost = tree().cost(hoverPath);
                yield hoverPath.isEmpty() ? "Unreachable" : "Click: allocate " + hoverPath.size() + (hoverPath.size() == 1 ? " node" : " nodes") + " (" + cost + (cost == 1 ? " point" : " points") + ")"
                        + (cost > availableAfter() ? " - not enough points" : "");
            }
        };
        lines.add(new Line(hint, state == State.PENDING ? Canvas.alpha(PENDING, 1) : state == State.REFUND ? Canvas.alpha(REFUND, 1) : 0xFFB5AF9E, 0.75f));
        drawTextTooltip(c, lines, mouseX, mouseY);
    }

    private void drawTextTooltip(Canvas c, List<Line> lines, int mouseX, int mouseY) {
        int maxWidth = 220;
        List<Line> wrapped = new ArrayList<>();
        float width = 0;
        for (Line line : lines) {
            if (line.text().isEmpty()) {
                continue;
            }
            for (String part : c.wrap(line.text(), (int) (maxWidth / line.scale()))) {
                wrapped.add(new Line(part, line.color(), line.scale()));
                width = Math.max(width, c.textWidth(part) * line.scale());
            }
        }
        float height = 0;
        for (Line line : wrapped) {
            height += 10 * line.scale() + 1;
        }
        float x = mouseX + 14;
        float y = mouseY - 6;
        if (x + width + 12 > w) {
            x = mouseX - width - 20;
        }
        if (y + height + 10 > h) {
            y = h - height - 10;
        }
        y = Math.max(4, y);
        int border = wrapped.isEmpty() ? 0xFF3A3F55 : wrapped.get(0).color();
        c.fill(x - 5, y - 5, x + width + 5, y + height + 4, 0xF20A0B12);
        c.rect(x - 5, y - 5, x + width + 5, y + height + 4, Canvas.alpha(border, 0.9f));
        c.fill(x - 4, y - 4, x + width + 4, y - 3, Canvas.alpha(border, 0.35f));
        float ty = y;
        for (Line line : wrapped) {
            c.text(line.text(), x, ty, line.scale(), line.color(), false);
            ty += 10 * line.scale() + 1;
        }
    }

    public static void resetCamera() {
        cameraSet = false;
    }

    public List<String> pendingForTest() {
        return Collections.unmodifiableList(new ArrayList<>(pending));
    }

    public Deque<String> unused() {
        return new ArrayDeque<>();
    }
}
