Adding new languages to the game or helping out with translating single strings is easy and helpful as it allows players from all over the world to enjoy the game, unhindered by language barriers!

First off, to keep things nice and clean, we advise you to work on the translation using a **feature branch**:

- `git checkout develop`
- `git branch i18n/{{your-language}}`
- `git checkout i18n/{{your-language}}`

To create a new language file, copy [`menu_en.lang`](https://github.com/MovingBlocks/Terasology/blob/develop/engine/src/main/resources/assets/i18n/menu_en.lang) to a new file called `menu_xx.lang` in [`engine/src/main/resources/assets/i18n`](https://github.com/MovingBlocks/Terasology/blob/develop/engine/src/main/resources/assets/i18n/) - `xx` should be an [ISO 639-1](http://en.wikipedia.org/wiki/ISO_639-1) two-letter code for your language.
Next up, translate everything in `menu_xx.lang` to your target language!
Every now and then, check up on your in-progress translation - the game's language can be changed in the _Settings - Player_ menu.
Some possible issues that may occur are long strings breaking UI elements and special characters not being rendered properly.

When your translation is finished, add a tiny flag to represent the language in the settings!
To do this, download an appropriate 16x11 icon from the [famfamfam.com icon pack](http://www.famfamfam.com/lab/icons/flags/) (or create your own 16x11 icon) and place it inside [`engine/src/main/resources/assets/textures/ui/icons`](https://github.com/MovingBlocks/Terasology/tree/develop/engine/src/main/resources/assets/textures/ui/icons).
Rename it to `flag_xx.png`, `xx` being the two-letter code you've used before.

Submit your translation as a pull request.
See [Contributor Quick Start](Contributor-Quick-Start.md) and [Dealing with Forks](Dealing-with-Forks.md) for help.

And while you're at it, feel free to add yourself to [Credits.md](https://github.com/MovingBlocks/Terasology/blob/develop/docs/Credits.md) as a new contributor!
