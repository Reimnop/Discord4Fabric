## Getting started
Make sure you have [Fabric Loader](https://fabricmc.net/) and [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api) installed.

Drop the mod jar into your mods folder on your server (Can be found on [Modrinth](https://modrinth.com/mod/discord4fabric) and [CurseForge](https://www.curseforge.com/minecraft/mc-mods/discord4fabric)).

Launch the server, wait for it to load, then stop the server.

A file named `discord4fabric.json` should now appear in your `config` folder inside your main server folder.

Change `token` to your bot token. More information [here](https://discord.com/developers/docs/topics/oauth2#bots).

Change `webhook` to your webhook URL (Optional). More information [here](https://discord.com/developers/docs/resources/webhook#create-webhook).

Change `guild_id` to your server ID.

Change `channel_id` to your text channel ID (where you want users to interact with the bot).

Note: This mod requires the `GUILD_MEMBERS` intent. Please make sure you have it enabled.

## Commands
(Require permission level 4/OP) `/discord4fabric reload`: Reload config from disk (will generate config if config file doesn't exist).

(Require permission level 4/OP) `/discord4fabric update`: Update config to latest version.

`/discord4fabric link`: Link account.

`/discord4fabric unlink`: Unlink an already linked account.

## Placeholders
Placeholders are a way to insert variables into customizable text messages

Syntax: `%category:name%`

## Player join/leave
`%df4:post_online%`: The amount of players online after the player has joined/left. Example: `42`

See here: https://placeholders.pb4.eu/user/default-placeholders/#player

## Player death
`%df4:reason%`: Death reason with player name. Example: `Steve fell from a high place`

For more info: https://placeholders.pb4.eu/user/default-placeholders/#player

## Advancement
`%df4:title%`: Advancement name. Example: `Serious Dedication`

`%d4f:description%`: Advancement description. Example: `Use a Netherite Ingot to upgrade a Hoe, and then reevaluate your life choices`

For more info: https://placeholders.pb4.eu/user/default-placeholders/#player

## Discord to Minecraft messages
`%d4f:fullname%`: Full name of user. Example: `Reimnop#3147`

`%d4f:nickname%`: Nickname of user. Example: `Reimnop`

`%d4f:discriminator%`: The 4-digit tag of the user. Example: `3147`

`%d4f:message%`: The message that the user sent. Example: `Why did you take the worm from the soup sock?`

### With reply
`%d4f:reply_fullname%`: Full name of user being replied to. Example: `Reimnop#3147`

`%d4f:reply_nickname%`: Nickname of user being replied to. Example: `Reimnop`

`%d4f:reply_discriminator%`: The 4-digit tag of the user being replied to. Example: `3147`

### Credits
![Image](https://cdn.discordapp.com/attachments/959467102962610177/983032671229870100/unknown.png)

For more info: https://placeholders.pb4.eu/user/default-placeholders/#server

## Minecraft to Discord messages
`%d4f:message%`: The player's message. Example: `MS-DOS wasnt actually coded my Microsoft, but was actually bought`<sub>yes I know there's a typo</sub>

### If there's no webhook
### Webhook to plain messages
`d4f:message`: The message after being processed by former config option.

`d4f:name`: The name after being processed by former config option.

For more info: https://placeholders.pb4.eu/user/default-placeholders/#player

### Credits
![Image](https://cdn.discordapp.com/attachments/959467102962610177/983033944733777920/unknown.png)

For more info: https://placeholders.pb4.eu/user/default-placeholders/#player

## Discord name
See here: https://placeholders.pb4.eu/user/default-placeholders/#player

## Discord ping
`%d4f:fullname%`: Full name of user. Example: `Reimnop#3147`

`%d4f:nickname%`: Nickname of user. Example: `Reimnop`

`%d4f:discriminator%`: The 4-digit tag of the user. Example: `3147`

## Status
See here: https://placeholders.pb4.eu/user/default-placeholders/#server

## Topic
See here: https://placeholders.pb4.eu/user/default-placeholders/#server

### Note
Channel topic update rate limit is absurdly high, please try not to set topic update interval to below 6000

## Custom events
Custom events is an extremely flexible and extensible feature of this mod. It allows for custom behavior defined in JSON (sending a message when a player join, run commands when someone gets an advancement, etc). However it is quite complicated and hard to understand. You can find more information about it [here](CUSTOM_EVENTS.md)
