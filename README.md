# Haunted Items

A Fabric mod for Minecraft 1.21.1. **Your inventory is haunted.** Not often. Just enough that you start to wonder.

Every second, each player has a 1 in 2,400 chance of something happening (about once every 40 minutes of play). When it hits, one of these happens:

| Haunting | What you notice |
|---|---|
| Whisper | A quiet cave sound, then: *"Your Diamond Pickaxe whispers something you can't quite hear..."* |
| Swap | Two items in your inventory quietly trade places |
| Nudge | Your hand slides to a different hotbar slot on its own |
| Phantom pickup | You hear an item get picked up right behind you. You didn't pick anything up. |
| Footsteps | Stone footsteps creeping up behind you, getting closer |
| Chest | A chest opens and closes behind you. There's no chest. |
| Cold | *"Your Iron Sword feels ice cold."* |

Nothing is ever deleted, dropped, or damaged. Items only move around inside your own inventory. Players in creative or spectator mode are left alone, and only the haunted player hears the sounds.

## Install

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.21.1.
2. Put [Fabric API](https://modrinth.com/mod/fabric-api) in your `mods` folder.
3. Download `haunteditems-x.x.x.jar` from [**Releases**](../../releases) and put it in `mods`.

**Servers:** only the server needs it. Players can join with a normal client and still get haunted, because it only uses vanilla sounds.

## Commands

- `/haunt`: haunt yourself right now (needs op / cheats on)
- `/haunt <player>`: haunt someone else with a random haunting
- `/haunt <player> <haunting>`: pick which one (`whisper`, `swap`, `nudge`, `phantom_pickup`, `footsteps`, `chest`, `cold`)

## Config

`config/haunteditems.properties`

| Setting | Default | What it does |
|---|---|---|
| `oddsPerSecond` | `2400` | 1 in this many chance each second. `600` is about every 10 minutes, `36000` about every 10 hours. |
| `allowItemMoving` | `true` | Set to `false` to turn off Swap and Nudge so items never move. |

## Building

```
./gradlew build
```

The jar ends up in `build/libs/`.
