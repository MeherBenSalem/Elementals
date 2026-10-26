"""Generates the GUI, entity and item textures for Remnants | Skills and Powers.

Usage: python3 tools/gen_textures.py <assets/elementals dir>
GUI textures are white so the game can tint them per school.
"""
import math
import os
import random
import sys

from PIL import Image, ImageDraw, ImageFilter

OUT = sys.argv[1]
SS = 4  # supersampling factor


def save(img, rel):
    path = os.path.join(OUT, rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)


FRAME = 128


def canvas(size=64):
    return Image.new("RGBA", (size * SS, size * SS), (0, 0, 0, 0))


def down(img, size=FRAME):
    return img.resize((size, size), Image.LANCZOS)


def ring(draw, cx, cy, r, width, fill=(255, 255, 255, 255)):
    draw.ellipse([cx - r, cy - r, cx + r, cy + r], outline=fill, width=width)


def gui():
    c = 32 * SS
    # minor node frame: clean ring with a soft inner bevel
    img = canvas()
    d = ImageDraw.Draw(img)
    ring(d, c, c, 29 * SS, 5 * SS)
    ring(d, c, c, 23 * SS, 1 * SS, (255, 255, 255, 110))
    save(down(img), "textures/gui/node_minor.png")

    # notable: double ring with eight studs
    img = canvas()
    d = ImageDraw.Draw(img)
    ring(d, c, c, 29 * SS, 4 * SS)
    ring(d, c, c, 23 * SS, 2 * SS, (255, 255, 255, 200))
    for i in range(8):
        a = i * math.pi / 4
        x = c + math.cos(a) * 26 * SS
        y = c + math.sin(a) * 26 * SS
        d.ellipse([x - 3 * SS, y - 3 * SS, x + 3 * SS, y + 3 * SS], fill=(255, 255, 255, 255))
    save(down(img), "textures/gui/node_notable.png")

    # keystone: ornate ring with twelve spikes
    img = canvas()
    d = ImageDraw.Draw(img)
    pts = []
    for i in range(24):
        a = i * math.pi / 12 - math.pi / 2
        r = 31 * SS if i % 2 == 0 else 26 * SS
        pts.append((c + math.cos(a) * r, c + math.sin(a) * r))
    d.polygon(pts, fill=(255, 255, 255, 255))
    d.ellipse([c - 23 * SS, c - 23 * SS, c + 23 * SS, c + 23 * SS], fill=(0, 0, 0, 0))
    ring(d, c, c, 20 * SS, 2 * SS, (255, 255, 255, 220))
    save(down(img), "textures/gui/node_keystone.png")

    # ability: diamond frame
    img = canvas()
    d = ImageDraw.Draw(img)
    outer = [(c, 2 * SS), (62 * SS, c), (c, 62 * SS), (2 * SS, c)]
    inner = [(c, 10 * SS), (54 * SS, c), (c, 54 * SS), (10 * SS, c)]
    d.polygon(outer, fill=(255, 255, 255, 255))
    d.polygon(inner, fill=(0, 0, 0, 0))
    inner2 = [(c, 13 * SS), (51 * SS, c), (c, 51 * SS), (13 * SS, c)]
    d.line(inner2 + [inner2[0]], fill=(255, 255, 255, 120), width=SS)
    save(down(img), "textures/gui/node_ability.png")

    # start: six-pointed star frame
    img = canvas()
    d = ImageDraw.Draw(img)
    for offset in (0, math.pi / 6):
        pts = []
        for i in range(6):
            a = offset + i * math.pi / 3 - math.pi / 2
            r = 31 * SS if offset == 0 else 26 * SS
            pts.append((c + math.cos(a) * r, c + math.sin(a) * r))
        tri1 = [pts[0], pts[2], pts[4]]
        tri2 = [pts[1], pts[3], pts[5]]
        d.polygon(tri1, outline=(255, 255, 255, 255), width=3 * SS)
        d.polygon(tri2, outline=(255, 255, 255, 255), width=3 * SS)
        break
    ring(d, c, c, 22 * SS, 4 * SS)
    save(down(img), "textures/gui/node_start.png")

    # filled disk
    img = canvas()
    d = ImageDraw.Draw(img)
    d.ellipse([3 * SS, 3 * SS, 61 * SS, 61 * SS], fill=(255, 255, 255, 255))
    save(down(img), "textures/gui/node_fill.png")

    # soft glow
    size = 256
    img = Image.new("RGBA", (size, size))
    px = img.load()
    for y in range(size):
        for x in range(size):
            dx = (x + 0.5 - size / 2) / (size / 2)
            dy = (y + 0.5 - size / 2) / (size / 2)
            r = math.sqrt(dx * dx + dy * dy)
            a = max(0.0, 1 - r) ** 2.2
            px[x, y] = (255, 255, 255, int(255 * a))
    save(img, "textures/gui/glow.png")

    # thin decorative ring with tick marks
    img = canvas()
    d = ImageDraw.Draw(img)
    ring(d, c, c, 30 * SS, int(1.2 * SS))
    for i in range(36):
        a = i * math.pi / 18
        r0 = 27 * SS if i % 3 == 0 else 28.5 * SS
        d.line([(c + math.cos(a) * r0, c + math.sin(a) * r0), (c + math.cos(a) * 30 * SS, c + math.sin(a) * 30 * SS)],
               fill=(255, 255, 255, 220), width=SS)
    save(down(img, 512), "textures/gui/ring.png")


