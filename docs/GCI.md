# Google Code-In

As with [Google Summer of Code](GSOC) we participate in [Google Code-In](https://developers.google.com/open-source/gci/) when able. We welcome students to contribute to Terasology while learning about open source :-)

GCI groups tasks for students into five categories. Below are some general guidance for what kinds of tasks may be encountered and how they relate to the open source way.

**BIG NOTE**: Terasology is _Alpha level software_ - expect bugs and crashes! Just about any task could potentially be blocked if a bug gets in the way. If you see something that doesn't seem right or makes a task unexpectedly difficult, reach out to a mentor or ask on [chat](https://github.com/MovingBlocks/Terasology/wiki/Using-IRC) to find out if you're on the right track or hitting a problem we may need to check on. Don't fret - ask! :-)

**Need help in finding an easy next task?**: Head over [here](https://github.com/MovingBlocks/Terasology/wiki/GCI#recommended-tasks).

**Want a nice one-page overview of the main stuff from setup through coding stuff and understanding our architecture?** See [this wiki page](https://github.com/casals/terasology-101/wiki/Terasology-Dev-101)

## Projects

### Terasology

Terasology has grown over the years and has hundreds of repos across different GitHub organizations at this point as well as multiple websites/services. We have a whole other game now and also consider options to be an umbrella org for smaller open source groups interested in GCI :-)

The game engine project and all our content modules remain our primary focus for GCI and make up most the tasks. There will be a lot of introductory tasks for base systems and research tasks on trying or testing something out and writing about the experience. Attempting to make some cool new content via other tasks is popular as well. Side tasks like looking at secondary projects/tool can be found as well.

### Destination Sol

Our second game is [DestSol](https://github.com/MovingBlocks/DestinationSol) - a simple and fun little 2D space shooter we adopted into the open source community from a team that used it as an experimental title to get on to Steam and Google Play (Android). Fly around in a variety of ships blasting enemies and asteroids with a bunch of different weapons. Then [make more of them](https://github.com/MovingBlocks/DestinationSol/wiki/Adding-content-to-Modules) as part of GCI tasks!

## Task Categories

For each project or sub project you will find a variety of types of tasks. GCI contains 5 primary categories although tasks may involve several at once. The below categories refer mostly to Terasology specific tasks but should give you an idea of what to inspect in general as well.

### Coding

Getting into the code itself might seem the most natural way to participate in an open source project, and it is often the most critical. After all, what is open source without _any_ source?

For these tasks you'll likely need a code workspace so you can run the game from source. The [quick start guide](Dev-Setup) is a good place to start, and there are several more resources in the wiki's sidebar. Alternatively, newcomers to the code will sometimes write about their experiences setting up - for instance, check out [this guide by Nihal Singh](http://nihal111.github.io/2016/11/22/Terasology-Getting-started.html)!

Recommended tasks to start with for coding are (TODO task link) "setting up to run from source" and then (TODO: task link) "creating a new block".

#### Structure Templates

One of the big content formats in Terasology involves writing files in JSON, of which Structure Templates (STs) feature prominently in GCI tasks. While the JSON file handles the config of the ST-based content it can usually be generated via in-game tool allowing you to mark an area you've built by hand and turn that into a big text file. Read about them more [in the forum](http://forum.terasology.org/threads/structure-templates.1550)

As part of fashioning ST-based content you may also need additional artwork or functionality requiring new or updated Java code.

### Documentation / Training

Sadly a lot of projects, both open source and proprietary, do not get to spend a sufficient amount of time and effort on documentation and training for users and contributors alike. We have a fair amount of documentation and guides, but definitely need more!

This area is especially important to open source as it is often the first thing you see if you are interested in contributing. Without great documentation and a friendly supporting community it is hard to learn.

Documentation can also be a way to get into code gently - by reading and trying to understand what something does, then documenting it better using Javadoc or small wiki guides. You can easily submit documentation updates as [pull requests](https://help.github.com/articles/about-pull-requests/) on GitHub, then clarify any points you are unsure of during the review phase. Wiki pages can be done first as drafts with a mentor or other community member reviewing at your request.

Beyond the game we maintain multiple websites and other services, and occasionally release videos on YouTube. Guides and training in video form is another nice-to-have area 🙂 

#### JavaDoc

Some tasks call for the creation or enhancement of code comments that become part of the source and something you can generate external documentation from. There are plenty of undocumented stuff to pick from *but* note that we often use multi-instance generic "Document something!" tasks that mean you could end up picking the same code to document as another student. Keep an eye out for any details in tasks about coordinating what you're working on to avoid overlap and wasted work.

You don't need to fully understand a whole system to be able to write JavaDoc for smaller pieces, but you do need to be able to do the detective work to figure out some of the parts. You can't just ask mentors what things do - but during review of the PR you can get your theories validated / corrected :-)

### Outreach / Research

Some projects are able to spread by pure word-of-mouth and/or by simply releasing their software on existing marketplaces. Plenty of others wouldn't be found easily without some significant effort by community members.

Terasology has spent most its time in self-imposed _stealth mode_ as we have been heavily focused on the architecture of our engine, finding ways to support content that would set our game apart in some novel fashion. However, we believe we have reached a point where the ability to make interesting new content has run far ahead of what actual content we have, and partly thanks to GSoC and GCI we are trying to come out of our shell to find new users and contributors.

As part of that process starting to reach out to more contributors and potential players is becoming more important, as is researching what is going on in our ecosystem these days.

#### Try something out, report on how it went

One type of task in this area is pretty simple: play a specific part of the game, then tell us what went well and what could be improved :-)

In addition to reporting back with details you can publish videos showing off large builds, howtos, showcases, or just about anything.

Better yet: find a friend or two and introduce them to the game for a little while in multiplayer, then record their reactions. Or if you set up for coding let us know how that process went, and then talk a friend through the same process to see how they experience it differently.

We rely on user reports to determine what we should work on, in what order. Since we've spent so much time just being developers around other developers we need your help! 

### Quality Assurance

Oh right, testing things! We should totally do that. Anybody? :D

There is an immense amount of testing technology and methodologies out there, and web applications in particular can apply heavily automated testing procedures, driving a browser to follow a script and validate that everything happens as expected.

Video games on the other hand are trickier. The automated tools still work at the code level and unit testing plus code analytics can help keep your code healthy. However outright testing how the game plays is much more difficult and usually a manual process. This is especially the case with all-volunteer open source where we might not have access to expensive tools.

Traditionally we've relied on our developers to check that their own code works in-game, but it is very easy to miss things as the game gets more complicated. Before we do a stable release we usually run through the very beginning of some of our basic content, but do not go very deep. It is time to get more serious about testing!

#### Test Plans

A large new field we are aiming to approach in part via GCI is running through manual test plans like how you would manually follow a script on a website, recording results. For web apps you can automate that part, but we have to rely on manual testing for now.

To formalize what we should be testing we need to first write actual test plans - a script you follow exactly inside the running game. For example:

* Run the game via executable
* Validate that the game appears and that music starts playing
* Go to the create new game screen and make sure you see `x` `y` and `z` UI elements on the screen
* Start a game with the default settings
* Validate that you load into a world and have items `a` `b` and `c` on your toolbar
* Place the chest from your toolbar on the ground
* Validate that the chest appears, that you can open it by `e` clicking it, and see that it contains more stuff

The above would make up a single "test case" - a series of them can be grouped into a "test suite" where you might test out several related features in one testing session. A test plan would cover a distinct area, such as the base game. Another test plan could aim to validate a particular series of modules instead of the base game, such as the JoshariasSurvival gameplay template.

An added difficulty is that thing about us being in **Alpha** - even trying to write a single test case may end up getting blocked if a bug gets in the way and breaks forward progress through a tech tree, for instance. In that case you may need to contact your mentor or ask on IRC, and work on other test cases in the meantime.

You may want to use [the latest stable release](https://github.com/MovingBlocks/Terasology/releases) to base your testing on rather than the current develop build, since it may be more unstable. If you still encounter issues you might want to try the other build to see if it works in either.

Additionally being new to the project you may not know _what_ to test, since we don't have any test plans yet! Individual tasks should give you pointers on what to look for, but you may also need to experiment in-game (sometimes there is a recipe browser), watch showcase videos, or even look at the code to find out what content exists. The game console can be very useful with commands like `listBlocks` or `search`. Finally you might be able to get a hold of individual authors to ask questions about how something is expected to work.

#### Module Testing Environment & Recordings

Beyond simple unit tests written as part of the code or manual test plans humans can work through we have [game environment based tests](http://forum.terasology.org/threads/moduletestingenvironment-integration-testing-with-in-process-engine-objects.1884) that can automatically validate in-game behavior with both the server and connected clients being "headless" - not attached to a monitor or a keyboard/mouse.

Those kinds of tests can be incredibly valuable as it allows us a good degree of confidence that actual gameplay should be working as we change things. Some tasks will ask for these kinds of "advanced" unit tests and are hugely appreciated!

Even newer than the MTE is a [Record & Replay system developed during GSOC 2018](https://forum.terasology.org/threads/record-and-replay-feature.2183) - this is akin to the MTE but works by having a player record a play session in-game, then add assertions at specific times that a condition was present.

### Design (old User Interface)

Renamed from plain "User Interface" for GCI 2018 this category now includes everything "Design" ... which is what, really? :-)

We're not actually sure! It still could be considered to involve UI, but maybe now moreso also User Experience (UX) or even game design elements like writing specifications for new gameplay. We doubt it involves developing the latest fashion for use on a catwalk, but technically you _could_ implement that in a new gameplay template!

While even UI work alone could be considered a subset of coding it can call for a subtly different skillset from traditional programming. At the UI level you get into ["User Experience Design" or UX](https://en.wikipedia.org/wiki/User_experience_design), which in a way is to the game as the documentation is to the project: you need to make sure it looks and feels right on the first impression!

We have our own UI framework with the cool extra that you can edit the game's UI from inside the game itself, seeing the UI elements update in real time. We also have a [tutorial for the framework and editor](https://github.com/Terasology/TutorialNui).

What we don't really have is a broad selection of users from different backgrounds that have tried out the game and let us know what part of the UI works well and which parts could use some work. Sometimes making adjustments or even trying out a few different approaches is easy enough where you can try it out and share the experience with a friend or two.

## Recommended Tasks

Here's a list of easy, suggested and easily approachable tasks that can be completed with little to no knowledge of programming. If you find yourself stuck after having completed two beginner tasks or don't know which task to choose next, this list is for you!

### Beginner tasks

* [Set up a local workspace and run Terasology from source](https://codein.withgoogle.com/dashboard/tasks/5667069010378752/)
* [DestSol - Set up a local workspace and run Destination Sol from source](https://codein.withgoogle.com/dashboard/tasks/5667069010378752/)
* [World Generation Chain (I) : Make a Basic World Generator](https://codein.withgoogle.com/dashboard/tasks/6630038057779200/)
* [Set up a Terasology server, provide usability feedback](https://codein.withgoogle.com/dashboard/tasks/6456084567425024/)
* [Create your owh GitHub module repository](https://codein.withgoogle.com/dashboard/tasks/5980461365460992/)
* [Research events for theming DestSol](https://codein.withgoogle.com/dashboard/tasks/5534877064101888/)

### Intermediate tasks

* [Behavior Chain (I): Create behaviors for characters](https://codein.withgoogle.com/dashboard/tasks/5656125752475648/)
* [Tutorial ECS: Learn how Terasology works and write about it!](https://codein.withgoogle.com/dashboard/tasks/6507050293526528/)
* [Write unit tests for the TeraMath project](https://codein.withgoogle.com/dashboard/tasks/4824882966167552/)
* [DestSol: Make your own faction!](https://codein.withgoogle.com/dashboard/tasks/4965113916817408/)


### Content focus areas

Terasology has a series of gameplay templates that provide a content base for playing the game in a particular way. Tasks in these areas will tend to be both useful and fairly easy. Here are some introductions to a few such areas.

#### Master of Oreon

Also known as "MOO" this setting has the player serving as a care taker for some "Oreon" creatures that can perform a variety of tasks like constructing buildings, farming, practicing their skills, researching new abilities, and so on. This is our main take on gameplay like that found in Dwarf Fortress or Dungeon Keeper and it was [heavily updated as part of GSOC 2018](https://forum.terasology.org/threads/master-of-oreon.1015/post-16075) now needing further expansion on available tasks, structures, food, behavior, etc.

Here are some of the tasks related to MOO:

* [Create a new building of your choice!](https://codein.withgoogle.com/dashboard/tasks/6465370823589888/)
* [Create new crops for planting (I)](https://codein.withgoogle.com/dashboard/tasks/5057108660191232/)