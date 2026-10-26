// Renders overlay.html to a transparent PNG sequence: node render_overlay.cjs [t1 t2 ...] (test frames) or all frames.
const fs = require('fs'), path = require('path');
const { chromium } = require('playwright');
const R = '/tmp/claude-0/reel';
(async () => {
  const caps = fs.readFileSync(`${R}/captions.json`, 'utf8');
  const words = fs.readFileSync(`${R}/words.json`, 'utf8');
  const html = fs.readFileSync(`${R}/overlay.html`, 'utf8').replace('__CAPTIONS__', caps).replace('__WORDS__', words);
  fs.writeFileSync(`${R}/.overlay_run.html`, html);
  const dur = JSON.parse(fs.readFileSync(`${R}/timeline.json`, 'utf8')).duration;
  const browser = await chromium.launch();
  const page = await browser.newPage({ viewport: { width: 1080, height: 1920 }, deviceScaleFactor: 1 });
  await page.goto('file://' + `${R}/.overlay_run.html`);
  await page.evaluate(() => document.fonts.ready);
  await page.waitForTimeout(300);
  const tests = process.argv.slice(2).map(Number);
  const out = tests.length ? `${R}/ovtest` : `${R}/ov`;
  fs.mkdirSync(out, { recursive: true });
  const times = tests.length ? tests : Array.from({ length: Math.round(dur * 30) }, (_, i) => i / 30);
  for (let i = 0; i < times.length; i++) {
    await page.evaluate(t => window.frame(t), times[i]);
    const name = tests.length ? `t_${times[i].toFixed(2)}.png` : `ov_${String(i).padStart(4, '0')}.png`;
    await page.screenshot({ path: path.join(out, name), omitBackground: true });
    if (!tests.length && i % 150 === 0) console.log('overlay', i, '/', times.length);
  }
  await browser.close();
})().catch(e => { console.error(e); process.exit(1); });
