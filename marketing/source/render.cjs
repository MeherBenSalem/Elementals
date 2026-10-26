// Renders the listing artwork in marketing/images/ from real in-game screenshots and the bundled OFL fonts.
// Usage (from the repository root): NODE_PATH="$(npm root -g)" node marketing/source/render.cjs [name...]
// Requires the `playwright` package with a Chromium build available.
const path = require('path');
const fs = require('fs');
const { chromium } = require('playwright');

// The release and the supported Minecraft versions. Adding a version is one line here
// (plus one row in the compatibility table of curseforge/description.md).
const RELEASE = { version: '2.0', name: 'The Remnant Constellation' };
const VERSIONS = [
  { minecraft: '1.20.1', loaders: ['Fabric', 'Forge'] },
  { minecraft: '1.21.1', loaders: ['Fabric', 'NeoForge'] },
  { minecraft: '26.2', loaders: ['Fabric', 'NeoForge'] },
  { minecraft: '26.3', loaders: ['Fabric', 'NeoForge'] },
];
const LOADERS = ['Fabric', 'Forge', 'NeoForge'].filter((loader) => VERSIONS.some((v) => v.loaders.includes(loader)));

const root = path.resolve(__dirname, '..', '..');
const out = path.join(root, 'marketing', 'images');
const shots = path.join(out, 'screenshots');
const fonts = path.join(__dirname, 'fonts');
const url = (file) => 'file://' + file;
const shot = (name) => url(path.join(shots, name));
const source = (name) => url(path.join(__dirname, name));

// School colours match the tree (common/.../core/School.java).
const school = { pyromancy: '#ff6a2a', storm: '#6fe7ff', umbral: '#b26bff', tide: '#4f8dff', earth: '#c9a05a', verdance: '#6ee07a' };

const css = `
@font-face { font-family: RsSerif; src: url('${url(path.join(fonts, 'eb-garamond.ttf'))}'); }
@font-face { font-family: RsDisplay; src: url('${url(path.join(fonts, 'cinzel.ttf'))}'); }
:root { --text:#dcd8e6; --dim:#9894a8; --header:#efe9d8; --gold:#e8c872; --gold-hi:#f6dc94; --bg:#07080f; --line:rgba(232,200,114,.35); }
* { box-sizing: border-box; }
html, body { margin: 0; background: var(--bg); color: var(--text); font-family: RsSerif, serif; }
.stage { position: relative; overflow: hidden; background: radial-gradient(ellipse at 30% 40%, #151a2e 0%, #07080f 70%); }
.display { font-family: RsDisplay, serif; letter-spacing: .04em; }
.gold { background: linear-gradient(180deg, #fff4cf 0%, #f6dc94 35%, #e8c872 70%, #9a7a3c 100%);
        -webkit-background-clip: text; background-clip: text; color: transparent; }
.glow { filter: drop-shadow(0 0 18px rgba(232,200,114,.35)); }
.kicker { font-family: RsDisplay, serif; color: var(--gold); letter-spacing: .22em; font-size: 22px; }
.rule { height: 1px; background: linear-gradient(90deg, rgba(232,200,114,0), rgba(232,200,114,.9) 20%, rgba(232,200,114,.9) 70%, rgba(232,200,114,0)); }
.diamond { display: inline-block; width: 9px; height: 9px; background: var(--gold); transform: rotate(45deg); margin: 0 12px 3px 0; }
.frame { position: relative; border: 1px solid rgba(232,200,114,.45); box-shadow: 0 0 0 1px rgba(0,0,0,.6), 0 30px 80px rgba(0,0,0,.65), 0 0 60px rgba(120,150,255,.14); background: #000; }
.frame > img { display: block; width: 100%; }
canvas.stars { position: absolute; left: 0; top: 0; width: 100%; height: 100%; pointer-events: none; }
.vignette { position: absolute; inset: 0; background: radial-gradient(ellipse at center, rgba(0,0,0,0) 45%, rgba(0,0,0,.78) 100%); }
.backdrop { position: absolute; inset: -40px; background-size: cover; background-position: center; filter: blur(14px) brightness(.5) saturate(1.2); }
.pill { display: inline-block; border: 1px solid rgba(232,200,114,.55); padding: 6px 14px 4px; margin-right: 10px;
        font-family: RsDisplay, serif; font-size: 18px; letter-spacing: .12em; color: var(--header); background: rgba(0,0,0,.35); }
.label { position: absolute; font-family: RsDisplay, serif; font-size: 20px; letter-spacing: .14em; color: var(--header);
         background: rgba(8,9,16,.88); border: 1px solid rgba(232,200,114,.8); padding: 7px 14px 5px; white-space: nowrap;
         box-shadow: 0 8px 26px rgba(0,0,0,.6); z-index: 2; }
.label small { display: block; font-family: RsSerif, serif; letter-spacing: 0; font-size: 18px; color: var(--dim); margin-top: 1px; }
ul.points { list-style: none; padding: 0; margin: 0; }
ul.points li { position: relative; padding-left: 26px; margin: 0 0 14px; font-size: 27px; line-height: 1.32; color: var(--text); }
ul.points li::before { content: ''; position: absolute; left: 2px; top: 13px; width: 8px; height: 8px; background: var(--gold); transform: rotate(45deg); }
.stats { display: flex; margin-top: 22px; border: 1px solid rgba(232,200,114,.35); background: rgba(8,9,16,.85); box-shadow: 0 18px 50px rgba(0,0,0,.55); }
.stats > div { flex: 1; padding: 14px 24px 12px; border-left: 1px solid rgba(232,200,114,.2); }
.stats > div:first-child { border-left: 0; }
.stats .t { font-family: RsDisplay, serif; font-size: 16px; letter-spacing: .18em; color: var(--dim); }
.stats .v { font-family: RsDisplay, serif; font-size: 30px; color: var(--header); margin-top: 4px; white-space: nowrap; }
b.hi { color: var(--header); font-weight: normal; }
.chip { display: inline-block; width: 12px; height: 12px; border-radius: 50%; margin-right: 10px; box-shadow: 0 0 10px currentColor; background: currentColor; }
`;

