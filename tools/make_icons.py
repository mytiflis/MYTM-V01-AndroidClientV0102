"""
Generate Android launcher icons for MYTM V01 from the user-supplied ICO.
Source (fixed): C:\\Users\\MevlutYILMAZ\\Downloads\\MYTM_V02.ico
Outputs: app/src/main/res/mipmap-*/ic_launcher(.png|_round.png|_foreground.png)
"""
import os
from PIL import Image

ICO = r"C:\Users\MevlutYILMAZ\Downloads\MYTM_V02.ico"
RES = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "app", "src", "main", "res")
RES = os.path.abspath(RES)

DENSITIES = {
    "mdpi": 1.0,
    "hdpi": 1.5,
    "xhdpi": 2.0,
    "xxhdpi": 3.0,
    "xxxhdpi": 4.0,
}


def load_source():
    im = Image.open(ICO)
    if getattr(im, "n_frames", 1) > 1:
        # pick the largest frame available
        best = None
        best_px = -1
        for i in range(im.n_frames):
            im.seek(i)
            frame = im.convert("RGBA")
            px = frame.width * frame.height
            if px > best_px:
                best_px = px
                best = frame
        im = best
    else:
        im = im.convert("RGBA")
    return im


def fit(src, size):
    return src.resize((size, size), Image.LANCZOS)


def circular(src, size):
    img = src.resize((size, size), Image.LANCZOS).convert("RGBA")
    mask = Image.new("L", (size, size), 0)
    from PIL import ImageDraw
    d = ImageDraw.Draw(mask)
    d.ellipse((0, 0, size - 1, size - 1), fill=255)
    out = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    out.paste(img, (0, 0), mask)
    return out


def foreground(src, size):
    canvas = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    inner = int(size * 0.62)  # keep inside adaptive safe zone
    logo = src.resize((inner, inner), Image.LANCZOS).convert("RGBA")
    off = (size - inner) // 2
    canvas.paste(logo, (off, off), logo)
    return canvas


def main():
    src = load_source()
    for dens, scale in DENSITIES.items():
        d = os.path.join(RES, "mipmap-" + dens)
        os.makedirs(d, exist_ok=True)
        legacy = int(48 * scale)
        fg = int(108 * scale)
        fit(src, legacy).save(os.path.join(d, "ic_launcher.png"))
        circular(src, legacy).save(os.path.join(d, "ic_launcher_round.png"))
        foreground(src, fg).save(os.path.join(d, "ic_launcher_foreground.png"))
        print("wrote", d, "legacy", legacy, "fg", fg)
    print("SOURCE_SIZE", src.width, src.height)


if __name__ == "__main__":
    main()
