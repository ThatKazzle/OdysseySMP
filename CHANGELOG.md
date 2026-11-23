# Changelog

Versions before 5.9.1 are listed only in the git history (`git log`).

## 5.12.0

### Fixed
- When the plugin is disabled (server stop or `/reload`), powers still in progress are undone:
  Monsters Hunted ice spheres, Fortnite Odyssey boxes, Hired Help golems, Uneasy Alliance
  invisibility and freeze, and the Balanced Diet Juggernaut size, reach and attack speed.
- Juggernaut attributes are reset when a player joins, in case the server crashed while they were active.
- Two overlapping Monsters Hunted ice spheres no longer leave ice behind.
- Balanced Diet's heal on hit no longer throws an error when it would go over max health.
- Fortnite Odyssey: blocks where a box stood drop items again once the box is gone (they used to
  drop nothing until the next restart).
- Removed a Monsters Hunted map that kept a copy of every block a sphere touched, forever.

### Changed
- Join message aliases (Charli4K → Terracotta, QuakX → Berries) moved from code to `join-aliases`
  in `config.yml`. A server whose config.yml doesn't have the key keeps the old aliases.
- The unfinished scroll-wheel test power only loads with `enable-test-power: true`.
- Code layout: the `PowerClasses` package is now `powers`, and command classes start with a capital
  letter. Nothing changes in game.

### Removed
- Unused `/skin` command entry, the empty `allPowersCommand`, the unused void and upside-down world
  generators, unused helpers, commented-out code, and the leftover `BALLER!` and debug log lines.

### Added
- README with requirements, build steps, commands, powers and file formats.
- Unit tests (JUnit 5 + Mockito) for player data, migration, the power registry and cooldown helpers.
  They run on every build, including GitHub Actions.

## 5.11.0

- One list of powers (`PowerRegistry`) replaces five hand-written copies.
- The client mod uses the same code as `/power1` and `/power2`. It now works for every power, and
  using Sniper Duel no longer also fires Uneasy Alliance.
- `/rc` resets every cooldown of every power, and `/rc <player>` resets only that player.
- "You already have this power!" now shows instead of "someone else has it".
- `/od description` tab-completes power names.
- Feels Like Home's Fire Resistance passive works.

## 5.10.0

- Player data moved from `config.yml` to `data.yml`, keyed by UUID instead of the (possibly
  disguised) name. Migration runs automatically on first start and backs up `config.yml` first.
- Data is saved safely: ownership changes right away, slot changes every 30 seconds, everything on shutdown.
- `/reloadconfig` actually reloads, and needs `simples5.admin`.

## 5.9.2

- Fixed crashes: right-clicking items with no data, dying with no powers, `/od withdraw` with no
  powers, a stale power slot, and console tab-complete.

## 5.9.1

- Builds for Java 21 and Paper API 1.21. Removed an unused library, stopped tracking IDE files,
  and added a GitHub Actions build.