def golem():
    """128x128 texture laid out like the iron golem: basalt plates with molten seams."""
    rnd = random.Random(7)
    w = h = 128
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    px = img.load()
    for y in range(h):
        for x in range(w):
            n = rnd.random()
            base = 34 + int(n * 18)
            px[x, y] = (base + 6, base, base - 4, 255)
    d = ImageDraw.Draw(img)
    # molten seams
    for _ in range(70):
        x, y = rnd.randrange(w), rnd.randrange(h)
        length = rnd.randrange(4, 14)
        dx, dy = rnd.choice([(1, 0), (0, 1), (1, 1), (1, -1)])
        for i in range(length):
            px_x, px_y = (x + dx * i) % w, (y + dy * i) % h
            glow = rnd.random()
            col = (255, 120 + int(glow * 90), 30, 255) if glow > 0.25 else (200, 60, 20, 255)
            px[px_x, px_y] = col
            if rnd.random() < 0.3:
                x += rnd.choice([-1, 0, 1])
    # embers
    for _ in range(140):
        x, y = rnd.randrange(w), rnd.randrange(h)
        px[x, y] = (255, 190, 80, 255)
    # face: head front face at u 8..16, v 8..18 (head box 8x10x8 at 0,0)
    d.rectangle([8, 8, 15, 17], fill=(48, 40, 36, 255))
    for (ex, ey) in ((9, 12), (13, 12)):
        d.rectangle([ex, ey, ex + 1, ey], fill=(255, 220, 120, 255))
        px[ex, ey + 1] = (255, 140, 40, 255)
        px[ex + 1, ey + 1] = (255, 140, 40, 255)
    d.line([(10, 15), (13, 15)], fill=(255, 110, 30, 255))
    # nose block at 24,0 (2x4x2): molten
    d.rectangle([24, 0, 31, 6], fill=(230, 100, 30, 255))
    # chest core on the body front face (body box at 0,40, 18x12x11 -> front face at u 11..29, v 51..63)
    d.ellipse([15, 52, 24, 61], fill=(255, 150, 40, 255))
    d.ellipse([17, 54, 22, 59], fill=(255, 230, 140, 255))
    save(img, "textures/entity/fire_golem.png")


def item_icon(name, palette, draw_fn):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw_fn(img.load(), palette)
    save(img, "textures/item/" + name + ".png")


