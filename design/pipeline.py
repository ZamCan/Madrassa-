#!/usr/bin/env python3
"""
Madrassa - EduNoor :: Brand Asset Pipeline
==========================================

Regenerates every brand asset in `app/src/main/res/` from the master
sources in `design/assets-src/`:

  pattern_tile.png     seamless cream girih tile (theme background)
  artifacts_black.png  gold Islamic line-art on black (screen-blended)
  icon_fullbleed.png   1024px full-bleed app icon master
                       (emerald glass tile + gold rim + MY MADRASSA emblem)
  books_trio.png       gold open-Qur'an + flanking books emblem on black
                       (landing ornament, converted to translucent alpha)
  solo_*.png           Solo Learning step icons (gold on black -> alpha)

Outputs:
  mipmap-*/ic_launcher_fg.png      adaptive-icon foreground (108..432 px)
  mipmap-xxxhdpi/ic_launcher_mono.png  monochrome (themed icon) layer
  drawable-nodpi/brand_icon.png    rounded in-app brand tile
  drawable-nodpi/edunoor_landing_bg.webp   pattern + gold artifacts
  drawable-nodpi/edunoor_splash_bg.webp    emerald gradient + gold artifacts
  drawable-nodpi/ornament_books.png        translucent gold books ornament

Usage:  python3 design/pipeline.py   (from repo root; requires Pillow)

Design language: see docs/architecture/BRAND_SYSTEM.md
"""

from pathlib import Path

from PIL import Image, ImageChops, ImageDraw, ImageOps

ROOT = Path(__file__).resolve().parent.parent
SRC = ROOT / "design" / "assets-src"
RES = ROOT / "app" / "src" / "main" / "res"

DENSITIES = {          # launcher legacy (48dp) and adaptive foreground (108dp)
    "mipmap-mdpi": (48, 108),
    "mipmap-hdpi": (72, 162),
    "mipmap-xhdpi": (96, 216),
    "mipmap-xxhdpi": (144, 324),
    "mipmap-xxxhdpi": (192, 432),
}

EMERALD_TOP = (21, 89, 65)      # #155941  splash gradient start
EMERALD_BOTTOM = (4, 21, 15)    # #04150F  splash gradient end
ARTIFACT_STRENGTH_SPLASH = 0.8  # gold glow intensity on splash
BRONZE = (146, 108, 50)         # #926C32  artifact ink on light surfaces


def load_masters():
    icon = Image.open(SRC / "icon_fullbleed.png").convert("RGB")
    tile = Image.open(SRC / "pattern_tile.png").convert("RGB")
    art = Image.open(SRC / "artifacts_black.png").convert("RGB")
    return icon, tile, art


# ---------------------------------------------------------------- icons ----

def rounded(image, radius_ratio):
    """Apply an anti-aliased rounded-corner mask (transparent outside)."""
    size = image.size[0]
    scale = 4
    big = size * scale
    mask = Image.new("L", (big, big), 0)
    ImageDraw.Draw(mask).rounded_rectangle(
        [0, 0, big - 1, big - 1], radius=int(big * radius_ratio), fill=255
    )
    mask = mask.resize((size, size), Image.LANCZOS)
    out = image.convert("RGBA")
    out.putalpha(mask)
    return out


def circular(image):
    size = image.size[0]
    scale = 4
    mask = Image.new("L", (size * scale, size * scale), 0)
    ImageDraw.Draw(mask).ellipse([0, 0, size * scale - 1, size * scale - 1], fill=255)
    mask = mask.resize((size, size), Image.LANCZOS)
    out = image.convert("RGBA")
    out.putalpha(mask)
    return out


def build_icons(icon):
    for bucket, (legacy, fg) in DENSITIES.items():
        d = RES / bucket
        d.mkdir(parents=True, exist_ok=True)

        # minSdk 26 -> every supported device resolves the adaptive
        # icon XML; only the foreground layer ships as bitmaps.
        # Foreground: master is already composed so the glass tile sits in
        # the central ~66% == the 72dp adaptive safe zone on a 108dp canvas.
        icon.resize((fg, fg), Image.LANCZOS).save(d / "ic_launcher_fg.png")

    # Monochrome / themed-icon layer: emblem silhouette as white-on-alpha.
    mono_src = icon.crop((int(1024 * 0.20), int(1024 * 0.22),
                          int(1024 * 0.80), int(1024 * 0.82)))
    lum = ImageOps.autocontrast(mono_src.convert("L"))
    alpha = lum.point(lambda v: max(0, min(255, int((v - 28) * 1.7))))
    mono = Image.new("RGBA", alpha.size, (255, 255, 255, 0))
    mono.putalpha(alpha)
    mono.resize((432, 432), Image.LANCZOS).save(
        RES / "mipmap-xxxhdpi" / "ic_launcher_mono.png"
    )

    # In-app rounded brand tile (header mark, splash emblem).
    brand = icon.crop((int(1024 * 0.165), int(1024 * 0.165),
                       int(1024 * 0.835), int(1024 * 0.835)))
    brand = brand.resize((360, 360), Image.LANCZOS)
    nodpi = RES / "drawable-nodpi"
    nodpi.mkdir(parents=True, exist_ok=True)
    rounded(brand, 0.18).save(nodpi / "brand_icon.png")


