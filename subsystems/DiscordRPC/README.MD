# DiscordRPC Subsystem

The Discord RPC subsystem (rich presence via IPC) allows to set and update the status message in Discord for Terasology.
When this system is active and the player has the Discord desktop client open the user status will be shown as "Playing Terasology" (or something like that).

The subsystem uses https://github.com/jagrosh/DiscordIPC (`com.jagrosh:DiscordIPC`) to communicate with the Discord client.

## Requirements

This subsystem only has an effect if the user has the [Discord Desktop client](https://discord.com/) and is logged in.
