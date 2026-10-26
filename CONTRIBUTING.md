# Contributing

Thanks for contributing to Remnants | Skills and Powers (mod id `elementals`).

## Before you start

- Open an issue or discussion for substantial changes before investing in a large implementation.
- Keep changes focused. Avoid unrelated refactors in the same pull request.
- Preserve compatibility-sensitive identifiers (`modId`, package names, registry IDs) unless a change explicitly requires them.

## Development workflow

1. Fork the repository and create a branch from `main`.
2. Make the smallest coherent change that solves the problem.
3. Build the version root you changed from inside that folder, for example `1.20.1/` or `1.21.1/`.
4. Update documentation when behavior, configuration, or release steps change.
5. Open a pull request with a clear summary, verification notes, and any compatibility risks.

## Coding expectations

- Follow the existing patterns in the targeted version root. The version folders are isolated Gradle projects.
- Version-independent code (the `core` package, abilities, the power manager, commands, the tree screen
  logic and the HUD) is edited in `1.21.1/` only and copied with `tools/sync-shared.sh 1.20.1 26.2 26.3`.
  The script lists every shared file. Everything else (Compat, StatApplier, entities, items that touch
  Minecraft APIs, GuiCanvas, screens, key mappings and loader modules) is written per version.
- The `core` package must not import Minecraft classes, so `CoreRegression` can run on a plain JVM.
- Run `./gradlew build` in every root you touched. It runs `CoreRegression` as part of `check`.
- For in-game checks, `-PextraMods=<dir>` (1.21.1 Fabric) loads extra mod jars such as RPG Player
  Skills into `runClient`, and `-PclientArgs="..."` passes arguments to Forge and NeoForge clients.
- Avoid committing generated jars, logs, run directories, secrets, or local IDE files.

## Reporting issues

- Use GitHub issues for bug reports and feature requests.
- Include the Minecraft version, loader, mod version, reproduction steps, and relevant logs when possible.

By contributing, you agree that your contributions are licensed under the Apache License 2.0 unless stated otherwise.
