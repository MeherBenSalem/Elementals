# Remnants | Skills and Powers

A Path of Exile-style passive skill tree for Minecraft. 522 nodes across six schools of magic,
30 castable abilities, 12 build-defining keystones, shareable build codes and a server-driven tree
that admins can rewrite without touching the client. Works on its own and links up with
**Remnants | RPG Player Skills** when both are installed.

Formerly *Elementals*. The mod id is still `elementals`, so 1.x worlds, items and player progress
carry over.

## Supported versions

These are the only supported targets (the NightBeam convention): 8 jars per release, all with the same version.

| Minecraft | Loaders | Java | Folder |
| --- | --- | --- | --- |
| 1.20.1 | Forge, Fabric | 17 bytecode (build on 21) | `1.20.1/{common,forge,fabric}` |
| 1.21.1 | NeoForge, Fabric | 21 | `1.21.1/{common,neoforge,fabric}` |
| 26.2 | NeoForge, Fabric | 25 | `26.2/{common,neoforge,fabric}` |
| 26.3 | NeoForge, Fabric | 25 | `26.3/{common,neoforge,fabric}` |

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

Each Minecraft version is a separate MultiLoader Gradle workspace with its own wrapper. The root build drives all four:

```bash
./gradlew buildAll             # every workspace, then the 8 loader jars into releases/
cd 1.20.1 && ./gradlew build   # Gradle on Java 21 (Java 17 toolchain)
cd 1.21.1 && ./gradlew build   # Java 21
cd 26.3 && ./gradlew build     # Java 25 (the same for 26.2)
```

`build` also runs the core regression suite (`CoreRegression`: tree validity, layout, build codes,
progression and config). The root build hands the 26.x workspaces `JAVA_HOME_25_X64` (or `-Pjava25Home=<jdk>`) and
the others `JAVA_HOME_21_X64` (or `-Pjava21Home=<jdk>`); without them each workspace uses the current `JAVA_HOME`.
Jars land in `<workspace>/<loader>/build/libs/remnants-skills-and-powers-<loader>-<minecraft>-<version>.jar` (the
mod id stays `elementals`). Every workspace's `gradle.properties` and the root `VERSION` file carry the same version
(`./gradlew checkVersions`; set it with `./gradlew syncVersion -PmodVersion=x.y.z`).
`python3 scripts/check_targets.py workspaces` checks the targets, the shared version and the metadata ranges.
Release notes for a version live in `patchnotes/<version>.md`.

Shared, version-independent code is edited in `1.21.1/` and copied to the other roots with
`tools/sync-shared.sh 1.20.1 26.2 26.3`. See [CONTRIBUTING.md](CONTRIBUTING.md).

## Releasing

CI (`.github/workflows/ci.yml`) builds all 8 jars on every push to `main` that touches the build. Publishing to
Modrinth and CurseForge is `.github/workflows/publish.yml`, run by hand (`version`, `dry_run`) or by a `v*.*.*` tag;
it never runs on a plain push. It refuses a second release of the mod on the same day (Africa/Tunis), takes the jars
from `releases/`, the GitHub Release or a build from source, fails unless exactly the 8 jars are found, and uploads
one Modrinth version and one CurseForge file per loader and Minecraft version through
`scripts/upload_platforms.mjs`. It needs the secrets `MODRINTH_TOKEN`, `CURSEFORGE_TOKEN`, `CURSEFORGE_API_KEY` and
the variables `MODRINTH_ID`, `CURSEFORGE_ID`. Store-page copy and images are in [marketing/](marketing/README.md).

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## Security

See [.github/SECURITY.md](.github/SECURITY.md) for responsible disclosure guidance.

## License

Apache License 2.0. See [LICENSE](LICENSE) and [NOTICE](NOTICE).
