import path from "node:path";

// NightBeam publishes to CurseForge only (since 24 Nov 2026); the existing Modrinth project stays up as it is but gets
// no new versions.
export const CURSEFORGE_ID = "1228643";
export const MOD_TITLE = "Remnants | Skills and Powers";
export const JAR_PREFIX = "remnants-skills-and-powers-";
export const LOADER_IDS = Object.freeze({ fabric: 7499, forge: 7498, neoforge: 10150 });

export const FABRIC_API = Object.freeze({ curseforgeSlug: "fabric-api" });
// Remnants | RPG Player Skills is an optional integration detected at runtime. It is not linked on CurseForge, because
// CurseForge relations need the project slug and an unknown slug fails the whole upload.

export const SUPPORTED_TARGETS = Object.freeze([
  "fabric:1.20.1", "forge:1.20.1",
  "fabric:1.21.1", "neoforge:1.21.1",
  "fabric:26.2", "neoforge:26.2",
  "fabric:26.3", "neoforge:26.3",
]);

export function normalizeVersion(value) {
  const version = String(value ?? "").replace(/^v/, "");
  if (!/^\d+\.\d+\.\d+(?:-[A-Za-z0-9.]+)?$/.test(version)) {
    throw new Error("--version must be a release version such as 2.0.0");
  }
  return version;
}

function assertTarget(loader, game) {
  if (!SUPPORTED_TARGETS.includes(`${loader}:${game}`)) {
    throw new Error(`Unsupported release target ${loader} ${game}`);
  }
}

export function assertCompleteRelease(releases) {
  const targets = new Set(releases.map((p) => `${p.loader}:${p.game}`));
  const missing = SUPPORTED_TARGETS.filter((target) => !targets.has(target));
  if (missing.length) throw new Error(`Missing release targets: ${missing.join(", ")}`);
}

export function parseJar(jar, expectedVersion) {
  const name = path.basename(jar);
  const match = name.match(
    /^remnants-skills-and-powers-(fabric|forge|neoforge)-(\d+\.\d+(?:\.\d+)?)-(\d+\.\d+\.\d+(?:-[A-Za-z0-9.]+)?)\.jar$/,
  );
  if (!match) throw new Error(`Cannot parse release jar ${name}`);
  const [, loader, game, version] = match;
  assertTarget(loader, game); // Fail closed instead of guessing a target.
  if (expectedVersion && version !== normalizeVersion(expectedVersion)) {
    throw new Error(`Jar version ${version} does not match ${expectedVersion}: ${name}`);
  }
  return { jar, name, loader, game, version };
}

export function curseforgeRelations(loader, game) {
  assertTarget(loader, game);
  return {
    projects: loader === "fabric" ? [{ slug: FABRIC_API.curseforgeSlug, type: "requiredDependency" }] : [],
  };
}

export function displayName(release, version) {
  return `${MOD_TITLE} ${version} · ${release.loader} · ${release.game}`;
}

export function curseforgeMetadata(release, { version, changelog, gameVersionIds }) {
  const relations = curseforgeRelations(release.loader, release.game);
  return {
    changelog,
    changelogType: "markdown",
    displayName: displayName(release, normalizeVersion(version)),
    gameVersions: gameVersionIds,
    releaseType: "release",
    // The upload API rejects an empty relations.projects array, so leave relations out when there are none.
    ...(relations.projects.length ? { relations } : {}),
  };
}

/** Parses "fabric:1.20.1,neoforge:26.3" into a set of supported targets. */
export function parseTargets(value) {
  const targets = String(value ?? "").split(",").map((t) => t.trim()).filter(Boolean);
  for (const target of targets) {
    if (!SUPPORTED_TARGETS.includes(target)) throw new Error(`Unsupported release target ${target}`);
  }
  return new Set(targets);
}
