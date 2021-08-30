// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.commands

import org.terasology.cli.GooeyCLI
import picocli.AutoComplete
import picocli.CommandLine
import picocli.CommandLine.Command

@Command(name = "completion", description = [
        'Generate autocompletion for zsh and bash.',
        '''

## Use completion

1. Register completion.
2. Type './gw' then press <TAB><TAB> 
3. Choose need command via TAB (or arrows if you terminal and shell allows this)


## Register completion (Bash and ZSH, Current shell)

1. Generate with `./gw completion > gw_completion`
2. Register completion with `. ./gw_completion`

(will disappear after restart shell)

## Register completion permanently (ZSH)

Zsh can handle bash completions functions.
The latest development version of zsh has a function bashcompinit, that when run will allow zsh to read bash completion specifications and functions.\
The zshcompsys man page has details.

Since picocli 4.1, the generated completion script contains the following code to run bashcompinit after compinit; \
this will define complete and compgen functions corresponding to the bash builtins. \
It is no longer necessary to manually run the below commands.

autoload -U +X compinit && compinit
autoload -U +X bashcompinit && bashcompinit

## Register completion permanently (BASH)

### If `bash-completion` is installed (https://github.com/scop/bash-completion)
To install it more permanently, place the completion script file in /etc/bash_completion.d (or /usr/local/etc/bash_completion.d on a Mac).\
If bash-completion is installed, placing the completion script in either of these directories should be sufficient. \
(Source your ~/.bash_profile or launch a new terminal to start using this completion script.)

### If `bash-completion` is not installed
Alternatively, make a directory mkdir ~/bash_completion.d, and place the completion script in this directory.\
 Edit your ~/.bashrc file (or ~/.zshrc file on ZSH) and add the following:

for bcfile in ~/bash_completion.d/* ; do
  . $bcfile
done

All completion scripts in the ~/bash_completion.d directory will now be available every time you launch a new shell.

Source the generated completion script or launch a new terminal to start using this completion script.
'''])
class BashCompletionCommand extends BaseCommandType implements Runnable {

    public static final String COMMAND_NAME = "gw"

    @CommandLine.Spec CommandLine.Model.CommandSpec spec

    @Override
    void run() {
        String completionScript = AutoComplete.bash(COMMAND_NAME, new CommandLine(GooeyCLI.class))
        // not PrintWriter.println: scripts with Windows line separators fail in strange ways!
        spec.commandLine().getOut().print(completionScript)
        spec.commandLine().getOut().print('\n')
        spec.commandLine().getOut().flush()
    }
}
