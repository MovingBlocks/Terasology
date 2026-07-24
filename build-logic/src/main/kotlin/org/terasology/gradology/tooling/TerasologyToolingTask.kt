// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.gradology.tooling

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.api.tasks.options.OptionValues

/**
 * `gradle terasology --type=... --command=... --params=...` - a Gradle-native replacement for
 * `groovyw <type> <sub-command> <params...>` (module/meta/lib/facade retrieval and management). Not
 * cacheable/up-to-date-checked, same as e.g. RunTerasology: this always does whatever it's told to
 * (git clone/pull/etc.), same as running any other CLI.
 *
 * Deliberately all named `@Option`s (Gradle's real, native, documented mechanism for task CLI flags -
 * the same one backing `application`'s `run --args=` and the built-in `Test` task's `--tests=`), not
 * positional arguments: Gradle's CLI grammar has no "everything after this task name is free text for
 * it" concept, so an earlier version of this task relied on rewriting `gradle.startParameter.taskNames`
 * from settings.gradle.kts to fake that. That broke for any word coinciding with a task name anywhere
 * in this build - a real, demonstrated problem ("init" is Gradle's own reserved build-init task name,
 * which made Gradle's launcher skip includeBuild resolution entirely whenever it appeared as a raw
 * positional token) and a theoretical one (Gradle's camelCase task-name abbreviation matching over
 * this whole multi-project build - modules/facades/metas/libs, growing over time). Named options never
 * have this problem: none of their values are ever interpreted as a task name by Gradle at all - which
 * is why the sub-command vocabulary in [Cli] can freely use "init" again.
 *
 * [getTypeOptions]/[getCommandOptions] (via @OptionValues) also make `--type`/`--command` tab-completable
 * and give real Gradle-native validation/`gradle help --task terasology` documentation, neither of
 * which the old positional form had.
 */
open class TerasologyToolingTask : DefaultTask() {
    @get:Internal
    @set:Option(option = "type", description = "Type of thing to operate on")
    var type: String = ""

    @OptionValues("type")
    fun getTypeOptions(): List<String> = ItemType.byName.keys.sorted()

    @get:Internal
    @set:Option(option = "command", description = "Sub-command to run")
    var command: String = ""

    @OptionValues("command")
    fun getCommandOptions(): List<String> = Cli.subCommands

    @get:Internal
    var params: List<String> = emptyList()

    @get:Internal
    @set:Option(option = "usage", description = "Print usage information and exit")
    var usage: Boolean = false

    init {
        group = "terasology tooling"
        description = "Runs Terasology module/meta/lib/facade tooling (Kotlin port of groovyw). See --usage."
    }

    @Option(
        option = "params",
        description = "Remaining space-separated parameters for the sub-command " +
            "(item names, remote name/URL, distro name, flags like '-remote someRemote' or '-skip-recently-updated')"
    )
    fun setParams(value: String) {
        params = value.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    }

    @TaskAction
    fun run() {
        val args = if (usage) {
            listOf("usage")
        } else {
            listOfNotNull(type.ifEmpty { null }, command.ifEmpty { null }) + params
        }
        // Explicit, not relative File(...) paths: this task runs inside the Gradle daemon process,
        // whose JVM working directory is not the project directory.
        Cli.run(args, project.rootDir)
    }
}
