import assert from "node:assert/strict";
import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import { test } from "node:test";
import { SUPPORTED_TARGETS, assertCompleteRelease, curseforgeMetadata, curseforgeRelations, modrinthMetadata, parseJar, parseTargets } from "./release_metadata.mjs";
import { collectReleases, parseArgs } from "./upload_platforms.mjs";

const jarName = (target, version = "2.0.0") => {
  const [loader, game] = target.split(":");
  return `remnants-skills-and-powers-${loader}-${game}-${version}.jar`;
};

test("parses every supported target, 26.x included", () => {
  for (const target of SUPPORTED_TARGETS) {
    const release = parseJar(jarName(target), "2.0.0");
    assert.equal(`${release.loader}:${release.game}`, target);
    assert.equal(release.version, "2.0.0");
  }
});

test("rejects unknown targets, other mods and version mismatches", () => {
  assert.throws(() => parseJar("remnants-skills-and-powers-quilt-1.21.1-2.0.0.jar"));
  assert.throws(() => parseJar("remnants-skills-and-powers-forge-1.21.1-2.0.0.jar"));
  assert.throws(() => parseJar("elementals-fabric-1.21.1-1.4.0.jar"));
  assert.throws(() => parseJar(jarName("fabric:26.3", "2.0.1"), "2.0.0"));
});

test("fabric jars require Fabric API; every jar lists RPG Player Skills as optional", () => {
  for (const target of SUPPORTED_TARGETS) {
    const release = parseJar(jarName(target));
    const body = modrinthMetadata(release, { version: "2.0.0", changelog: "x" });
    const types = Object.fromEntries(body.dependencies.map((d) => [d.project_id, d.dependency_type]));
    assert.equal(types.d85UTOuq, "optional");
    assert.equal(types.P7dR8mSH, release.loader === "fabric" ? "required" : undefined);
    assert.deepEqual(body.game_versions, [release.game]);
    assert.deepEqual(body.loaders, [release.loader]);
    assert.equal(body.version_number, `2.0.0+${release.loader}-${release.game}`);
    const slugs = curseforgeRelations(release.loader, release.game).projects.map((p) => p.slug);
    assert.deepEqual(slugs, release.loader === "fabric" ? ["fabric-api"] : []);
  }
});

test("collects exactly the eight jars and ignores sources/javadoc", () => {
  const dir = fs.mkdtempSync(path.join(os.tmpdir(), "rsp-jars-"));
  for (const target of SUPPORTED_TARGETS) {
    fs.writeFileSync(path.join(dir, jarName(target)), "jar");
    fs.writeFileSync(path.join(dir, jarName(target).replace(".jar", "-sources.jar")), "src");
  }
  const releases = collectReleases(dir, "2.0.0");
  assert.equal(releases.length, 8);
  assertCompleteRelease(releases);
  assert.throws(() => assertCompleteRelease(releases.slice(1)));
});

test("parses arguments", () => {
  const args = parseArgs(["node", "x", "--version", "v2.0.0", "--dry-run", "--require-all-targets"]);
  assert.equal(args.version, "2.0.0");
  assert.ok(args.dryRun && args.requireAllTargets);
  assert.throws(() => parseArgs(["node", "x", "--version"]));
});

test("CurseForge metadata leaves out relations when a jar has no dependencies", () => {
  for (const target of SUPPORTED_TARGETS) {
    const release = parseJar(jarName(target));
    const meta = curseforgeMetadata(release, { version: "2.0.0", changelog: "x", gameVersionIds: [1] });
    if (release.loader === "fabric") assert.deepEqual(meta.relations.projects.map((p) => p.slug), ["fabric-api"]);
    else assert.equal("relations" in meta, false);
  }
});

test("target filters only accept supported targets", () => {
  assert.deepEqual([...parseTargets("forge:1.20.1, neoforge:26.3")], ["forge:1.20.1", "neoforge:26.3"]);
  assert.throws(() => parseTargets("forge:1.21.1"));
  assert.throws(() => parseArgs(["node", "x", "--version", "2.0.0", "--targets", "forge:1.20.1", "--require-all-targets"]));
  const args = parseArgs(["node", "x", "--version", "2.0.0", "--curseforge-only", "--targets", "forge:1.20.1"]);
  assert.ok(args.curseforgeOnly && args.targets.has("forge:1.20.1"));
});
