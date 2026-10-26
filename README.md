# Remnants | Skills and Powers

A Path of Exile-style passive skill tree for Minecraft. 522 nodes across six schools of magic,
30 castable abilities, 12 build-defining keystones, shareable build codes and a server-driven tree
that admins can rewrite without touching the client. Works on its own and links up with
**Remnants | RPG Player Skills** when both are installed.

Formerly *Elementals*. The mod id is still `elementals`, so 1.x worlds, items and player progress
carry over.

## Supported versions

| Version root | Minecraft | Loaders | Java |
| --- | --- | --- | --- |
| `1.20.1/` | 1.20.1 | Fabric, Forge | 17 |
| `1.21.1/` | 1.21.1 | Fabric, NeoForge | 21 |
| `26.2/` | 26.2 | Fabric, NeoForge | 25 |
| `26.3/` | 26.3 | Fabric, NeoForge | 25 |

Fabric builds need Fabric API. Nothing else is required.

## Features

- **The Remnant Constellation:** 522 nodes in six schools (Pyromancy, Stormcalling, Umbral,
  Tidecalling, Earthshaping, Verdance) around a shared ring, with notable wheels, hybrid bridges
  between neighbouring schools, mastery notables and two-point ultimates.
- **30 abilities**, five per school, on four cast slots (default keys **R / Z / B / H**).
- **12 keystones** that bend the rules: Glass Cannon, Blood Pact, Eternal Tide, Undying Grove and more.
- **A tree screen built for planning:** path preview with point cost, staged changes with Apply and
  Undo, plan mode, search with fly-to, minimap, current → after stat summary, and build codes you
  can paste into chat or a guide. Scales from 720p at Auto GUI scale up to 4K.
- **Progression your way:** Power levels from kills and casting, Remnant Tomes, or RPG levels from
  Remnants | RPG Player Skills. Respec one node at a time or reset the whole tree.
- **Server-authoritative:** the server validates every allocation and sends the tree, ability
  numbers and config to clients. Custom trees and balance changes apply on `/powers reload`.
- **Admin tools:** `/powers` for points, levels, origins, grants, casting and reloads; per-node
  requirements and commands that run on allocate or refund.
- Fire Golem, rune drops, spell books and element seeds from Elementals 1.x, reworked to feed the tree.

Player documentation lives in [docs/](docs/README.md).

## Building

Each version root is an independent Gradle project. Build one from inside its folder:

```bash
cd 1.21.1
./gradlew build
```

`build` also runs the core regression suite (`CoreRegression`: tree validity, layout, build codes,
progression and config). 26.2 and 26.3 need Gradle to run on JDK 25.

Build every version and collect the eight jars in `all-jars/`:

```bash
export JAVA_HOME_17=/path/to/jdk-17 JAVA_HOME_21=/path/to/jdk-21 JAVA_HOME_25=/path/to/jdk-25
export JAVA_HOME=$JAVA_HOME_21
./gradlew buildAll
```

Shared, version-independent code is edited in `1.21.1/` and copied to the other roots with
`tools/sync-shared.sh 1.20.1 26.2 26.3`. See [CONTRIBUTING.md](CONTRIBUTING.md).

## Releasing

`git tag v2.0.0 && git push --tags` runs `.github/workflows/publish.yml`, which builds all versions
and uploads one Modrinth version and one CurseForge file per loader and Minecraft version through
`scripts/upload_platforms.mjs`. Store-page copy and images are in [marketing/](marketing/README.md).

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## Security

See [.github/SECURITY.md](.github/SECURITY.md) for responsible disclosure guidance.

## License

Apache License 2.0. See [LICENSE](LICENSE) and [NOTICE](NOTICE).
