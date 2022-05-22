# Contributing to Terasology

If you would like to contribute code, documentation, or other assets you can do so through GitHub by forking the repository and sending a pull request (PR). You can also simply report issues (bugs), but please search for any previous reports first.

*When submitting code, please make every effort to follow existing conventions and style in order to keep the code as readable as possible.*

Read on for an overview and [check the wiki for more details](https://github.com/MovingBlocks/Terasology/wiki).
For questions please join us in our [forum] or [Discord].

## File an Issue

You can report bugs and feature requests to [GitHub Issues](https://github.com/MovingBlocks/Terasology/issues). As mentioned please look for a similar existing issue before submitting a new one.

For finding easy to do issues to start with look at the [Good First Issue](https://github.com/MovingBlocks/Terasology/labels/Good%20First%20Issue) issues.

We prefer questions and support requests be posted in the [forum] or [Discord].

__Please provide as much information as possible to help us solve problems and answer questions better!__

[forum]: https://forum.terasology.org/forum/
[Discord]: https://discord.gg/Terasology

## PR Title / Commit Message Guidelines

We try to follow the [conventional commits](https://www.conventionalcommits.org/en/v1.0.0-beta.2/) style for commit messages and pull request titles.
This leads to **more readable messages** that are easy to follow when looking through the **project history**.
But also, we use the git commit messages to **generate the Terasology change log**.

### Commit Message Format
Each commit message consists of a **header**, a **body** and a **footer**.  The header has a special
format that includes a **type**, a **scope** and a **subject**:

```
<type>(<scope>): <subject>
<BLANK LINE>
<body>
<BLANK LINE>
<footer>
```

The **header** is mandatory and the **scope** of the header is optional.

Any line of the commit message cannot be longer 100 characters! This allows the message to be easier
to read on GitHub as well as in various git tools.

The footer should contain a [closing reference to an issue](https://help.github.com/articles/closing-issues-via-commit-messages/) if any.

Samples: (even more [samples](https://github.com/angular/angular/commits/master))

```
doc(changelog): update changelog to beta.5
```
```
fix(release): need to depend on latest rxjs and zone.js

The version in our package.json gets copied to the one we publish, and users need the latest of these.
```

### Revert
If the commit reverts a previous commit, it should begin with `revert: `, followed by the header of the reverted commit. In the body it should say: `This reverts commit <hash>.`, where the hash is the SHA of the commit being reverted.

### Type
Must be one of the following:

* **build**: Changes that affect the build system or external dependencies (example scopes: gradle, git, idea)
* **ci**: Changes to our CI configuration files and scripts (example scopes: Jenkins)
* **doc**: Documentation only changes
* **feat**: A new feature
* **fix**: A bug fix for the game
* **perf**: A code change that improves performance of the game
* **refactor**: A code change that neither fixes a bug nor adds a feature
* **style**: Changes that do not affect the meaning of the code (white-space, formatting, copyright, etc)
* **test**: Adding missing tests or correcting existing tests

### Scope
The scope  is optional.
It should describe the area of the code base this change affects.
For instance, it may be the name of the package(s) affected (as perceived by the person reading the changelog generated from commit messages).
The package prefix `org.terasology` should be ommitted.

Samples:
```
feat(world): add JOML API to WorldProvider

fix(block/family): fix off-by-one error in block placement

build(gradle): migrate all scripts to Kotlin
```

### Subject
The subject contains a succinct description of the change:

* use the imperative, present tense: "change" not "changed" nor "changes"
* don't capitalize the first letter
* no dot (.) at the end

### Body
Just as in the **subject**, use the imperative, present tense: "change" not "changed" nor "changes".
The body should include the motivation for the change and contrast this with previous behavior.

### Footer
The footer should contain any information about **Breaking Changes** and is also the place to
reference GitHub issues that this commit **Closes**.

**Breaking Changes** should start with the word `BREAKING CHANGE:` with a space or two newlines. The rest of the commit message is then used for this.

A detailed explanation can be found in this [document][commit-message-format].

## License

Submissions must be licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).

If you are adding a new file it should have a header like this (automatically available as a template in IntelliJ):

```java
// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
 ```

## How to Submit a Pull Request

Pull requests are highly appreciated! Please follow the simple guidelines below.

1. Fork the repository to your personal GitHub account.
1. Create a topic branch for every separate change you make. The branch should have a short but explanatory name, such as "MakesGrassGrow".
1. Apply your changes, committing at logical breaks. Make sure your changes are well-tested.
1. If your code includes any strings shown to the user, make sure they're accessed through the translation system as opposed to hardcoded values.
1. Check any unit tests that cover the affected code and if at all possible include new ones for new code.
1. Push your branch to your personal account and [create a pull request](https://help.github.com/articles/using-pull-requests/).
1. Watch for comments or acceptance on your PR. The PR can be updated by just pushing to the original branch.

__Please note:__ if you want to change multiple things that don't depend on each other, make sure you create a new branch off the main develop branch before making more changes - that way we can take in each change separately.

## How to Merge a Pull Request

For trusted contributors with push access to our root repos you are welcome to help merge pull requests.

1. Consider what scope is suitable for the change being PRed.
  * **Trivial**: If it is a single typo fix in a comment or other *inactive text* it can be merged without further review.
  * **Patch**: Small bug fixes. Be careful: even a string value changed can have unexpected impact elsewhere if it gets used as a key somewhere, involved in our internationalization, etc. Consider any possible integrations (like our translation server) and test the change locally. Report any concerns in the PR or merge if all appears well.
  * **Minor**: If substantial code is being changed/added please review as able and ping relevant contributors with `@username` to bring their attention to the PR for review. Probably one additional person should review then either person can merge.
  * **Major**: If any involved code is likely to cause a backwards incompatibility of some sort the PR needs the most possible review as it would require a major release. Consider if it is possible to deprecate the old version of the feature and provide a bridge to the new version to restore backwards compatibility. Alternatively should the change be parked in a longer term branch waiting for the next major release?
1. React appropriately to the scope and test what seems appropriate locally. Remember to run local multiplayer if it seems likely the change could cause an effect there.
1. If any issues are found or concerns noted mention them in the PR and ping anybody appropriate with a `@username` mention.
1. If sufficient review has been done and no issues have been noted push the merged code to GitHub to auto-close the PR as merged.
1. Assign the PR and any completed issues to an appropriate milestone if available for later release handling.
