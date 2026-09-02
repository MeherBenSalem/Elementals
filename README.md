# Elementals

Elementals is a Minecraft mod with elemental spellcasting, runes, elemental progression,
signature abilities, and a Fire Golem encounter.

## Supported versions

| Version root | Minecraft | Loaders | Java |
| --- | --- | --- | --- |
| `1.20.1/` | 1.20.1 | Fabric, Forge | 17 |
| `1.21.1/` | 1.21.1 | Fabric, NeoForge | 21 |
| `26.2/` | 26.2 | Fabric, NeoForge | 25 |

Each version root is an independent Gradle project. Run build commands from the
specific version folder you want to work on.

## Features

- Fire and water elemental progression with passive effects
- Spell books: Explosive Orb, Ember Fire Shield, Inferno Wave
- Fire rune drops and signature ability (F key)
- Fire Golem with spawn egg
- Elemental HUD indicator

## Building

Build a specific version from inside that version folder:

```powershell
cd 1.20.1
.\gradlew.bat build --no-daemon
```

Build all version roots from the repository root:

```powershell
$env:JAVA_HOME_17 = "C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot"
$env:JAVA_HOME_21 = "C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot"
$env:JAVA_HOME_25 = "C:\Program Files\Eclipse Adoptium\jdk-25.0.4.7-hotspot"
$env:JAVA_HOME = $env:JAVA_HOME_21
.\gradlew.bat buildAll --no-daemon
```

Built mod JARs are collected under `all-jars/` when using the root `buildAll` task.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for development and pull request expectations.

## Security

See [.github/SECURITY.md](.github/SECURITY.md) for responsible disclosure guidance.

## License

This project is licensed under the Apache License 2.0. See [LICENSE](LICENSE) and
[NOTICE](NOTICE).
