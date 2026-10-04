# Mascot

The mascot is a turquoise flame. Its five expressions come from the design SVGs (256 x 256 viewport)
and are imported as VectorDrawables by `scripts/mascot_to_vector.py`. Nothing is redrawn: circles,
ellipses and rects become equivalent paths, and SVG rotations and scales become `<group>`s.

```sh
python scripts/mascot_to_vector.py <folder with reposo.svg, escuchando.svg, ...>
```

Each expression is split in three layers that share the viewport and stack exactly, so the tip can
sway on its own:

| Layer | SVG groups | Drawable |
|---|---|---|
| Body | `cuerpo` | `mascot_<expression>_body` |
| Tip | `punta` (pivot 128, 100) | `mascot_<expression>_tip` |
| Face | `reflejo`, `mejillas`, `ojos`, `parpados`, `brillos`, `boca` | `mascot_<expression>_face` |

| Design name | Code name | Use |
|---|---|---|
| Reposo | `rest` | Home screen, waiting |
| Escuchando | `listening` | Microphone open |
| Pensando | `thinking` | Interpreting what was said |
| Contenta | `happy` | Session confirmed |
| Cansada | `tired` | Imported, not used yet |

The mascot never reflects energy (that is the silhouette); it only changes expression.