# ----------------------------------------------------------- backgrounds ----

def tile_to(tile, w, h):
    base = Image.new("RGB", (w, h))
    tw, th = tile.size
    for y in range(0, h, th):
        for x in range(0, w, tw):
            base.paste(tile, (x, y))
    return base


def build_landing_bg(tile, art):
    """Cream girih canvas with the gold artifacts sitting ON it.

    Pure screen-blending washes gold out on a light background, so the
    artifacts are drawn twice, like gilded linework:
      1. lerp base -> bronze where the artifact lines are  (visible ink)
      2. screen a soft glow pass                           (luminous halo)
    """
    w, h = 1080, 2400
    base = tile_to(tile, w, h)
    art_scaled = art.resize((w, h), Image.LANCZOS)

    lum = art_scaled.convert("L")
    lum_n = lum.point(lambda v: v / 255.0)

    ink_strength, glow_strength = 0.50, 0.38
    bronze = Image.new("RGB", (w, h), BRONZE)

    # 1. bronze ink: base darkened toward bronze along the linework
    inv = lum_n.point(lambda v: int(255 * (1 - v * ink_strength)))
    inked = ImageChops.multiply(base, inv.convert("RGB"))
    inked = Image.composite(
        Image.blend(base, bronze, 0.55), inked,
        lum.point(lambda v: int(v * ink_strength * 0.55))
    )

    # 2. soft gold glow on top
    glow = art_scaled.point(lambda v: int(v * glow_strength))
    merged = ImageChops.screen(inked, glow)

    merged.save(RES / "drawable-nodpi" / "edunoor_landing_bg.webp",
                quality=84, method=6)


def build_splash_bg(art):
    w, h = 1080, 2280
    grad = Image.linear_gradient("L").resize((w, h))          # 0 (top) .. 255
    top = Image.new("RGB", (w, h), EMERALD_TOP)
    bottom = Image.new("RGB", (w, h), EMERALD_BOTTOM)
    splash = Image.composite(bottom, top, grad)
    art_dim = art.resize((w, h), Image.LANCZOS).point(
        lambda v: int(v * ARTIFACT_STRENGTH_SPLASH)
    )
    merged = ImageChops.screen(splash, art_dim)
    merged.save(RES / "drawable-nodpi" / "edunoor_splash_bg.webp",
                quality=84, method=6)


def build_books_ornament():
    """Landing ornament: gold books trio as translucent gilded art.

    The master is gold linework on black; luminance becomes the alpha
    channel (black -> fully transparent, gold -> opaque) so the piece
    composites onto the cream canvas as clearly readable gilded
    line-art with a gentle sheen - present and defined, never a
    faint smudge and never competing with content.
    """
    art = Image.open(SRC / "books_trio.png").convert("L")
    bbox = art.point(lambda v: 255 if v > 24 else 0).getbbox()
    art = art.crop(bbox)

    out_w = 720
    out_h = round(art.size[1] * out_w / art.size[0])
    art = art.resize((out_w, out_h), Image.LANCZOS)

    # Placement is the dark emerald glass canvas, so the emblem uses
    # the bright EduNoor gold: line cores near-solid, glow falloff
    # soft - clearly visible, gently shining, never loud.
    gold = Image.new("RGBA", art.size, (214, 170, 92, 0))
    gold.putalpha(art.point(
        lambda v: min(240, int(v * 1.25 * 0.94))
    ))

    (RES / "drawable-nodpi").mkdir(parents=True, exist_ok=True)
    gold.save(RES / "drawable-nodpi" / "ornament_books.png")


def build_solo_assets():
    """Solo Learning step visuals: gold-on-black masters -> transparent
    gold-alpha PNGs that composite onto the emerald glass cards."""
    out_dir = RES / "drawable-nodpi"
    out_dir.mkdir(parents=True, exist_ok=True)
    for src in sorted(SRC.glob("solo_*.png")):
        art = Image.open(src).convert("L")
        bbox = art.point(lambda v: 255 if v > 24 else 0).getbbox()
        if bbox:
            art = art.crop(bbox)
        target = 560
        w, h = art.size
        if w >= h:
            art = art.resize((target, round(h * target / w)), Image.LANCZOS)
        else:
            art = art.resize((round(w * target / h), target), Image.LANCZOS)
        gold = Image.new("RGBA", art.size, (214, 170, 92, 0))
        gold.putalpha(art.point(lambda v: min(240, int(v * 1.25 * 0.94))))
        gold.save(out_dir / (src.stem + ".png"))
        print("  solo asset:", src.stem)


def main():
    icon, tile, art = load_masters()
    build_icons(icon)
    build_landing_bg(tile, art)
    build_splash_bg(art)
    build_books_ornament()
    build_solo_assets()
    print("Brand assets regenerated into", RES.relative_to(ROOT))


if __name__ == "__main__":
    main()
