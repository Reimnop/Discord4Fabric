## Getting started
Assuming you already have [everything else](USAGE.md) set up, open `d4f_custom_events.json` in the same folder as the config file in your favorite text editor

## Commands
`/discord4fabric reload_custom_events`: Reloads custom events

## Some concepts
- `Event`: Executes a list of actions if all of its `Constraint`s are satisfied
- `Action`: A command expressing what you want to do
- `Constraint`: A condition if the event should execute or not

## General structure of custom events
Custom events are defined in JSON format, make sure you are comfortable with that

Comments are **not supported**

```js
{
  "event_name": {
    "requires": [
      "constraint_1",
      "!negated_constraint_2" // this will be satisfied when negated_constraint_2 is not satisfied
    ],
    "actions": [
      {
        "id": "action_type",
        "value": "action_parameter"
      },
      {
        "id": "action_type2",
        "value": {
          "param1": "used when there are more than",
          "param2": "one parameter for the action type"
        } // and more...
      }
    ]
  },
  "event_name2": {
    // similar to above
  }
}
```

## Constraints
### `linked_account`: Satisfied when the player has a linked Discord account
This constraint adds the following placeholders:
- `%linked_account:id%`: The player's linked Discord account ID. Example: 349852642123448321
- `%linked_account:fullname%`: Full name of user. Example: Reimnop#3147
- `%linked_account:nickname%`: Nickname of user. Example: Reimnop
- `%linked_account:discriminator%`: The 4-digit tag of the user. Example: 3147

### `operator`: Satisfied when the player is a server operator
This constraint does not add any placeholder

## Events
- `player_join`: Triggers when a player joins the server (supports `linked_account` and `operator` constraints)
- `player_leave`: Triggers when a player leaves the server (supports `linked_account` and `operator` constraints)
- `server_start`: Triggers when the server starts (does not support any constraint)
- `server_stop`: Triggers when the server stops (does not support any constraint)
- `discord_message`: Triggers when a Discord message is sent in the configured channel (does not support any constraint; the `%d4f:message%` placeholder is available for this event, it returns the message content)
- `minecraft_message`: Triggers when a Minecraft message is sent (supports `linked_account` and `operator` constraints; the `%d4f:message%` placeholder is available for this event, it returns the message content)
- `advancement`: Triggers when a player achieves an advancement (supports `linked_account` and `operator` constraints; the `%d4f:title%` placeholder is available for this event, it returns the advancement title)

## Actions
### `run_command`: Runs a Minecraft command as Console
`value` is String. Example:
```js
{
  "id": "run_command",
  "value": "gamemode creative %player:name_unformatted%"
}
```
Will set a player's gamemode to Creative

### `send_discord_message`: Sends a Discord message in the specified channel
`value` is String. Example:
```js
{
  "id": "send_discord_message",
  "value": "Hello there, %linked_account:nickname%"
}
```
Will send a message on Discord with the content `Hello there, <linked account's nickname>`

### `send_minecraft_message`: Sends a Minecraft message
`value` is String. Example:
```js
{
  "id": "send_minecraft_message",
  "value": "Congratulations, %player:name%!"
}
```
Will send a message in-game with the content `Congratulations, <player name>!`

### `grant_role`: Grants a Discord user a role
`value` is Object. Example:
```js
{
  "id": "grant_role",
  "value": {
    "user": "%linked_account:id%",
    "role": "993535629453430835"
  }
 }
 ```
 Will grant the player's linked account the role with ID 993535629453430835
 
 ### `revoke_role`: Revokes a Discord user a role
`value` is Object. Example:
```js
{
  "id": "revoke_role",
  "value": {
    "user": "%linked_account:id%",
    "role": "993535629453430835"
  }
 }
 ```
 Will revoke the player's linked account the role with ID 993535629453430835
 
 ## Example custom events
 Grant a role to a user with linked account when they join the server and remove it when they leave
 
```js
{
  "player_join": {
    "requires": [
      "linked_account"
    ],
    "actions": [
      {
        "id": "grant_role",
        "value": {
          "user": "%linked_account:id%",
          "role": "993535629453430835"
        }
      }
    ]
  },
  "player_leave": {
    "requires": [
      "linked_account"
    ],
    "actions": [
      {
        "id": "revoke_role",
        "value": {
          "user": "%linked_account:id%",
          "role": "993535629453430835"
        }
      }
    ]
  }
}
```

_The `%d4f:pig%` placeholder is available for all events and constraints and it returns a random Technoblade quote. This is my tribute to the legend. Fly high Technoblade, you will be missed._
