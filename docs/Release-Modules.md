This is a guide for maintainers to explain how the [CI/CD pipeline](http://jenkins.terasology.io/teraorg/job/Terasology/job/Modules/) for modules in the [Terasology :octocat: ](https://github.com/Tearsology) organization works, and what steps should be taken to release a new version of a module. 

## CI/CD Setup

Modules in the [Terasology :octocat: ](https://github.com/Tearsology) organization follow a **single-branch setup** with `develop`. Commits to this branch are automatically built and published to our [Artifactory], either as a release or as snapshots, depending on the version specified in `module.txt`.

### Branches

- **`develop`** • central place for _active development_
  > All active development happens on this branch. PRs SHOULD be directed against the `develop` branch. This branch is to be considered **unstable** and may hold experimental features and/or even fail compilation during active development. During development, a module MAY depend on unreleased modules. 
  > In the current state, modules only evolve forward and are not tied to a support or maintenance window. There is only a single track for releases (e.g., no bug fixes or patches on older releases).
  > Releases of modules SHOULD be tagged commits directly in the history of the main `develop` branch. Release commits SHOULD NOT contain dependencies to unreleased modules (e.g., snapshot releases).

PRs against modules are built and checked by the pipeline, but not published to [Artifactory].

### Versioning

Modules SHOULD be versioned according to [Semantic Versioning 2.0.0 (SemVer)](https://semver.org/). The module version is defined in the _version_ entry in `module.txt`.

Builds from `develop` directly use the version as defined in `module.txt`. Note, that there is only one snapshot build for a specific base version (e.g., `1.2.3-SNAPSHOT`) and subsequent snapshot builds will just override the artifact (read more about [What exactly is a Maven Snapshot and why do we need it?](https://stackoverflow.com/questions/5901378/what-exactly-is-a-maven-snapshot-and-why-do-we-need-it) on StackOverflow).
If the module was already released with a specific version (e.g., there was no version bump after the last release), publishing it to [Artifactory] again will fail and only the existing release will be available.

## Module Release Process

Releasing a module involves a couple of manual steps bump the version accordingly.

1. **Decide on release version** • Make sure `develop` has the intended next version in `module.txt`

    _The version number MUST be a higher SemVer than the last release commit. The version bump SHOULD follow SemVer specifications, e.g., increase the major version for breaking changes, or do a patch release if only internal behavior was fixed. You can [compare commits or branches](https://docs.github.com/en/github/committing-changes-to-your-project/comparing-commits) on Github easily to get an overview of the changes:_
    ```
    https://github.com/Terasology/{{module}}/compare/{{latest_release_tag}}...develop
    ```

    _Make sure that your local state of `develop` is up to date:_

    ```sh
    git checkout develop
    git pull
    ```

    _Change the version by updating it in `module.txt` and commit the change with the following message:_

    > `release: version {{version}}`

1. **Push `develop` to upstream** • This triggers the actual release!    
    
    ```sh
    git push origin develop
    ```

    Until we have automatic tagging or a tag-based release process it is recommended to manually [create and push an annotated tag][git-tag] for the respective version on `master`. For a module release v1.2.3 the tag process is:

    ```sh
    git tag -a v1.2.3 -m "Release version 1.2.3"
    git push origin v1.2.3
    ```

1. **Prepare for next release** • Bump version

    _We have to increase the version number on `develop` to be able to get pre-release `-SNAPSHOT` builds for any progress on the development branch. Therefore, the version number MUST be a higher SemVer than the version just released on `master`. This will typically be a **minor** version bump. To do this, just update the version in `module.txt` and commit the change with the following message:_

    > `chore: prepare snapshot builds for {{version}}`

    _Finally, push the preparation commit and you are done!_

    ```sh
    git push origin develop
    ```

## How Publishing Works

The builds steps for modules are defined in the [Jenkinsfile](https://github.com/MovingBlocks/ModuleJteConfig/blob/master/Jenkinsfile) of the [ModuleJteConfig repository](https://github.com/MovingBlocks/ModuleJteConfig), which are similar to the engine steps with the [JTE](https://plugins.jenkins.io/templating-engine/) repository just being a way to apply that build setup across _all_ module repos, without having to maintain individual files.

The major difference is that much like not having their own `Jenkinsfile` module repos also skip all the other build-related files, in favor of having Jenkins copy them in at build time (a sort of "build harness"). Modules are built in a standalone directory (no engine source), which has some pros and cons. You can replicate a standalone module build locally by copying the module to its own home then copying in the files indicated in the `Jenkinsfile` (Gradle stuff and such)

In a regular engine workspace locally our utility scripting simply drops in a template `build.gradle` to make the module part of the project tree, then the Gradle files elsewhere in the workspace work normally.

At the conclusion of a Gradle build if the publish step is invoked Gradle will try to publish binaries to a given target _artifact repository_ in Artifactory, so a full release goes to a release repo and a snapshot build goes to a snapshot repo. Jenkins defines a credential to grant access to do the publish against Artifactory, which is loaded in the `Jenkinsfile`

## Publish an Artifact Manually or for Testing

You can do a publish of binaries from a local system without Jenkins being involved at all, as well as redirect where the published artifacts go if necessary.

With a local _engine_ workspace you should get a template `gradle.properties` placed in the root directory with a series of related fields.

```
# Alternative resolution repo to use - this can be used to ignore "live" artifacts and only accept experimental ones.
# alternativeResolutionRepo=http://artifactory.terasology.org/artifactory/virtual-nanoware-and-remote

# Where to publish artifacts, if not to the default snapshot/release repos
# Publishing a snapshot to a release-only repo will get an intended but quirky HTTP 409 Conflict error as of Nov 2014
# publishRepo=nanoware-snapshot-local

# Credentials for publishing to Artifactory. Good for local testing
# mavenUser=
# mavenPass=
```

If set, these properties will determine how Gradle resolves artifacts, where binaries are published, and what credential to use. If you're just trying to publish from a local workspace (engine or module) you should simply acquire and fill in the username and password fields, leaving the rest still commented out. The `gradlew publish` task should then succeed.

If trying to solely test the publishing process _itself_ you can redirect where artifacts go by uncommenting the `publishRepo` property, in this case that would publish to a Nanoware (test) repo instead of the Terasology (live) repo, which means the published binaries will **not** be resolved with normal config. To test if such binaries do resolve you can uncomment the `alternativeResolutionRepo` property, keeping in mind that most things aren't published there in the first place so other dependencies may become unavailable.

There is a whole separate line of [Nanoware](http://jenkins.terasology.io/teraorg/job/Nanoware/) build jobs available in Jenkins that use the same two properties (defined as environment variables on the Nanoware _Folder_ object in Jenkins) so you can test multi-stage builds that rely on released artifacts from earlier phases, all without ever exposing any released artifacts to the "live" environment.

<!-- References -->

[artifactory]: http://artifactory.terasology.org
[git-tag]: https://www.atlassian.com/git/tutorials/inspecting-a-repository/git-tag
[linear-git-history]: https://www.bitsnbites.eu/a-tidy-linear-git-history/
