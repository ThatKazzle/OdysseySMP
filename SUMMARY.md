# SimpleS5 overhaul: what was done, and what to do next

This document covers the cleanup of SimpleS5 from **5.9.0.1 to 5.12.0**, done in eight phases (0–7).
Each phase was built, committed and pushed to `master` separately. Per-version notes are in
[CHANGELOG.md](CHANGELOG.md), and how to build and run the plugin is in [README.md](README.md).

Everything below compiles and passes 25 unit tests. **None of it has been play-tested on a
server yet.** Test it on a test server before putting it on the live one (see "Before going live" at the end).

---

## What was done

### Phase 0: build setup (5.9.1)
- The project now builds for **Java 21**, which Paper 1.21.1 needs. It used to say Java 8 and 14.
- `plugin.yml` says `api-version: '1.21'`, matching the server.
- Removed the `org.reflections` library, which nothing used.
- IntelliJ files and build output are no longer stored in git (`.gitignore`).
- **GitHub Actions** builds the plugin on every push, so a broken build shows up straight away.

### Phase 1: crash fixes (5.9.2)
Fixed errors that normal play could cause:
- right-clicking an item with no item data
- dying with no powers
- `/od withdraw` with no powers
- a saved power slot that pointed past the end of the list
- tab-completing `/od` from the console

### Phases 2 + 3: player data (5.10.0)
- **Player data moved out of `config.yml` into a new `data.yml`, stored by UUID instead of by name.**
  Lookups by name broke when a player was disguised or changed their name.
- All 18 powers now receive the actual `Player` instead of a name to look up.
- **Safer saving:** a power changing hands is saved right away, the selected slot every 30
  seconds, and everything on shutdown. Each save writes a temporary file and then swaps it in, so a
  crash mid-save can't corrupt the data.
- **Automatic migration:** the first start on 5.10.0 backs up `config.yml`, then moves everyone's
  data into `data.yml`. Players it can't match are kept under `unmigrated`, not deleted.
- `/reloadconfig` reloads the config now (before, it did nothing). It needs the
  `simples5.admin` permission instead of a hard-coded list of names.

### Phases 4 + 5: one list of powers, and bug fixes (5.11.0)
- **`PowerRegistry`:** every power is listed once, with its key, display name, class and action-bar
  cooldown. It replaces five hand-kept lists that had drifted apart. Adding a power now takes one class
  and one line.
- `/rc` finds every cooldown of every power on its own, so it can't miss one again.
- The **client mod** runs the same code as `/power1` and `/power2`. Before:
  - it only knew 6 powers
  - it numbered slots differently
  - using Sniper Duel also fired Uneasy Alliance
  - it trusted the player name in the packet
- Other fixes:
  - `/rc <player>` reset the wrong player.
  - "You already have this power!" never showed.
  - `/od description` had no tab-complete.
  - Feels Like Home's Fire Resistance passive never worked.

### Phase 6: shutdown cleanup and dead code (5.12.0)
- **When the server stops or reloads**, powers still in progress are undone. Before, a restart at the
  wrong moment left these behind permanently:
  - Monsters Hunted ice spheres
  - Fortnite boxes
  - Hired Help golems
  - invisibility and freezes
  - Juggernaut's bigger size and reach

  Each power class has an `onShutdown()` method for this.
- Juggernaut attributes are also reset when a player joins, in case the server crashed.
- Bugs found and fixed along the way:
  - **Monsters Hunted:** two ice spheres at once left ice in the world for good.
  - **Monsters Hunted:** a hidden map kept a copy of every block a sphere ever touched, using up memory.
  - **Fortnite Odyssey:** wherever a box had stood, blocks dropped nothing when mined, until the next restart.
  - **Balanced Diet:** the heal on hit threw an error when it would go over max health.
- The hard-coded join names (Charli4K → Terracotta, QuakX → Berries) moved to `join-aliases` in
  `config.yml`.
- The unfinished scroll-wheel test power only loads with `enable-test-power: true`.
- Removed:
  - `/skin`
  - the empty "all powers" command
  - the unused void and upside-down world code
  - unused helper methods
  - commented-out blocks
  - the `BALLER!` log lines and the debug log spam from Juggernaut
- Renamed the package and classes to normal Java style (`PowerClasses` → `powers`,
  `odysseyCommands` → `OdysseyCommands`, and so on). This was its own commit, with no logic changes.

### Phase 7: docs and tests (5.12.0)
- **README.md**: requirements, build steps, commands, permissions, powers, how to add a power, and
  what `config.yml` and `data.yml` look like.
- **CHANGELOG.md**: what changed in each version from 5.9.1 on.
- **Unit tests** (`src/test/java`, 25 tests, about 2 seconds) run on every build, locally and on GitHub:
  - `PowerStoreTest`:
    - giving, dropping and saving powers
    - power order and slots
    - claimed advancements and the mace
    - a corrupted `data.yml` is set aside, not overwritten
    - the full migration from `config.yml`
  - `PowerRegistryTest`: key and name handling, duplicate detection.
  - `ParentPowerClassTest`: cooldown timing and text, and `/rc` reset rules.
- One change from the plan: the plan suggested MockBukkit, but the tests use **JUnit 5 + Mockito with
  a small fake server** instead. MockBukkit has to match the server's Paper version, while this
  setup has no such tie-in and covers what needed testing. MockBukkit is still worth adding later
  for event-level tests (see below).

