![CyberWorldReset](https://i.imgur.com/pufhj51.png)

# CyberWorldReset Standard

Reset or restore worlds on a schedule without restarting the server. Players are warned before the reset, moved to a safe world while it runs, and sent back once the new world is ready.

| | |
|---|---|
| **Version** | 0.8.1 |
| **Minecraft** | 1.8 - 26.3 |
| **Servers** | Spigot, Paper and their forks |
| **Java** | 8 or newer |
| **Wiki** | [wiki.bitaspire.com/en/docs/cyberworldreset](https://wiki.bitaspire.com/en/docs/cyberworldreset) |
| **Support** | [Discord](https://discord.gg/invite/DC4Gqj3y5V) |

Minecraft versions are detected through [VNC](https://github.com/CroaBeast/VNC), so both classic (`1.21.4`) and year-based (`26.3`) releases are recognized.

## Features

- **Manual and scheduled resets** - reset from a command or with timers: a fixed date, a repeating interval (`15m`, `2h`, `3d`, `2M`...), or both.
- **Seed control** - keep the current seed, set a fixed one, or roll a random seed on every reset.
- **Rollback** - save a world with `/cwr save` and restore that copy instead of generating a new one.
- **Safe world** - players in the world are teleported to a safe world during the reset and back after a delay, or kicked if it is disabled.
- **Warnings** - chat messages and titles at any number of seconds before the reset.
- **Reset commands** - run commands for every player, for players in a world, or once from console when the reset finishes.
- **Environment and generator** - reset into `NORMAL`, `NETHER` or `THE_END`, or through a custom generator such as Iris, Terra or TerraformGenerator.
- **Chunk loading** - loads the chunks around spawn after the reset, with a configurable radius.
- **In-game editing** - change every world setup with `/cwr edit` instead of touching `worlds.yml`.
- **PlaceholderAPI** - reset status, remaining time, chunk progress and TPS.
- **Localization** - every message lives in `lang.yml`; official translations are on the wiki.

## Commands

The base command is `/cwr` (aliases: `/cyberworldreset`, `/worldreset`, `/cworldreset`, `/cyberwr`, `/worldregen`). Most commands also run from console, where the world must always be given.

| Command | Permission | Description |
|---|---|---|
| `/cwr about` | `CyberWorldReset.player.about` | Plugin, server and hook information. |
| `/cwr help` | `CyberWorldReset.player.help` | Player help. |
| `/cwr reload` | `CyberWorldReset.admin.reload` | Reload the configuration. |
| `/cwr list` | `CyberWorldReset.admin.list` | List world setups. |
| `/cwr info [world]` | `CyberWorldReset.admin.info` | Show a world setup. |
| `/cwr create [world]` | `CyberWorldReset.admin.create` | Create a world setup. |
| `/cwr save [world]` | `CyberWorldReset.admin.save` | Save the world files for rollback. |
| `/cwr reset [world]` | `CyberWorldReset.admin.reset` | Reset a world now. |
| `/cwr edit <world> <setting> <value>` | `CyberWorldReset.admin.edit.*` | Change a world setting. |
| `/cwr confirm` | `CyberWorldReset.admin.*` | Confirm a pending command. |

`CyberWorldReset.player.*` and `CyberWorldReset.admin.*` grant every command of each group. The full list of `/cwr edit` settings is on the [Commands & Permissions](https://wiki.bitaspire.com/en/docs/cyberworldreset/commands) page.

## Placeholders

Requires [PlaceholderAPI](https://www.spigotmc.org/resources/6245/).

| Placeholder | Description |
|---|---|
| `%cwr_reset_status%` / `%cwr_reset_status_<world>%` | Next reset status, globally or for one world. |
| `%cwr_remaining_time_<world>%` | Time left until the next reset. |
| `%cwr_remaining_seconds_<world>%` | Seconds left until the next reset. |
| `%cwr_loaded_chunks_<world>%` / `%cwr_remaining_chunks_<world>%` | Chunk loading progress. |
| `%cwr_reset_percent_<world>%` | Chunk loading progress as a percentage. |
| `%cwr_chunk_radius%` / `%cwr_chunk_diameter%` / `%cwr_chunk_area%` | Chunk loading size from the config. |
| `%cwr_tps%` | Current server TPS. |

## Integrations

All of them are optional.

| Plugin | Use |
|---|---|
| [Multiverse-Core](https://www.spigotmc.org/resources/390/) 4.x and 5.x | Refreshes Multiverse portals after a reset. |
| [WorldGuard](https://modrinth.com/plugin/worldguard) | Can delete the regions of a reset world. |
| CMI | Keeps CMI warps and can refresh CMI portals. |
| [PlaceholderAPI](https://www.spigotmc.org/resources/6245/) | Placeholders above. |
| Iris, Terra, TerraformGenerator | Custom world generation on reset. |

Each hook is toggled under `config.hooks` in `config.yml`.

## Configuration

| File | Contents |
|---|---|
| `config.yml` | Confirmations, chunk loading, reset delays, suffocation fixes, hooks and language. |
| `worlds.yml` | One section per world setup: timers, seed, environment, generator, safe world, warnings, messages and commands. |
| `lang.yml` | Every message the plugin sends. |

The generated files are commented, and each option is documented on the [wiki](https://wiki.bitaspire.com/en/docs/cyberworldreset).

## Download

**CWR Standard**: [SpigotMC](https://www.spigotmc.org/resources/96834/) - [BuiltByBit](https://www.mc-market.org/resources/21684/) - [Polymart](https://polymart.org/resource/1636)

**CWR Premium**: [SpigotMC](https://www.spigotmc.org/resources/99431/) - [BuiltByBit](https://www.mc-market.org/resources/22649/)

## Building

Requires Java 8+ and Maven.

```bash
git clone https://github.com/Kihsomray/CyberWorldReset.git
cd CyberWorldReset
mvn clean package
```

The plugin jar is written to `target/CyberWorldReset-<version>.jar`.

## License

Released under the [Apache License 2.0](LICENSE.md).
