## How it works

* webhook is configured on Discord server
* GitHub repo is configured to push html post updates to Discord webhook
* Discord webhook receives data and posts it as a message to the selected channel

  ![grafik](https://github.com/MovingBlocks/Terasology/assets/29981695/62d59bf6-4bcc-4a47-9001-8094778311b8)

## How to set up / update Discord webhook

1. Go to `Server Settings`
1. In the settings, go to `Integrations`
1. Select `Show Webhooks`
1. Create new webhook or edit existing one

   ![grafik](https://github.com/MovingBlocks/Terasology/assets/29981695/badc1a63-d0bc-4613-ad2d-af1921e6a7e2)
1. Copy the webhook URL

## How to set up / update GitHub webhook integration

1. Go to the desired repo's `Settings`
1. In the settings, go to `Webhooks`
1. Create new webhook integration or edit existing one

   ![grafik](https://github.com/MovingBlocks/Terasology/assets/29981695/db23dc5c-cac6-442e-97dd-2902d9a9f9bc)
1. Insert the webhook URL copied from the Discord settings into the "Payload URL" field
1. In the "Payload URL" field, append `/github` to the webhook URL
1. Ensure "Content type" field is set to `application/json`

_Note:_ Consider selecting individual events to avoid spamming the target channel on Discord with irrelevant messages.
