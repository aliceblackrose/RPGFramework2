# RPGFramework

A Java 25 / Paper 26.2 rewrite of the RPG framework.

This branch intentionally treats the previous implementation as behavioral reference rather than source to preserve. The
runtime is now composed from small services with explicit ownership and lifecycle boundaries instead of static singletons
that initialize themselves as a side effect of class loading.

## Runtime architecture

- `RPGFramework` owns startup/shutdown and command registration.
- `PlayerService` owns loaded player profiles.
- `YamlProfileRepository` owns persistence and keeps file I/O off the server thread.
- `ClassService` applies class/talent-derived player attributes.
- `AdvancedDamageHandler` preserves the existing custom damage event API while delegating calculation to a pure
  `DamageCalculator`.
- `PartyService` stores parties by UUID rather than retaining `Player` objects.
- `ItemRegistry` loads the existing YAML item definitions and creates fresh `ItemStack` instances on demand.
- Selected legacy manager class names remain as narrow transition facades while downstream plugins migrate to services.

## Build

The plugin targets Java 25 and Paper 26.2.

```bash
./gradlew :RPG-Framework:build
```

The resulting jar is `plugin/build/libs/RPGFramework.jar`.