// A deterministic starfield with a few gold sparks, so renders are reproducible.
const stars = `<canvas class="stars"></canvas><script>
(() => { const c = document.querySelector('canvas.stars'); const r = c.getBoundingClientRect();
  c.width = r.width; c.height = r.height; const g = c.getContext('2d'); let s = 11;
  const rnd = () => (s = (s * 16807) % 2147483647) / 2147483647;
  for (let i = 0; i < 140; i++) { const x = rnd() * c.width, y = rnd() * c.height, a = .2 + rnd() * .6, z = .8 + rnd() * 1.8;
    const gold = rnd() < .25, col = gold ? '246,220,148' : '190,205,255';
    const h = g.createRadialGradient(x, y, 0, x, y, z * 5); h.addColorStop(0, 'rgba(' + col + ',' + a * .5 + ')'); h.addColorStop(1, 'rgba(' + col + ',0)');
    g.fillStyle = h; g.beginPath(); g.arc(x, y, z * 5, 0, Math.PI * 2); g.fill();
    g.fillStyle = 'rgba(' + col + ',' + a + ')'; g.fillRect(x - z / 2, y - z / 2, z, z); } })();
</script>`;

const page = (width, height, body, transparent = false) => `<!doctype html><html><head><meta charset="utf-8"><style>${css}
body { width: ${width}px; height: ${height}px; } .stage { width: ${width}px; height: ${height}px; }
${transparent ? 'html, body, .stage { background: transparent; }' : ''}</style></head>
<body><div class="stage">${body}</div></body></html>`;

const header = (title) => page(1400, 120, `
  <div style="position:absolute;inset:0;display:flex;align-items:center;justify-content:center;gap:28px;background:linear-gradient(90deg,rgba(0,0,0,0),rgba(12,14,26,.9) 30%,rgba(12,14,26,.9) 70%,rgba(0,0,0,0))">
    <div class="rule" style="width:300px;transform:scaleX(-1)"></div>
    <span class="diamond" style="margin:0"></span>
    <div class="display gold glow" style="font-size:44px;white-space:nowrap">${title}</div>
    <span class="diamond" style="margin:0"></span>
    <div class="rule" style="width:300px"></div>
  </div>`, true);

