/**
 * Upload Remnants | Skills and Powers jars from all-jars/ to Modrinth + CurseForge.
 * One Modrinth version and one CurseForge file per loader and Minecraft version.
 *
 *   node --test scripts/upload_platforms.test.mjs
 *   node scripts/upload_platforms.mjs --version 2.0.0 --dry-run
 *   node scripts/upload_platforms.mjs --version 2.0.0 --require-all-targets
 *   node scripts/upload_platforms.mjs --version 2.0.0 --curseforge-only --targets forge:1.20.1,neoforge:26.3
 *
 * The changelog defaults to patchnotes/<version>.md. Uploads already recorded in
 * .release-upload-state.json are skipped, so a failed run can be resumed.
 */
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath, pathToFileURL } from "node:url";
import { CURSEFORGE_ID, JAR_PREFIX, LOADER_IDS, MOD_TITLE, MODRINTH_ID,
  assertCompleteRelease, curseforgeMetadata, modrinthMetadata, normalizeVersion, parseJar, parseTargets } from "./release_metadata.mjs";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const ROOT = path.join(__dirname, "..");
const JAR_DIR = path.join(ROOT, "all-jars");
const RELEASE_STATE_PATH = path.join(ROOT, ".release-upload-state.json");

function loadEnv() {
  const candidates = [
    path.join(ROOT, "secrets", "local.env"),
    path.join(process.env.USERPROFILE || "", "NightBeam-Knowledge-Base", "secrets", "local.env"),
    path.join(process.env.USERPROFILE || "", "Desktop", "local.env"),
  ];
  for (const envPath of candidates) {
    if (!fs.existsSync(envPath)) continue;
    for (const line of fs.readFileSync(envPath, "utf8").split(/\r?\n/)) {
      const m = line.match(/^([^#=]+)=(.*)$/);
      if (m && !process.env[m[1].trim()]) process.env[m[1].trim()] = m[2].trim();
    }
    console.log("Loaded secrets from", envPath);
    return;
  }
}

export function parseArgs(argv) {
  const out = { curseforgeOnly: false, modrinthOnly: false, dryRun: false, requireAllTargets: false, jarDir: JAR_DIR };
  for (let i = 2; i < argv.length; i++) {
    const a = argv[i];
    if (["--version", "--changelog-file", "--jar-dir", "--targets"].includes(a)) {
      const value = argv[++i];
      if (!value || value.startsWith("--")) throw new Error(`Missing value for ${a}`);
      if (a === "--version") out.version = value;
      else if (a === "--changelog-file") out.changelogFile = value;
      else if (a === "--targets") out.targets = parseTargets(value);
      else out.jarDir = path.resolve(value);
    } else if (a === "--curseforge-only") out.curseforgeOnly = true;
    else if (a === "--modrinth-only") out.modrinthOnly = true;
    else if (a === "--require-all-targets") out.requireAllTargets = true;
    else if (a === "--dry-run") out.dryRun = true;
    else throw new Error(`Unknown arg ${a}`);
  }
  out.version = normalizeVersion(out.version);
  if (out.targets && out.requireAllTargets) throw new Error("--targets and --require-all-targets cannot be combined");
  if (out.curseforgeOnly && out.modrinthOnly) throw new Error("--curseforge-only and --modrinth-only cannot be combined");
  return out;
}

export function collectReleases(jarDir, version) {
  if (!fs.existsSync(jarDir)) throw new Error(`Missing ${jarDir}; run ./gradlew buildAll first`);
  const jars = fs.readdirSync(jarDir)
    .filter((n) => n.startsWith(JAR_PREFIX) && n.endsWith(`-${version}.jar`))
    .map((n) => path.join(jarDir, n))
    .sort();
  if (!jars.length) throw new Error(`No ${JAR_PREFIX}*-${version}.jar in ${jarDir}`);
  const parsed = jars.map((jar) => parseJar(jar, version));
  const targets = parsed.map((p) => `${p.loader}:${p.game}`);
  if (new Set(targets).size !== targets.length) throw new Error("Duplicate release target");
  return parsed;
}

export function readChangelog(version, changelogFile) {
  for (const f of [changelogFile, path.join(ROOT, "patchnotes", `${version}.md`)].filter(Boolean)) {
    if (fs.existsSync(f)) return fs.readFileSync(f, "utf8");
  }
  return `## ${MOD_TITLE} ${version}\n\nSee CHANGELOG.md.`;
}

function loadState() {
  try {
    const parsed = JSON.parse(fs.readFileSync(RELEASE_STATE_PATH, "utf8"));
    if (Array.isArray(parsed.uploads)) return parsed;
  } catch {}
  return { uploads: [] };
}

function recorded(state, platform, version, release) {
  return state.uploads.some((e) => e.platform === platform && e.version === version && e.fileName === release.name);
}

function record(state, platform, version, release, remoteId) {
  if (recorded(state, platform, version, release)) return;
  state.uploads.push({ platform, version, loader: release.loader, game: release.game, fileName: release.name, remoteId,
    recordedAt: new Date().toISOString() });
  fs.writeFileSync(RELEASE_STATE_PATH, `${JSON.stringify(state, null, 2)}\n`, "utf8");
}

async function postWithRetry(url, init, label) {
  for (let attempt = 1; ; attempt++) {
    const res = await fetch(url, init());
    const text = await res.text();
    if (res.ok) {
      try { return JSON.parse(text).id ?? null; } catch { return null; }
    }
    if ((res.status === 429 || res.status >= 500) && attempt < 4) {
      await new Promise((r) => setTimeout(r, 15000 * attempt));
      continue;
    }
    throw new Error(`${label} ${res.status} ${text.slice(0, 500)}`);
  }
}

async function uploadModrinth(releases, version, changelog, state) {
  const projectId = process.env.MODRINTH_ID || MODRINTH_ID;
  for (const release of releases) {
    if (recorded(state, "modrinth", version, release)) {
      console.log("Modrinth skip", release.name);
      continue;
    }
    const data = modrinthMetadata(release, { version, changelog, projectId });
    const id = await postWithRetry("https://api.modrinth.com/v2/version", () => {
      const form = new FormData();
      form.append("data", JSON.stringify(data));
      form.append("file_0", new Blob([fs.readFileSync(release.jar)]), release.name);
      return { method: "POST", headers: { Authorization: process.env.MODRINTH_TOKEN }, body: form };
    }, `Modrinth ${release.name}`);
    record(state, "modrinth", version, release, id);
    console.log("Modrinth OK", data.version_number);
  }
}

async function uploadCurseForge(releases, version, changelog, state) {
  const projectId = process.env.CURSEFORGE_ID || CURSEFORGE_ID;
  const token = process.env.CURSEFORGE_TOKEN;
  const payload = await fetch("https://minecraft.curseforge.com/api/game/versions", {
    headers: { "X-Api-Token": token },
  }).then((r) => r.json());
  const versions = Array.isArray(payload) ? payload : payload?.data || [];
  const client = versions.find((v) => v.name === "Client")?.id;
  const server = versions.find((v) => v.name === "Server")?.id;
  if (!client || !server) throw new Error("CurseForge Client/Server version ids missing");
  for (const release of releases) {
    if (recorded(state, "curseforge", version, release)) {
      console.log("CurseForge skip", release.name);
      continue;
    }
    const game = versions.find((v) => v.name === release.game);
    if (!game) throw new Error(`No CurseForge game version for ${release.game}`);
    const meta = curseforgeMetadata(release, {
      version, changelog, gameVersionIds: [client, server, LOADER_IDS[release.loader], game.id],
    });
    const id = await postWithRetry(`https://minecraft.curseforge.com/api/projects/${projectId}/upload-file`, () => {
      const form = new FormData();
      form.append("metadata", JSON.stringify(meta));
      form.append("file", new Blob([fs.readFileSync(release.jar)]), release.name);
      return { method: "POST", headers: { "X-Api-Token": token }, body: form };
    }, `CurseForge ${release.name}`);
    record(state, "curseforge", version, release, id);
    console.log("CurseForge OK", meta.displayName);
  }
}

async function main() {
  const args = parseArgs(process.argv);
  const version = args.version;
  let releases = collectReleases(args.jarDir, version);
  if (args.requireAllTargets) assertCompleteRelease(releases);
  if (args.targets) {
    // Re-run part of a release, for example the files one platform rejected.
    releases = releases.filter((r) => args.targets.has(`${r.loader}:${r.game}`));
    if (releases.length !== args.targets.size) throw new Error("A requested target has no jar");
  }
  const changelog = readChangelog(version, args.changelogFile);
  console.log(`${MOD_TITLE} ${version}: ${releases.length} jars`);
  for (const r of releases) console.log(" ", r.name, "->", r.loader, r.game);
  if (args.dryRun) {
    // A preview never loads secrets, reads jar bytes or calls either API.
    for (const r of releases) {
      if (!args.curseforgeOnly) console.log("[dry-run] Modrinth", JSON.stringify(modrinthMetadata(r, { version, changelog: "…" })));
      if (!args.modrinthOnly) console.log("[dry-run] CurseForge", JSON.stringify(curseforgeMetadata(r, { version, changelog: "…", gameVersionIds: [] })));
    }
    return;
  }
  loadEnv();
  const { MODRINTH_TOKEN, CURSEFORGE_TOKEN } = process.env;
  if ((!args.curseforgeOnly && !MODRINTH_TOKEN) || (!args.modrinthOnly && !CURSEFORGE_TOKEN)) {
    throw new Error("Set MODRINTH_TOKEN and CURSEFORGE_TOKEN");
  }
  const state = loadState();
  if (!args.curseforgeOnly) await uploadModrinth(releases, version, changelog, state);
  if (!args.modrinthOnly) await uploadCurseForge(releases, version, changelog, state);
}

if (import.meta.url === pathToFileURL(process.argv[1] || "").href) {
  main().catch((e) => {
    console.error(e);
    process.exit(1);
  });
}
