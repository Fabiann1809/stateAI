# Mascota

Lumi es un pequeño robot celeste con una pantalla oscura como cara. Se dibuja directamente en un
`Canvas` (`Mascot.kt`) a partir de los SVG del rediseño (área de 256 x 256): cuerpo, sombra, reflejo,
orejas, brazos, pantalla y cara. Los trazados de `LumiLook.kt` son los del diseño, sin redibujar nada.

Lo que cambia entre expresiones es el ángulo de los brazos y de las orejas, los ojos, la boca, las
mejillas y las chispas:

| Nombre en el diseño | Nombre en el código | Brazos | Ojos | Boca | Uso |
|---|---|---|---|---|---|
| Tranquilo | `REST` | rectos | píldoras | sonrisa suave | Pantalla de Inicio, en espera |
| Sorprendido | `LISTENING` | abiertos | píldoras altas | círculo | Micrófono abierto |
| Concentrado | `THINKING` | algo cerrados | rasgados | línea | Interpretando lo que se dijo |
| Feliz | `HAPPY` | alzados | píldoras, con mejillas | sonrisa grande | Sesión confirmada, descanso |
| Cansado | `TIRED` | caídos, orejas bajas | arcos cerrados | línea corta | Dibujada, todavía no se usa |

`AnimatedMascot` parpadea de vez en cuando y respira despacio; no se mueve en modo ambiente ni con
"reducir movimiento". La mascota nunca refleja la energía (para eso está la silueta); solo cambia de
expresión.
