A concept that is especially confusing to newcomers that do not have a lot of Git experience is Terasology's multi-repository workspace.
In the following, we try to shed some light on what "multi-repository workspace" means in the first place and how it affects your development workflow.

## The Workspace Root

Your Terasology workspace is created by cloning our [engine repository](https://github.com/MovingBlocks/Terasology) (see the first step in the [Contributor Quick Start](https://github.com/MovingBlocks/Terasology/wiki/Contributor-Quick-Start#set-up-your-terasology-development-workspace)). The directory this creates will be considered your workspace and whenever you'll be in this directory, you'll be in your workspace root.

The workspace root content structure equals what you can see in the engine repository. You'll find the same directories and root-level files in your workspace root that you will also find in the engine repository.

## The `modules` sub-directory

Directly after cloning the engine repository, you could enter the `modules` sub-directory and notice that it does not have any sub-directories itself. However, after following the Contributor Quick Start further, you'll eventually notice that new directories pop up in your `modules` subdirectory. These directories are the local representation of modules. Whenever you build and start Terasology from source, the modules you can see listed in the Advanced Game Setup are the once locally available in your `modules` sub-directory.

These module directories typically show up as soon as you run either of the following commands:
* `groovyw module init <distro>`, e.g. `groovyw module init iota`
* `groovyw module recurse <module>`, e.g. `groovyw module recurse JoshariasSurvival`
* `groovyw module get <module>`, e.g. `groovyw module get Health`

What these `groovyw module` commands do in the background is clone a single (in case of `get`) or multiple (in case of `init` and `recurse`) module repositories into your `modules` sub-directory. Each directory this creates in your `modules` sub-directory will contain the exact contents the respective module repository in the ["Terasology" GitHub organization](https://github.com/Terasology) does.

![One local workspace, two separate repos on GitHub](DevelopingModules.png)

The same is true for the `libraries` sub-directory, only with library repositories like [`gestalt`](https://github.com/MovingBlocks/gestalt) or [`TeraNui`](https://github.com/MovingBlocks/TeraNUI) residing in the [`MovingBlocks` GitHub organization](https://github.com/MovingBlocks) instead. However, in your basic Terasology contributions, you'll rarely have to care about this.

## What this means for your development workflow

Whenever you are in your workspace root or any of its sub-directories and execute `git` commands (e.g. `git switch`, `git commit`, `git push`), these commands will target the upstream engine repository which usually is either the original [Terasology engine repository](https://github.com/MovingBlocks/Terasology) or your fork of it. **This is true for all sub-directories except the sub-directories of `modules` and `libraries`**.

Whenever you are in a sub-directory of `modules` and `libraries`, the `git` commands you execute will target the respective upstream module or library repository. For instance, when you're in the `modules/Health` sub-directory, your `git` commands will either target the original [`Health` repository](https://github.com/Terasology/Health) or your fork of it.

![image](https://user-images.githubusercontent.com/29981695/211222153-5920d408-24a7-43cb-b68d-081a6f364209.png)