def tome(px, p):
    cover, edge, pages, gem = p
    for y in range(2, 15):
        for x in range(3, 13):
            px[x, y] = cover
    for y in range(2, 15):
        px[3, y] = edge
        px[12, y] = edge
    for x in range(3, 13):
        px[x, 2] = edge
        px[x, 14] = edge
    for y in range(3, 14):
        px[13, y] = pages
    for x in range(4, 13):
        px[x, 15] = edge if x % 2 else pages
    for (x, y) in ((7, 6), (8, 6), (7, 7), (8, 7), (6, 8), (9, 8), (7, 9), (8, 9), (7, 10), (8, 10)):
        px[x, y] = gem
    px[7, 8] = (255, 255, 255, 255)


def logo():
    size = 512
    img = Image.new("RGBA", (size * 2, size * 2), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    c = size
    d.ellipse([20, 20, 2 * size - 20, 2 * size - 20], fill=(10, 11, 20, 255))
    colors = [(255, 106, 42), (111, 231, 255), (178, 107, 255), (79, 141, 255), (201, 160, 90), (110, 224, 122)]
    for i, col in enumerate(colors):
        a = -math.pi / 2 + i * math.pi / 3
        x, y = c + math.cos(a) * 560, c + math.sin(a) * 560
        glow = Image.new("RGBA", img.size, (0, 0, 0, 0))
        ImageDraw.Draw(glow).ellipse([x - 300, y - 300, x + 300, y + 300], fill=col + (90,))
        glow = glow.filter(ImageFilter.GaussianBlur(120))
        img = Image.alpha_composite(img, glow)
    d = ImageDraw.Draw(img)
    gold = (232, 200, 114, 255)
    for r, wdt in ((470, 14), (330, 6), (180, 10)):
        d.ellipse([c - r, c - r, c + r, c + r], outline=gold, width=wdt)
    pts = []
    for i, col in enumerate(colors):
        a = -math.pi / 2 + i * math.pi / 3
        x, y = c + math.cos(a) * 330, c + math.sin(a) * 330
        pts.append((x, y))
    for i in range(6):
        d.line([pts[i], pts[(i + 2) % 6]], fill=(232, 200, 114, 170), width=6)
        d.line([pts[i], (c, c)], fill=(232, 200, 114, 120), width=4)
    for i, (x, y) in enumerate(pts):
        col = colors[i]
        d.ellipse([x - 52, y - 52, x + 52, y + 52], fill=(18, 20, 32, 255), outline=col + (255,), width=12)
        d.ellipse([x - 22, y - 22, x + 22, y + 22], fill=col + (255,))
    d.regular_polygon((c, c, 120), 4, rotation=45, fill=(18, 20, 32, 255), outline=gold, width=14)
    d.regular_polygon((c, c, 55), 4, rotation=45, fill=gold)
    mask = Image.new("L", img.size, 0)
    ImageDraw.Draw(mask).ellipse([20, 20, 2 * size - 20, 2 * size - 20], fill=255)
    clipped = Image.new("RGBA", img.size, (0, 0, 0, 0))
    clipped.paste(img, (0, 0), mask)
    return clipped.resize((size, size), Image.LANCZOS)


if __name__ == "__main__":
    gui()
    golem()
    item_icon("remnant_tome", ((92, 40, 120, 255), (232, 200, 114, 255), (240, 232, 210, 255), (111, 231, 255, 255)), tome)
    item_icon("tome_of_unbinding", ((40, 44, 60, 255), (150, 150, 170, 255), (220, 215, 200, 255), (255, 106, 90, 255)), tome)
    big = logo()
    big.save(os.path.join(OUT, "..", "..", "logo.png"))
    big.resize((128, 128), Image.LANCZOS).save(os.path.join(OUT, "textures", "gui", "logo.png"))
    print("textures written to", OUT)
