## Introduction
At this stage this page is mostly a placeholder for renderer-related documentation that will be written. 

The renderer has undergone major refactoring over the 2015-2017 period, and is now _approaching_ the stage where A) we can map its components and what it does (see diagrams below for a starter) and B) we will be able to provide an API for modules developers to use, to improve, expand or replace the current renderer functionality. 

We expect to write documentation on these matters as soon as we have sufficiently stabilized the API and its underlying code. 

## Early Diagrams
These diagrams are the very first to have been created since the renderer was structured enough for us to map it. They provide an early understanding of the current state of the renderer.

* Render DAG - Nodes-only: https://sketchboard.me/XAE5jbOHEtPN#/ (easier version to follow, for an overview)
* Render DAG - Full: https://sketchboard.me/nAE1f4q2pDpd#/ (complete version, more details, more intricate)

## Debugging Rendering with RenderDoc

For easier debugging of rendering issues, you can use [RenderDoc](https://renderdoc.org/).
Please refer to RenderDoc's [Quick Start Documentation](https://renderdoc.org/docs/getting_started/quick_start.html) for the basic setup.

The recommended configuration for launching an application to capture a frame is the following:

![RenderDoc Configuration](https://cdn.discordapp.com/attachments/591982628476026891/850801080644403220/unknown.png)

When running Terasology from IntelliJ, this should display the java command used by IntelliJ to start Terasology.
This command can be used in RenderDoc to attach to the running application.