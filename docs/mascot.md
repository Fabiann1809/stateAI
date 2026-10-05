# Mascota

La mascota es una llama turquesa. Sus cinco expresiones vienen de los SVG del diseño (área de
256 x 256) y se importan como VectorDrawables con `scripts/mascot_to_vector.py`. No se redibuja nada:
los círculos, elipses y rectángulos se convierten en trazados equivalentes, y las rotaciones y escalas
del SVG en elementos `<group>`.

```sh
python scripts/mascot_to_vector.py <carpeta con reposo.svg, escuchando.svg, ...>
```

Cada expresión se divide en tres capas que comparten el área y se superponen exactamente, para que la
punta pueda balancearse por su cuenta:

| Capa | Grupos del SVG | Drawable |
|---|---|---|
| Cuerpo | `cuerpo` | `mascot_<expression>_body` |
| Punta | `punta` (pivote 128, 100) | `mascot_<expression>_tip` |
| Cara | `reflejo`, `mejillas`, `ojos`, `parpados`, `brillos`, `boca` | `mascot_<expression>_face` |

| Nombre en el diseño | Nombre en el código | Uso |
|---|---|---|
| Reposo | `rest` | Pantalla de Inicio, en espera |
| Escuchando | `listening` | Micrófono abierto |
| Pensando | `thinking` | Interpretando lo que se dijo |
| Contenta | `happy` | Sesión confirmada |
| Cansada | `tired` | Importada, todavía no se usa |

La mascota nunca refleja la energía (para eso está la silueta); solo cambia de expresión.