// A feature card: copy on the left, artwork on the right. Bullets never end on a one-word line.
const card = ({ kicker, title, points, art, width = 1600, height = 900, artWidth = 980 }) => page(width, height, `
  <div class="backdrop" style="background-image:url('${source('backdrop.jpg')}');opacity:.5"></div>
  <div class="vignette"></div>${stars}
  <div style="position:absolute;left:80px;top:0;bottom:0;width:${width - artWidth - 150}px;display:flex;flex-direction:column;justify-content:center">
    <div class="kicker"${kicker.length > 24 ? ' style="font-size:18px;letter-spacing:.12em"' : ''}><span class="diamond"></span>${kicker}</div>
    <div class="display gold glow" style="font-size:58px;line-height:1.08;margin:18px 0 22px">${title}</div>
    <div class="rule" style="width:80%;margin-bottom:30px"></div>
    <ul class="points">${points.map((point) => `<li>${point.replace(/ (\S+)$/, '&nbsp;$1')}</li>`).join('')}</ul>
  </div>
  <div style="position:absolute;right:60px;top:50%;transform:translateY(-50%);width:${artWidth}px">${art}</div>`);

// An exact region (x, y, w, h in 1920x1080 pixels) of a capture, scaled to W pixels wide.
const region = (file, x, y, w, h, W) => `<div style="position:relative;width:${W}px;height:${Math.round(W * h / w)}px;overflow:hidden">
    <img src="${shot(file)}" style="position:absolute;max-width:none;width:${W * 1920 / w}px;left:${-x * W / w}px;top:${-y * W / w}px"></div>`;
const framed = (inner, style = '', color = '') => `<div class="frame" style="${color ? `border-color:${color}aa;` : ''}${style}">${inner}</div>`;
const label = (text, style, small = '') => `<div class="label" style="${style}">${text}${small ? `<small>${small}</small>` : ''}</div>`;
const stats = (items, color) => `<div class="stats"${color ? ` style="border-top:2px solid ${color}"` : ''}>${items.map(([t, v, flex]) =>
  `<div${flex ? ` style="flex:${flex}"` : ''}><div class="t">${t}</div><div class="v">${v}</div></div>`).join('')}</div>`;
// Two overlapping captures.
const pair = (first, second, firstLabel, secondLabel, firstSpot = 'left:20px;top:20px', height = 700) => `<div style="position:relative;height:${height}px">
    <div class="frame" style="position:absolute;left:0;top:0;width:74%"><img src="${shot(first)}" style="display:block;width:100%"></div>
    <div class="frame" style="position:absolute;right:0;bottom:0;width:74%"><img src="${shot(second)}" style="display:block;width:100%"></div>
    ${label(firstLabel, firstSpot)}${label(secondLabel, 'right:20px;bottom:20px')}</div>`;
// A 2x2 grid of labelled action shots.
const grid = (items) => `<div style="display:grid;grid-template-columns:1fr 1fr;gap:18px">${items.map(([file, name, sub, color]) =>
  framed(`<img src="${shot(file)}">` + label(name, 'left:12px;bottom:12px;font-size:17px', sub), '', color)).join('')}</div>`;

const versionText = VERSIONS.map((v) => v.minecraft).join(' · ');

