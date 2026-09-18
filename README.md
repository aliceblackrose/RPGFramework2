# RPGFramework

Standalone Java 25 / Paper 26.2 RPG framework plugin.

The repository is intentionally one Gradle project: the plugin itself. There are no studio modules or auxiliary Gradle projects.

## Prototype systems

The current prototype provides:

- asynchronous YAML player profiles with periodic and disconnect persistence
- combat classes with health, movement-speed, and outgoing-damage modifiers
- experience-driven talent points
- health, damage, movement-speed, and critical-chance talents
- real combat-state and keyed cooldown services for downstream abilities
- typed RPG damage with physical/magic resistance and cancellable lethal-damage events
- a verified-death event emitted only after the Bukkit death event occurs
- UUID-based parties with expiring invites, leadership, kicking, disbanding, friendly-fire prevention, and party chat
- YAML-backed RPG items carrying persistent item IDs
- compatibility with the bundled legacy HAND and ARTIFACT item slots
- automatic RPG replacements for crafted vanilla armor defined in items/vanilla.yml
- admin commands to reload items, grant test experience, and give registered RPG items

## Player commands

```text
/classes
/classes list
/classes <class>

/stats
/stats spend <health|damage|speed|crit_chance> [points]
/stats reset

/party
/party create
/party invite <player>
/party accept
/party decline
/party leave
/party disband
/party kick <player>
/party leader <player>
/party chat [message]
```

## Admin/prototype commands

```text
/rpg status
/rpg reload
/rpg xp <player> <amount>
/rpg-give <item-id> [player]
```

## Build

```bash
./gradlew build
```

The resulting plugin jar is:

```text
build/libs/RPGFramework.jar
```

The project targets Java 25 and Paper 26.2 and compiles with `-Xlint:all -Werror`.
