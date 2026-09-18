# RPGFramework

Standalone Java 25 / Paper 26.2 RPG framework plugin.

This branch contains one Gradle project only: the plugin itself. There are no auxiliary modules, studio projects, or Gradle subprojects.

## Layout

- `src/main/java` — plugin source
- `src/main/resources` — plugin resources and RPG data
- `src/test/java` — framework tests
- `build.gradle` — the single root build

## Build

```bash
./gradlew build
```

The resulting plugin jar is:

```text
build/libs/RPGFramework.jar
```

The project targets Java 25 and Paper 26.2.
