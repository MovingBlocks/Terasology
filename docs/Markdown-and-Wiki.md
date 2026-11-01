Markdown and Wiki
===============================

The GitHub wiki (and the per-project README) uses the [Markdown](http://daringfireball.net/projects/markdown/syntax) syntax

In addition to editing the wiki and README via the GitHub interface you can also use IDE plugins to do highlighting with the Markdown formatting and even edit the wiki via Git

IntelliJ Markdown
---------

To set up IntelliJ to support Markdown syntax highlighting and local preview follow these steps:

* Go to Settings / Plugins
* Browse repositories and search for "Markdown"
* Install the plugin using the button in the toolbar (don't just select the plugin and click Ok - nothing will happen)
* Under Settings / File Types look for "Markdown" (you may have to restart IntelliJ) and make sure `*.md` and `*.markdown` are registered patterns there
* Under Settings / Markdown be sure to enable "hard wraps" to use the right line break format for GitHub
* Restart IntelliJ - you should now have a "Text" and a "Preview" tab in bottom of the main edit area for Markdown files

Wiki via Git
---------

Every wiki hosted on GitHub is itself maintained in Git using [Gollum](https://github.com/github/gollum#readme). There are [instructions](https://github.com/MovingBlocks/Terasology/wiki/_access) available on how to clone a wiki repo

With a wiki cloned to a local directory you can use IntelliJ (or other editors) to then edit the files locally and do a single push to update a bunch of pages at once, even preview locally. Searching is also made easy.

An added advantage is that via Git you can enable some advanced features you cannot start with (although you can later edit them) through the web interface, like [sidebars, headers, and footers](https://github.com/github/gollum#sidebar-files)

We might make a nice automated IntelliJ setup later for wiki editing but it is really super easy to set up - just create a new project in the cloned directory and register the Git root (if IntelliJ doesn't figure that out for you)

Wiki Automation
---------

Since we can interact with the wiki using Git it should be possible to do something like the following:

* Keep game commands / hot keys / tools etc in definition files that also include help text and examples
* Parse out said information during `develop` builds and update relevant pages in the wiki via Git (separate repo, won't retrigger a build)
    * This could also be used by fan-sites to display the information in a different format (raw game info database of sorts)
* Keep a summary in the [README.markdown](https://github.com/MovingBlocks/Terasology#terasology) on the front page that's matched with the `master` branch and update that automatically in the `develop` branch (which is default and thus the front page)
