# Fonts

Terasology uses [Google's Noto Font](https://www.google.com/get/noto/)
for most text rendering operations. The font was chosen, because:

* The font is [open source](https://github.com/googlei18n/noto-fonts)
(SIL Open Font License, Version 1.1)
* Supports many languages
* Supports symbols through [Noto Sans Symbols](https://www.google.com/get/noto/#sans-zsym)
* Supports emoji - currently not used though
* Supports italic and bold font styles
* Looks good

The font is sampled at different sizes and converted into 2D
textures using [BMFont 1.14
beta](http://www.angelcode.com/products/bmfont/).
The following Unicode groups are used:

* Latin + Latin Supplement (0000)
* Latin Extended A and B (0100)
* IPA Extensions (0250) and Spacing Modifier Letters - In sum,
everything in [0001-0300]
* Greek, Coptic, Cyrillic+Supplement (0370-0530)
* Greek Extended (1F00)
* Subscripts, superscripts (2070)
* Currency Symbols (20A0)
* Arrows (2190)
* Mathematical Operators (2200)
* Box Drawings (2500)
* Geometric Shapes (25A0)
* Miscellaneous Symbols (2600)

These characters are present in the regular/bold/italic/large font
textures. 

For Japanese language support, the Hiragana and Katakana font glyphs have been added from the corresponding font (CJK-jp).

* CJK Symbols and Punctuation (3000)
* Hiragana (3040)
* Katakana (30A0)

On top of that, the regular and regular large font textures
support the following Unicode characters from the corresponding Symbols
font:

* Enclosed Alphanumerics (only 2460-2474 and 24B6-24EB)
* Dingbats (2700)

The individual character codes can be used from Java code through char
constants in the classes in the ` org.terasology.unicode` package. A
similar constant collection has been made by the [Java UniCode Constants
(UCC) package by
CodeCop](http://blog.code-cop.org/2007/08/java-unicode-constants.html).

The `fnt` files have been manually merged to get the symbols characters
into the regular font. The raw files, BMFont installation files, etc.
can be found in TeraMisc:

https://github.com/MovingBlocks/TeraMisc/tree/master/fonts

Combined Unicode characters (larger than FFFF) are not supported.