const assets = {
  banner: () => page(1800, 600, `
    <div style="position:absolute;right:-20px;top:-60px;-webkit-mask-image:linear-gradient(90deg,transparent 0%,#000 35%);mask-image:linear-gradient(90deg,transparent 0%,#000 35%)">
      ${region('tree-overview.jpg', 300, 100, 1030, 610, 1180)}</div>
    <div style="position:absolute;inset:0;background:linear-gradient(90deg,rgba(6,7,13,.97) 0%,rgba(6,7,13,.9) 32%,rgba(6,7,13,.35) 58%,rgba(6,7,13,0) 78%)"></div>
    <div style="position:absolute;inset:0;background:linear-gradient(0deg,rgba(6,7,13,.75) 0%,rgba(6,7,13,0) 30%)"></div>
    <div class="vignette"></div>${stars}
    <div style="position:absolute;left:90px;top:74px;width:960px">
      <div class="kicker"><span class="diamond"></span>${RELEASE.version} · ${RELEASE.name}</div>
      <div class="display gold glow" style="font-size:80px;line-height:1.04;margin:20px 0 24px;white-space:nowrap">Remnants<br><span style="font-size:58px">Skills and Powers</span></div>
      <div class="rule" style="width:620px;margin-bottom:24px"></div>
      <div style="font-size:33px;color:var(--header);line-height:1.3">A 522-node skill tree. 30 abilities. 12 keystones.<br>Plan it, share it, cast it.</div>
      <div style="margin-top:32px">${LOADERS.map((l) => `<span class="pill">${l}</span>`).join('')}
        <span style="font-size:24px;color:var(--dim);margin-left:8px">${versionText}</span></div>
    </div>`),

  icon: () => page(512, 512, `
    <div style="position:absolute;inset:0;background:radial-gradient(circle at 50% 45%,#1d2240 0%,#0b0d1a 55%,#05060b 100%)"></div>${stars}
    <div style="position:absolute;left:78px;top:78px;width:356px;height:356px;border-radius:50%;overflow:hidden;box-shadow:inset 0 0 30px rgba(0,0,0,.8)">
      <img src="${source('logo.png')}" style="position:absolute;left:-14px;top:-14px;width:384px;height:384px"></div>
    <div style="position:absolute;left:56px;top:56px;width:400px;height:400px;border-radius:50%;border:3px solid var(--gold);box-shadow:0 0 40px rgba(232,200,114,.45), inset 0 0 40px rgba(232,200,114,.25)"></div>
    <div style="position:absolute;left:78px;top:78px;width:356px;height:356px;border-radius:50%;border:1px solid rgba(232,200,114,.55)"></div>
    ${Object.values(school).map((color, i) => { const a = -Math.PI / 2 + i * Math.PI / 3, x = 256 + Math.cos(a) * 200, y = 256 + Math.sin(a) * 200;
      return `<div style="position:absolute;left:${x - 11}px;top:${y - 11}px;width:22px;height:22px;background:${color};transform:rotate(45deg);border:2px solid #0b0d1a;box-shadow:0 0 16px ${color}"></div>`; }).join('')}`),

  'card-constellation': () => card({
    kicker: 'The Remnant Constellation', title: 'A tree worth<br>exploring',
    points: ['Six schools around one shared ring: start anywhere', 'Notable wheels, mastery nodes and two-point ultimates',
      'Hybrid bridges like Plasma Arc and Black Ice reward mixing', 'Neighbouring schools are only three nodes apart',
      'Every node, link and number is server config'],
    art: framed(`<img src="${shot('tree-overview.jpg')}">`) + stats([['Nodes', '522'], ['Notables', '54'], ['Abilities', '30'], ['Keystones', '12']]) }),

  'card-schools': () => card({
    kicker: 'Six schools of magic', title: 'Choose your<br>origin',
    points: [`<span class="chip" style="color:${school.pyromancy}"></span><b class="hi">Pyromancy</b>: Ember Bolt to Meteor`,
      `<span class="chip" style="color:${school.storm}"></span><b class="hi">Stormcalling</b>: Chain Lightning to Tempest`,
      `<span class="chip" style="color:${school.umbral}"></span><b class="hi">Umbral Arts</b>: Shadow Step to Eclipse`,
      `<span class="chip" style="color:${school.tide}"></span><b class="hi">Tidecalling</b>: Frost Lance to Maelstrom`,
      `<span class="chip" style="color:${school.earth}"></span><b class="hi">Earthshaping</b>: Stone Skin to Mountain’s Wrath`,
      `<span class="chip" style="color:${school.verdance}"></span><b class="hi">Verdance</b>: Mending Light to Verdant Rebirth`],
    art: framed(`<img src="${shot('tree-origin.jpg')}">`) }),

  'card-abilities': () => card({
    kicker: '30 abilities', title: 'Spells that<br>land hard',
    points: ['Five per school, each with an ultimate', 'Four cast slots on R, Z, B and H', 'Essence, cooldowns and crits that scale with your tree',
      'Never breaks blocks. Respects teams. PvP off by default', 'Every number tunable in abilities.json'],
    art: grid([['meteor.jpg', 'Meteor', 'Pyromancy ultimate', school.pyromancy], ['tempest.jpg', 'Tempest', 'Stormcalling ultimate', school.storm],
      ['maelstrom.jpg', 'Maelstrom', 'Tidecalling ultimate', school.tide], ['verdant-rebirth.jpg', 'Verdant Rebirth', 'Verdance ultimate', school.verdance]]),
    artWidth: 930 }),

  'card-combat': () => card({
    kicker: 'In combat', title: 'Cast from<br>the hotbar',
    points: ['Ability bar right of the hotbar, with cooldown sweeps', 'Essence bar and Power XP at a glance', 'A reminder when you have points to spend',
      'Sits clear of the Remnants | RPG Player Skills HUD', 'Hides with F1, follows your key bindings'],
    art: `<div style="position:relative">${framed(`<img src="${shot('combat-hud-storm.jpg')}">`)}
      <div class="frame" style="position:absolute;right:-24px;bottom:-60px;width:470px">${region('combat-hud.jpg', 1240, 940, 300, 140, 470)}</div>
      ${label('Tempest into Chain Lightning', 'left:20px;top:20px')}</div>` }),

  'card-planning': () => card({
    kicker: 'Plan before you spend', title: 'See the path.<br>Then commit.',
    points: ['Hover any node: the whole path and its cost light up', 'Stage changes, then Apply or Undo. Nothing is lost by accident',
      'Current → after for every stat you would change', 'Shift-click to plan a build you cannot afford yet', 'Search any node and fly straight to it'],
    art: pair('tree-path-preview.jpg', 'tree-stats.jpg', 'Path preview', 'Staged · stat diff', 'left:20px;top:20px', 720) }),

  'card-keystones': () => card({
    kicker: '12 keystones', title: 'Bend the rules',
    points: ['<b class="hi">Glass Cannon</b>: +40% ability damage, 25% less health', '<b class="hi">Conduit</b>: every 4th cast is free and hits twice as hard',
      '<b class="hi">Blood Pact</b>: pay with Health instead of Essence', '<b class="hi">Undying Grove</b>: cheat death once every five minutes',
      '<b class="hi">Windborne</b>, <b class="hi">Eternal Tide</b>, <b class="hi">Bedrock Will</b> and five more'],
    art: framed(region('tree-keystone.jpg', 120, 60, 1680, 945, 978)) }),

  'card-share': () => card({
    kicker: 'Build codes', title: 'Share a build<br>in one line',
    points: ['Copy your build as a short <b class="hi">RSP1-</b> code', 'Paste any code to import it as a plan', 'Imports never spend points: stage what you can afford',
      'A code from an edited tree imports what still fits', '<b class="hi">/powers build</b> prints a clickable code in chat'],
    art: framed(`<img src="${shot('tree-share.jpg')}">`) }),

  'card-rpg': () => card({
    kicker: 'Remnants | RPG Player Skills', title: 'Better<br>together',
    points: ['Installed together, RPG levels grant skill points', 'Retroactive: an RPG level 30 player starts with 30 points',
      '+0.5% ability damage per RPG level', 'Nodes can require an RPG level or run its commands', 'Neither mod requires the other'],
    art: `<div style="position:relative">${framed(`<img src="${shot('rpg-level-up.jpg')}">`)}
      <div class="frame" style="position:absolute;left:-30px;bottom:-70px;width:440px">${region('tree-build.jpg', 0, 940, 540, 140, 440)}</div>
      ${label('+1 skill point from RPG level 27', 'right:20px;top:20px')}</div>` }),

  'card-progression': () => card({
    kicker: 'Progression', title: 'Earn it<br>your way',
    points: ['Power XP from kills, scaled by mob health, and from casting', '<b class="hi">Remnant Tomes</b>: rare Fire Golem drop, also craftable',
      'Refund one node for 1 XP level, or reset for 10', 'A <b class="hi">Tome of Unbinding</b> resets the tree for free', 'Elementals 1.x players keep their element as an origin'],
    art: pair('tree-build.jpg', 'fire-golem.jpg', 'A Pyromancy / Storm build', 'Fire Golem', 'left:20px;top:20px', 700) }),

  'card-config': () => card({
    kicker: 'For server owners', title: 'Your tree,<br>your rules',
    points: ['The server sends the tree: clients need no config', 'Edit, run <b class="hi">/powers reload</b>, everyone updates live',
      'Nodes can require levels and run commands on allocate', '<b class="hi">/powers</b> for points, levels, origins, grants and casts', 'Missing keys are added, your values are kept'],
    art: `<div style="display:flex;flex-direction:column;gap:24px">${[
      ['config/remnants/skills_and_powers/general.json', [['mode', '"auto"'], ['skill_points_per_rpg_level', '1'], ['ability_damage_percent_per_rpg_level', '0.5'],
        ['refund_cost_xp_levels_per_node', '1'], ['abilities_hurt_players', 'false']], true],
      ['config/remnants/skills_and_powers/abilities.json', [['cost', '60.0'], ['cooldown', '30.0'], ['damage', '22.0'], ['radius', '5.0'], ['delay', '1.2']], true, '"meteor": ']]
      .map(([file, rows, more, prefix = '']) => `<div class="frame" style="background:rgba(8,9,16,.94);padding:24px 36px;font-family:monospace;font-size:23px;line-height:1.5;color:var(--text)">
        <div style="color:var(--dim)">${file}</div>
        <pre style="margin:10px 0 0;font-family:inherit">${prefix ? `<span style="color:var(--gold)">${prefix}</span>` : ''}{
${rows.map(([k, v], i) => `  <span style="color:var(--gold)">"${k}"</span>: <span style="color:#8fb4ff">${v}</span>${i < rows.length - 1 || more ? ',' : ''}`).join('\n')}${more ? '\n  <span style="color:var(--dim)">…</span>' : ''}
}</pre></div>`).join('')}</div>` }),
};