### Numbers
| | Before (5.9.0.1) | After (5.12.0) |
|---|---|---|
| Hand-kept power lists | 5 | 1 |
| Places player data is saved | `config.yml`, on almost every action | `data.yml`, batched and crash-safe |
| Unit tests | 0 | 25 |
| Build check on push | no | yes |

---

## Suggestions: making it better and faster

These are not done yet. They're ordered by value for the effort.

### Performance

1. **Keep player powers in memory instead of reading YAML every time.**
   - `PowerStore` reads the YAML tree on every call.
   - The once-a-second timer and the action bar both call `getEnabledPowers` several times per player per second.
   - Beaconator and Complete Catalogue check `hasPower` for every player every tick or every 3 ticks.
   - Fix: keep a `Map<UUID, PlayerPowers>` in memory and write YAML only when saving. This is the biggest easy win.
2. **Monsters Hunted rebuilds its ice sphere every tick.**
   - That's about 600 block changes, 20 times a second, for 8 seconds, and each one is sent to every nearby client.
   - Fix: place the sphere once, then cancel `BlockBreakEvent` (and explosions) inside it.
   - Fortnite Odyssey's box does the same thing every 2 ticks and can be fixed the same way.
3. **The invisibility packet listener checks every online player for every equipment packet.**
   - Fix: return straight away when nobody is invisible, and keep a set of hidden entity IDs instead of looping.
4. **Beaconator records every holder's state every tick.**
   - That's fine with one holder, but it creates a new `PlayerState` object 20 times a second.
   - Fix: record every 2–5 ticks (rewind only needs about 3 seconds of history), or reuse objects.
5. **Max health is set every second.**
   - Complete Catalogue's 24 max health is applied by setting the base value once a second for every player.
   - Fix: use an `AttributeModifier` added when the power is gained and removed when it's lost (see 7).
6. **`getPlayersInRange` uses `distance()`, which takes a square root.**
   - The comment there says it doesn't. Fix: use `distanceSquared()` against `radius * radius`.

### Reliability

7. **Use attribute modifiers instead of changing base values.**
   - Juggernaut and Complete Catalogue change *base* attribute values, which are saved with the player.
     That's why reset code is needed on quit, join and shutdown.
   - Fix: use *transient* modifiers (`addTransientModifier`). They aren't saved, so nothing can get stuck after a crash.
8. **Overlapping Fortnite boxes interfere with each other.**
   - Fortnite Odyssey saves its blocks in one list shared by every box, so the first box to end also
     removes any other box that is still up.
   - Fix: give each box its own saved-blocks list. Monsters Hunted already does this per sphere.
9. **Hired Help remembers who to attack in a single shared field.**
   - It also keeps a reference to that player after they log out.
   - Fix: store it per owner UUID and look the player up when the golem spawns.
10. **Revoke advancements through the API.**
    - Right now it runs the `/advancement revoke` console command.
    - Fix: use `AdvancementProgress.revokeCriteria`, which avoids name problems and console spam.
11. **Keep the `/toggle-pvp` setting across restarts.** It resets to "on" every restart. Store it in `data.yml`.

### Ease of running the server

12. **Move numbers into `config.yml`: cooldowns, durations, radii and damage.**
    Then you can balance powers with `/reloadconfig` instead of rebuilding the jar.
13. **Use permissions instead of `isOp()`** for `/rc`, `/toggle-pvp` and `/scatter`
    (for example `simples5.admin`), so you can give staff those commands without full op.
14. **Give `/power1` and `/power2` real descriptions and usage text** in `plugin.yml`. The current
    text ("I don't think you gotta power lmao") is what players see in `/help`.
15. **Move player messages into `config.yml`.** Use Paper's Adventure/MiniMessage instead of `ChatColor`,
    which Paper marks deprecated. Messages could then be edited, or translated, without code changes.
16. **Drop ModernDisguise if Mimic isn't coming back.** Nothing disguises players any more. The library
    is only used to look up display names, which `player.getName()` already gives, so removing it would
    make the jar smaller and remove a version-sensitive dependency.

### Development

17. **Add MockBukkit tests for events** (advancement → power, death drops, the shard right-click, the
    2-power limit), using a MockBukkit release that matches the server's Paper version.
18. **Tag releases with the version** (`git tag 5.12.0`), and attach the jar to a GitHub Release. There is
    currently a `2.10.0` tag that looks like it was meant to be `5.10.0`.
19. **Upgrading Paper later:** in 1.21.3+ attribute names lose their `GENERIC_` prefix
    (`GENERIC_MAX_HEALTH` → `MAX_HEALTH`). Expect a batch of small compile fixes when upgrading.

---

## Before going live with 5.12.0

1. Back up the server's `plugins/SimpleS5/` folder.
2. Replace the jar with `target/SimpleS5-5.12.0.jar` and restart. Don't use `/reload`.
3. Optional: copy `join-aliases` and `enable-test-power` from the new default `config.yml` into the
   server's `config.yml`. Without them the old join names still work and the test power stays off.
4. On a test server, try:
   - each power's main and sneak abilities
   - `/power1`, `/power2` and the client mod keys
   - `/rc` and `/rc <player>`
   - `/od powers`, `/od description` and `/od withdraw`
   - picking up a power shard, and the 2-power limit
   - dying with powers
   - stopping the server during a Monsters Hunted sphere, a Fortnite box and a Hired Help golem: all
     three should be gone after the restart
