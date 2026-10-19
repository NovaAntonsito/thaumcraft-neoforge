"""Builds textures/models/thaumonomicon_book.png and thaumonomicon_cheat_book.png in the layout of the vanilla BookModel (64x32).

The covers are the Thaumonomicon item's own cover, shrunk to each lid; the pages are cream with faint ink lines.
Run from the repo root: python3 tools/gen_thaumonomicon_book.py
"""
import random
from PIL import Image

A = 'src/main/resources/assets/thaumcraft/textures/'

LEATHER_DARK = (38, 18, 8, 255)
SEAM = (27, 11, 3, 255)
GOLD = (196, 150, 52, 255)
PAGE = (232, 220, 181, 255)
PAGE_EDGE = (201, 185, 138, 255)
INK = (150, 128, 96, 255)


def fill(x0, y0, x1, y1, c):
    for x in range(x0, x1):
        for y in range(y0, y1):
            out.putpixel((x, y), c)


def leather(x0, y0, x1, y1):
    for x in range(x0, x1):
        for y in range(y0, y1):
            d = rnd.randint(-6, 6)
            out.putpixel((x, y), tuple(max(0, min(255, v + d)) for v in LEATHER_DARK[:3]) + (255,))



def build(item_name, out_name):
    global out, rnd
    item = Image.open(A + 'items/' + item_name + '.png').convert('RGBA')
    cover = item.crop((4, 2, 29, 29))  # the leather part of the 32x32 item
    out = Image.new('RGBA', (64, 32), (0, 0, 0, 0))
    rnd = random.Random(1)
    # Lids (6x10 faces, no depth): left lid front 0-6 back 6-12, seam 12-14 / 14-16, right lid front 16-22 back 22-28.
    lid = cover.resize((6, 10), Image.NEAREST)
    out.paste(lid, (0, 0))
    out.paste(lid.transpose(Image.FLIP_LEFT_RIGHT), (16, 0))
    leather(6, 0, 12, 10)
    leather(22, 0, 28, 10)
    fill(12, 0, 16, 10, SEAM)
    for y in (1, 4, 5, 8):
        out.putpixel((12, y), GOLD)
        out.putpixel((13, y), GOLD)

    # Page blocks (5x8x1): left at (0,10), right at (12,10). Top/bottom strips, then the four sides.
    for u in (0, 12):
        fill(u + 1, 10, u + 11, 11, PAGE_EDGE)
        fill(u, 11, u + 12, 19, PAGE_EDGE)
        fill(u + 1, 11, u + 6, 19, PAGE)
        fill(u + 7, 11, u + 12, 19, PAGE)
    # Flip pages (5x8, no depth) at (24,10): front 24-29, back 29-34.
    fill(24, 10, 34, 18, PAGE)

    # Faint lines of writing on every page face.
    for x0 in (1, 7, 13, 19, 24, 29):
        for y in range(12 if x0 < 24 else 11, 18 if x0 < 24 else 17, 2):
            for x in range(x0 + 1, x0 + 4):
                if rnd.random() < 0.8:
                    out.putpixel((x, y), INK)
    out.save(A + 'models/' + out_name + '.png')


build('thaumonomicon', 'thaumonomicon_book')
build('thaumonomicon_cheat', 'thaumonomicon_cheat_book')
