This is a guide for maintainers to explain how the [CI/CD pipeline](http://jenkins.terasology.io/teraorg/job/Terasology/job/Omega/) for the Omega game bundle of [Terasology :octocat: ](https://github.com/MovingBlocks/Terasology) works, and what steps should be taken to release a new (stable) version of the game. 

# CI/CD Setup

The Terasology engine repository at [MovingBlocks/Terasology :octocat:](https://github.com/MovingBlocks/Terasology) follows a **two-branch setup** with `master` and `develop`.
Commits to these branches are automatically built and published to our [Artifactory], either as a release or as snapshots.
For more details, see our [Build-Setup](Build-Setup.md).

## Branches

- **`master`** • the _release_ branch 
  > In the current state, the engine only evolves forward and is not tied to a support or maintenance window. There is only a single track for releases (e.g., no bug fixes or patches on older releases). The release branch is infrequently updated with the latest state from the develop branch to form a new release. Release commits SHOULD NOT contain dependencies to unreleased modules (e.g., snapshot releases).
- **`develop`** • central place for _active development_
  > All active development happens on this branch. PRs SHOULD be directed against the `develop` branch. This branch is to be considered **unstable** and may hold experimental features and/or even fail compilation during active development. During development, the engine MAY depend on unreleased dependencies (e.g., SNAPSHOTs of libraries).

PRs against the engine are built and checked by the pipeline, but not published to [Artifactory].

## Versioning

The engine SHOULD be versioned according to [Semantic Versioning 2.0.0 (SemVer)](https://semver.org/). The engine version is defined in multiple places. Best to search for the current version number to find them - for instance one in `engine/`, one in `engine-tests/`.

> _State of August 8, 2021: The engine version is defined in the following files:_
> * _[Engine `module.txt`](https://github.com/MovingBlocks/Terasology/blob/develop/engine/src/main/resources/org/terasology/engine/module.txt)_
> * _[Engine Tests `module.txt`](https://github.com/MovingBlocks/Terasology/blob/develop/engine-tests/src/main/resources/org/terasology/unittest/module.txt)_
>
> _Please also search for other occurrences in case this has changed since._

In addition, the _maturity level_ for the overall project is tracked in `templates/version.txt`. It lists the maturity, e.g., "alpha", and has an _increment_ for the release. When we release the engine and bump to a new snapshot version the increment should also be bumped (for instance, bump from "alpha-18" to "alpha-19").

The version on `develop` MUST be appended with `-SNAPSHOT` for snapshot builds. Note, that there is only one snapshot build for a specific base version (e.g., `1.2.3-SNAPSHOT`) and subsequent snapshot builds will just override the artifact (read more about [What exactly is a Maven Snapshot and why do we need it?](https://stackoverflow.com/questions/5901378/what-exactly-is-a-maven-snapshot-and-why-do-we-need-it) on StackOverflow).
If the engine was already released with that version, publishing it to [Artifactory] again will fail and only the existing release will be available.

# Omega Release Process

**Note:** Building the Omega release bundle for Terasology will always include the latest versions of modules available at the point in time the build is triggered. This means that re-triggering an Omega build might result in a different result when modules have been built in the meantime, e.g., because of merging changes.

## Preview Builds

With every commit to the main branch of [MovingBlocks/Terasology](https://github.com/MovingBlocks/Terasology) both [an engine build](http://jenkins.terasology.io/teraorg/job/Terasology/job/engine/job/develop/) and, on success, [an Omega build](http://jenkins.terasology.io/teraorg/job/Terasology/job/Omega/job/develop/) are automatically triggered.

A manual preview build can be triggered on demand by just triggering the build job in Jenkins. This might be required to get a preview build with module changes even if the engine did not change.

* Log in at http://jenkins.terasology.io - uses GitHub OAuth
* Run the build for [teraorg/Terasology/Omega/develop](http://jenkins.terasology.io/teraorg/job/Terasology/job/Omega/job/develop/)
* After about 10-15 minutes the build should be finished with a new game release ([latest successful build](http://jenkins.terasology.io/teraorg/job/Terasology/job/Omega/job/develop/lastSuccessfulBuild/))

## (Stable) Release Builds

Releasing the engine (and with it, an Omega bundle) involves a couple of manual steps to merge changes into the _release branch_ and prepare the development branch for further contributions.

1. **Decide on release version** • Make sure `develop` has the intended next version in the respective files (see above):

    _The version number MUST be a higher SemVer than the current version of `master`. The version bump SHOULD follow SemVer specifications, e.g., increase the major version for breaking changes, or do a patch release if only internal behavior was fixed. You can [compare commits or branches](https://docs.github.com/en/github/committing-changes-to-your-project/comparing-commits) on Github easily like this:_
    ```
    https://github.com/MovingBlocks/Terasology/compare/master...develop
    ```

    _Make sure that your local state of `develop` is up to date:_

    ```sh
    git checkout develop
    git pull
    ```

    _If the version needs to be changed, just update it and commit the change with the following message:_

    > `chore: prepare release {{version}}`

1. **Merge `develop` into `master`** • This triggers the actual release!

    _The merge SHOULD happen with a **merge commit** to be able to identify when we merged to master, i.e., which commit is a release commit. 
    The final commit is done with `--no-ff` to prevent fast-forward merges and create a merge commit.
    Use the following commit message for this commit:_
    > `release: version {{version}}`
    
    
    ```sh
    git checkout master
    git pull
    git merge --no-ff develop
    git push origin master
    ```

    Until we have automatic tagging or a tag-based release process it is recommended to manually [create and push an annotated tag][git-tag] for the respective version on `master`. For a release v1.2.3 the tag process is:

    ```sh
    git tag -a v1.2.3 -m "Release version 1.2.3"
    git push origin v1.2.3
    ```

1. **Prepare for next release** • Reset `develop` and bump version

    > 🚧 Is resetting `develop` really necessary ❓

    _After creating a merge commit to merge the develop branch into the release branch now reset `develop` to the latest state of `master` - both branches are even and we can start fresh with the next development goals. To reset the development branch, run the following command that pushes the state of `master` to `develop`._

    ```sh
    git push origin master:develop
    ``` 

    _Finally, we have to increase the version number on `develop` to be able to get pre-release `-SNAPSHOT` builds for any progress on the development branch. Therefore, the version number MUST be a higher SemVer than the version just released on `master`. This will typically be a **minor** version bump. Don't forget to bump the version in `templates/version.txt` as well. Commit the change with the following message:_

    > `chore: prepare snapshot builds for {{version}}`


<!-- References -->

[artifactory]: http://artifactory.terasology.org
[git-tag]: https://www.atlassian.com/git/tutorials/inspecting-a-repository/git-tag
