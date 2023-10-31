# How to use Record and Replay

## Overview

Record & Replay is a feature created with the intent to improve Terasology's Quality Assurance by recording some events of specific types during a singleplayer session and then replay them to see if everything was reproduced accordingly. This is especially useful when a recording is made in a game version and it is replayed on future game versions to check if something was broken.

## Recording a single-player session

To record a game, it is necessary to first have a created game. If there is no singleplayer game, create one normally by clicking the "Singleplayer" button.
Once there is at least one singleplayer game, click on the "Extras" button in the Main Menu, which will lead to another screen. On this new screen, click on the "Record" button, select a game to record and then click "load". The game will load normally as it would in a normal Singleplayer game, but once the game is loaded, some events will be recorded. Play the game normally, and when you wish to stop the recording, just exit to the main menu or exit the game. Once that's done, notice that a new folder called "recordings" was created, and inside it, there will be another folder with the name of the world that was recorded. In this folder, there will be the save files and also some files with recorded events and other parameters important for the replay.

## Replaying a single-player session

To replay a game, just click on the "Replay" button on the extras menu and then select a recording to load. Once the game is loaded, notice that you won't have control over your character, since it is not possible to normally send the events of the same type of the ones recorded during a recording. For example, since InputEvents are recorded, it is not possible for the player to send InputEvents during a replay. A few seconds after the replay is loaded, the actions made during a recording will begin to be replayed. Once every recorded event is replayed, you will have control over your character again.

## Important Information

* As of now, recording the game that uses a Module that implements new types of InputEvents that are not BindButtonEvent or BindAxisEvent may cause issues in the replay if said events are executed during a recording.
* It is known that sometimes "right click" causes some issues in a recording, especially if the button is held during a replay, which may result in a few blocks being placed on wrong locations during a replay.
