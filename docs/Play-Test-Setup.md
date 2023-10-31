We try to run a monthly play test session and may also run some ad hoc whenever there's a reason to. This page covers the logistics in setting up the test server.

- [Overview](#overview)
- [Create the server](#create-the-server)
- [Expose the server](#expose-the-server)
- [Upload build](#upload-build)
  - [Unusual builds](#unusual-builds)
- [Adjust configuration](#adjust-configuration)
- [Start server](#start-server)
- [Adjust Log Output](#adjust-log-output)

## Overview

The involved phases will usually go like so:

* Create the server
* Expose the server (update DNS)
* Upload the desired build
  * Getting a desired build
* Adjust configuration
* Start server

Then we may rinse & repeat as necessary during a single play test to either adjust config or possible update to a newer build yet. In the end consider destroying the server again to clean up

## Create the server

At present we use DigitalOcean to host virtual machines, called droplets. There is a stored snapshot we can create a new droplet from to have everything needed for Terasology ready.

With access acquired to our DO account (ask any logistically minded contributor that looks like they might know what they're doing):

* Log in to [DO](https://cloud.digitalocean.com/projects/ef80fed5-434c-450c-848e-f76a6a38667a/resources?i=dae9f4) and make sure you're on the "Terasology" project
* Go to "Create Droplets"
* Go to the "Snapshots" tab
* Pick the `testserver.terasology.net...` entry 
* Click the "Select all" checkbox in the Authentication section to give all keys access
* Adjust the hostname to just say "testserver.terasology.net" then press "Create Droplet"!

Wait for the droplet to appear in the list, and consider copying its IP. Occasionally we've enabled IPv6 as well but not sure we ever use it or have even tested it. If you need it there should be a checkbox to enable it as well to check

## Expose the server

* With the working server up go to Networking - Domains - `terasology.net`
* Find the existing `A` entry for `testserver.terasology.net` and click More -> Edit Record
* Paste in the IP, or just click in the box a time or two and let auto-complete show you all the droplets then pick the right one
* Do the same for the `AAAA` record if you want to enable IPv6 (will only work if you added IPv6 networking during droplet creation)

Consider testing locally with `ipconfig -flushdns` or comparable if needed, then ping `testserver.terasology.net` and make sure the IP looks right

## Upload build

Use a tool like WinSCP or any other SSH / file transfer tool of your choice

* Make sure you have the desired build available locally and extracted (see [Release: Omega](Release-Omega.md) for instructions how to create a release)
* Open an `ssh` session for user "root" on the server and go to `/opt/terasology` if not already there
* `rm -rf server libs modules` to reset current state
* Copy the `libs` and `modules` from your local build to the server

That is usually enough. The executables change exceedingly rarely, as do the natives - but they do change every once in a while! That's often when it is a good idea to make a whole new snapshot so you again only need to worry about `libs` and `modules` to get back up to date.


### Unusual builds

Occasionally we might need to do unusual things for builds

* If the server crashes with an error sounding like natives are out of date simply replace `natives/` on the server with the same from a local Omega zip
* If you need to test a PR use the attached zip on a PR build from http://jenkins.terasology.io/teraorg/job/Terasology/job/engine/view/change-requests/ to update solely `libs/` on the server. Either leave the modules in place (if testing engine) or replace individual module jars in `modules/`
  * Module jars should be attached to their builds under http://jenkins.terasology.io/teraorg/job/Terasology/job/Modules and their `develop` branches are also [published to Artifactory](http://artifactory.terasology.org/artifactory/webapp/#/artifacts/browse/tree/General/terasology-snapshot-local/org/terasology/modules) during those builds
    * If you only see weird artifacts attached in Jenkins then simply rebuild the target branch/PR for the module to get new artifacts (changed 2020-11-20) - Artifactory should always have latest and historical module jars, but generally only for `develop` branches


## Adjust configuration

With a remote session open the `override.cfg` file at `/opt/terasology` and adjust the list of active modules and the target world. Easy enough. Typical modules to _usually_ enable

* CheatsForAll (to ease console cheating for connected players)
* ChrisVolume1OST because pretty music (and maybe even ChrisVolume2OST!)
* HumanoidCharacters so you can see other players as more than a floating cube - can be situational (don't use for Light & Shadow, for instance)

Likely not needed but if for some reason you need to vary the launch arguments you may need to edit one of the executables (memory settings, server facade ..)

## Start server

In your `ssh` session:

* `screen` to get a detachable sub-shell or whatever that may be called :-)
* `run_linux.sh`
* `CTRL-A` then `D` to detach from the session (so the server doesn't die if you disconnect your `ssh`)
* To reattach SSH back in then use `screen -r` (if there are multiple screens instructions will be logged)

After the play test is over you may or may not want to terminate the droplet server, based on who might need it in the near future. Does cost money to keep it running.

## Adjust Log Output

To adjust the logging configuration for the server update the `logback.xml` contained in `libs/Terasology.jar`. 
To patch the JAR file the `jar` tool is required (part of the JDK).

> If it is not installed on the server droplet, you may need to download `libs/Terasology.jar` and perform the steps locally.

Perform the following steps to extract, patch, and update the Logback configuration file:

* `jar xf Terasology.jar logback.xml` to extract the logback configuration from the JAR
* `vi logback.xml` (or any other text editor) to adjust the logback configuration (see [The logback manual](https://logback.qos.ch/manual/))
* `jar uf Terasology.jar logback.xml` to patch the JAR file with the updated configuration
* (`rm logback.xml` to clean up the extract configuration file as it is no longer needed)
