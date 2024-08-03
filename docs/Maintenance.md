## How to archive a module?

You moved the contents of a module into other modules and now the old one is empty?
You cleaned broken and deprecated code out of a module and now it's empty?
You found a module, that is not used anymore and won't be in the near future but is fully functional?
You found a module that wasn't tested for ages and is probably just broken?

Well maybe it's time to archive that module. 

### Prerequisites

The following steps are required before archiving:

* Remove the module name from any distro's `gradle.properties` file in the [`Index` module](https://github.com/Terasology/Index/tree/master/distros)
* Remove the module name and description from the [engine's module list](https://github.com/MovingBlocks/Terasology/blob/develop/docs/Modules.md)
* Remove the module dependency from any other module's `module.txt` file

### Archiving unused modules

Modules that are simply currently not in use (and won't be in the near future) are welcome to stay in the ["Terasology" GitHub organization](https://github.com/Terasology).

For these, you can simply (if you have the required permissions) go to the module's repository settings, scroll down to the very bottom and click on "Archive this repository". This will mark the repository as archived and read-only. Thus, if it gets traction again, 

### Archiving broken / deprecated modules

Modules that are broken or deprecated or whose contents were moved into other modules - to cut it short modules that "won't be coming back" - should be removed from the "Terasology" GitHub organization. For these modules, we have a dedicated ["Terasology-Archived" Github organization](https://github.com/Terasology-Archived).

To archive such a module, you can (if you have the required permissions) go to the module' repository settings, scroll down to the very bottom and click on "Transfer". This will open up a form asking for the new organization this repository should be moved to. Enter "Terasology-Archived" and confirm.

_Please note, that you need to have permissions to create repositories in "Terasology-Archived" to be able to do so._

## How to restart the Artifactory

Prerequisites:
* Access to Digital Ocean "Terasology" project
* Optionally: SSH key configured on the artifactory droplet

Instructions:
1. Log in to [Digital Ocean (DO)](https://cloud.digitalocean.com/projects/ef80fed5-434c-450c-848e-f76a6a38667a/resources?i=dae9f4) and make sure you're on the "Terasology" project
1. Click on the "artifactory.terasology.org" droplet
1. If you have an SSH key configured on the artifactory droplet follow the steps below, otherwise go to [cheat-restart](#cheat-restart)
1. Copy the ipv4 address
1. Open a terminal and use SSH to connect to the droplet: `ssh root@<ipv4>`
1. Optional: If there is an issue with the artifactory and you know what you're doing, try to investigate the issue
1. Restart the artifactory: `service artifactory restart`
1. End the SSH connection and log out of DO

### Cheat Restart

1. If not yet done, log in to [Digital Ocean (DO)](https://cloud.digitalocean.com/projects/ef80fed5-434c-450c-848e-f76a6a38667a/resources?i=dae9f4), make sure you're on the "Terasology" project and click on the "artifactory.terasology.org" droplet
1. In the droplet sub-navigation on the left, go to "Power"
1. Click on "Turn off" and wait until the droplet is successfully stopped
1. Click on "Turn on" and wait until the droplet is successfully running again

## How to fix an expired GitHub Action token

The [`project-autoadd`](https://github.com/MovingBlocks/Terasology/blob/develop/.github/workflows/project-autoadd.yml) GitHub action requires privileges on projects to do its work. For this, it uses a Personal Access Token (PAT) with project scope stored in the organization secrets as `PROJECT_GITHUB_TOKEN`.
This PAT may expire at some point in time leading to failures of the GitHub action due to missing privileges. To fix this, you'll need to perform the following steps. Please note, that you'll require administration privileges on the [`MovingBlocks`](https://github.com/MovingBlocks?type=source) organization to modify the organization secrets.

1. Go to your account's [Settings -> Developer settings -> Personal Access Tokens -> Tokens (classic)](https://github.com/settings/tokens)

   ![image](https://user-images.githubusercontent.com/29981695/201881307-9b6165b4-3477-47f7-91c3-306f1ed6f98d.png)

1. Generate a new classic token, give it a name, set an expiry date (recommended: 90 days) and enable the `project` scope (_Full control of projects_). Do not enable any other privileges, they are not needed.

   ![image](https://user-images.githubusercontent.com/29981695/201881921-3c97da31-0c09-412b-a601-40ac88f68a85.png)

1. **Note: Do not reload the resulting page before you created the org secret as you'll not be able to see the generated token again!**
   
   Go to the organization's [Settings -> Secrets -> Actions](https://github.com/organizations/MovingBlocks/settings/secrets/actions)
   If you cannot access the settings or secrets, you're probably not an organization admin. Please approach one of the maintainers on our [Discord Server](https://discord.gg/terasology) to support you.

   ![image](https://user-images.githubusercontent.com/29981695/201882840-e513c3be-0ea8-4626-8a28-6fd60aa47f2d.png)

1. Remove the existing `PROJECT_GITHUB_TOKEN` secret.

1. Create a new organization secret, name it `PROJECT_GITHUB_TOKEN`, leave the "Repository access" default and copy the token you generated earlier into the `Value` field

   ![image](https://user-images.githubusercontent.com/29981695/201883447-c3320bf2-d4e6-47e1-a405-0ddeba13b400.png)

1. Retrigger the last failed run of the `project-audoadd` GitHub action by selecting the run in the [Actions view](https://github.com/MovingBlocks/Terasology/actions/workflows/project-autoadd.yml) and clicking on "Re-run all jobs".
