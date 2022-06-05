## Getting started
Make sure you have [Fabric Loader](https://fabricmc.net/) and [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api) installed.

Drop the mod jar into your mods folder on your server (Can be found on [Modrinth](https://modrinth.com/mod/discord4fabric) and [CurseForge](https://www.curseforge.com/minecraft/mc-mods/discord4fabric)).

Launch the server, wait for it to load, then stop the server.

A file named `discord4fabric.json` should now appear in your `config` folder inside your main server folder.

Change `token` to your bot token. More information [here](https://discord.com/developers/docs/topics/oauth2#bots).

Change `webhook` to your webhook URL. More information [here](https://discord.com/developers/docs/resources/webhook#create-webhook).

Change `guild_id` to your server ID.

Change `channel_id` to your text channel ID (where you want users to interact with the bot).

## Placeholders
Placeholders are a way to insert variables into customizable text messages

Syntax: `%category:name%`

## Player join/leave
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

### Credits
![Image](https://cdn.discordapp.com/attachments/959467102962610177/983032671229870100/unknown.png)

For more info: https://placeholders.pb4.eu/user/default-placeholders/#server

## Minecraft to Discord messages
`%d4f:message%`: The player's message. Example: `MS-DOS wasnt actually coded my Microsoft, but was actually bought`<sub>yes I know there's a typo</sub>

### Credits
![Image](https://cdn.discordapp.com/attachments/959467102962610177/983033944733777920/unknown.png)

For more info: https://placeholders.pb4.eu/user/default-placeholders/#player

## Discord name
See here: https://placeholders.pb4.eu/user/default-placeholders/#player

## Status
See here: https://placeholders.pb4.eu/user/default-placeholders/#server
