# SimpleS5

The powers plugin for Odyssey SMP, season 1. Getting certain advancements gives you a power. Each
player can hold two powers at once, and only one player can hold each power. You switch between
your two powers and use the selected one with commands or with the `odysseyclientside` mod's keys.

## Requirements

| | |
|---|---|
| Server | Paper 1.21.1 |
| Java | 21 |
| Plugins | [ProtocolLib](https://github.com/dmulloy2/ProtocolLib) 5.3.0 or newer |

ModernDisguise and GlowingEntities are included in the plugin jar.

## Building

```
mvn clean package
```

This builds the plugin and runs the tests. The plugin is `target/SimpleS5-<version>.jar`; ignore
`original-SimpleS5-<version>.jar`. GitHub also builds every push: the jar is under the run's
**Artifacts** on the repository's **Actions** tab.

No Maven or Java installed? IntelliJ IDEA has both: open the project and run the Maven `package` goal.

## Commands

| Command | Who | What it does |
|---|---|---|
| `/power1` | everyone | Switch between your two powers. |
| `/power2` | everyone | Use the selected power. Sneak while using it for the power's second ability. |
| `/odyssey powers` (`/od`) | everyone | Which powers are taken. |
| `/odyssey description <power>` | everyone | What a power does. Tab-completes power names. |
| `/odyssey withdraw` | everyone | Take one of your powers out as an item (an amethyst shard) that you can give away. |
| `/resetcooldowns [player]` (`/rc`) | ops | Reset every power cooldown for yourself or another player. |
| `/toggle-pvp` | ops | Turn PvP on or off for non-ops. |
| `/scatter <radius>` | ops | Teleport all players to random spots within the radius around you. |
| `/reloadconfig` | `simples5.admin` | Reload `config.yml`. Player data isn't affected. |

### Permissions

| Permission | Default | Gives |
|---|---|---|
| `simples5.admin` | op | `/reloadconfig` |

## Powers

| Power | Advancement | Shown as |
|---|---|---|
| Very Very Frightening | adventure/very_very_frightening | Very Very Frightening |
| All Effects | nether/all_effects | How Did We Get Here? |
| Complete Catalogue | husbandry/complete_catalogue | Complete Catalogue |
| Monsters Hunted | adventure/kill_all_mobs | Monsters Hunted |
| Sniper Duel | adventure/sniper_duel | Sniper Duel |
| Uneasy Alliance | nether/uneasy_alliance | Uneasy Alliance |
| Feels Like Home | nether/ride_strider_in_overworld_lava | Feels Like Home |
| With Our Powers Combined | husbandry/froglights | WOPC |
| Hired Help | adventure/summon_iron_golem | Hired Help |
| Beaconator | nether/create_full_beacon | Beaconator |
| Balanced Diet | husbandry/balanced_diet | Balanced Diet |
| The Next Generation | end/dragon_egg | The Next Generation |
| Bullseye | adventure/bullseye | Bullseye (item only, not from the advancement) |
| Event powers | events/charlis_odyssey, events/quaks_odyssey, events/fortnite_odyssey | given out as items |

Each power's abilities are described in `config.yml` under `descriptions`, and in game with `/od description`.

### Adding a power

1. Write a class in `src/main/java/powers` that extends `ParentPowerClass` and implements `action(Player)`.
   Implement `Listener` too if it reacts to events; it is registered automatically.
2. Add one `powers.register(...)` line to `SimpleS5.registerPowers()`.
3. If an advancement should give it, add it under `defaults` and `descriptions` in `config.yml`.

If the power changes blocks, spawns mobs or hides players for a while, override `onShutdown()` to
undo that. That way a restart in the middle of the power doesn't leave anything behind.

## Files

The plugin keeps two files in `plugins/SimpleS5/`:

**`config.yml`**: settings and text. Edit it, then run `/reloadconfig`.

```yaml
join-aliases:            # show another name in these players' join messages; {} turns it off
  Charli4K: "Terracotta"
enable-test-power: false # loads the unfinished scroll-wheel test power (needs a restart)
defaults:                # power short name -> advancement that gives it
  sniper_duel: "adventure/sniper_duel"
descriptions:            # /od description text, one line per entry
  sniper_duel:
    - "§bAbilities:"
```

**`data.yml`**: player data, keyed by UUID. Don't edit it while the server is running, because the
plugin overwrites it.

```yaml
players:
  <uuid>:
    name: ItsKazzle          # last known name, for reading only
    mode: 0                  # selected power slot (0 or 1)
    powers:
      adventure/sniper_duel: true
capped_advancements:         # powers already given out once by their advancement
  sniper_duel: true
world:
  mace_crafted: true
```

Ownership changes are saved within a tick, slot changes every 30 seconds, and everything is saved
on shutdown. Versions before 5.10.0 kept player data in `config.yml`. The first start on 5.10.0 or
newer moves it to `data.yml`, after making a backup called `config.yml.pre-5.10-backup-<time>`.

## Tests

`mvn test` runs the unit tests in `src/test/java`. They use JUnit 5 and Mockito with a fake server,
so they don't need Minecraft. They cover saving, loading and migrating player data, the power
registry, and the cooldown helpers. Powers themselves still need testing in game.

See [CHANGELOG.md](CHANGELOG.md) for what changed in each version.