const headers = { 'header-constellation': 'The Constellation', 'header-schools': 'Six Schools', 'header-abilities': 'Abilities',
  'header-combat': 'In Combat', 'header-planning': 'Planning', 'header-keystones': 'Keystones', 'header-share': 'Build Codes',
  'header-rpg': 'RPG Player Skills', 'header-progression': 'Progression', 'header-config': 'Server Owners', 'header-everything': 'Everything Else',
  'header-compatibility': 'Downloads & Compatibility', 'header-faq': 'Questions', 'header-gallery': 'Gallery' };
for (const [name, title] of Object.entries(headers)) assets[name] = () => header(title);
assets.divider = () => page(1400, 40, `<div style="position:absolute;inset:0;display:flex;align-items:center;justify-content:center;gap:18px">
  <div class="rule" style="width:520px;transform:scaleX(-1)"></div><span class="diamond" style="margin:0"></span><div class="rule" style="width:520px"></div></div>`, true);

(async () => {
  const wanted = process.argv.slice(2);
  const browser = await chromium.launch();
  fs.mkdirSync(out, { recursive: true });
  for (const [name, build] of Object.entries(assets)) {
    if (wanted.length && !wanted.includes(name)) continue;
    const html = build();
    const size = html.match(/body \{ width: (\d+)px; height: (\d+)px; \}/);
    const tab = await browser.newPage({ viewport: { width: +size[1], height: +size[2] }, deviceScaleFactor: 1 });
    const file = path.join(__dirname, `.render-${name}.html`);
    fs.writeFileSync(file, html);
    await tab.goto(url(file));
    await tab.evaluate(() => document.fonts.ready);
    await tab.waitForLoadState('networkidle');
    await tab.screenshot({ path: path.join(out, `${name}.png`), omitBackground: name === 'divider' || name.startsWith('header-') });
    fs.unlinkSync(file);
    await tab.close();
    console.log('rendered', name);
  }
  await browser.close();
})().catch((error) => { console.error(error); process.exit(1); });
