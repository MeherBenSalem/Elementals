"""Builds docs/abilities.md, docs/keystones.md and docs/stats.md from the Java sources, so numbers never drift.

Usage: python3 tools/gen_docs.py
"""
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
CORE = ROOT / "1.21.1/common/src/main/java/com/naizo/elementals/core"
SCHOOLS = {
    "PYROMANCY": "Pyromancy (Fire)", "STORMCALLING": "Stormcalling (Storm)", "UMBRAL": "Umbral Arts (Void)",
    "TIDECALLING": "Tidecalling (Frost)", "EARTHSHAPING": "Earthshaping (Earth)", "VERDANCE": "Verdance (Life)",
}


def trim(v):
    v = float(v)
    return str(int(v)) if v == int(v) else ("%g" % v)


def abilities():
    src = (CORE / "AbilityCatalog.java").read_text()
    pattern = re.compile(r'def\("(\w+)", School\.(\w+), "([^"]+)", "([^"]+)", (true|false),\s*"((?:[^"\\]|\\.)*)"\)\s*((?:\.p\("\w+", [\d.]+\))+);', re.S)
    out = ["# Abilities", "",
           "Every ability below ships in the default skill tree. All numbers are defaults from",
           "`config/remnants/skills_and_powers/abilities.json` and can be changed per server.",
           "Damage and healing grow with your tree stats, and with your RPG level when Remnants | RPG Player Skills is installed.", ""]
    current = None
    for m in pattern.finditer(src):
        aid, school, name, icon, ult, desc, params = m.groups()
        p = dict((k, trim(v)) for k, v in re.findall(r'\.p\("(\w+)", ([\d.]+)\)', params))
        if school != current:
            current = school
            out += ["", "## " + SCHOOLS[school], "", "| Ability | What it does | Cost | Cooldown |", "| --- | --- | --- | --- |"]
        text = desc
        for k, v in p.items():
            text = text.replace("{" + k + "}", v)
        label = "**" + name + "**" + (" (ultimate)" if ult == "true" else "") + "<br>`" + aid + "`"
        out.append("| " + label + " | " + text + " | " + p.get("cost", "0") + " Essence | " + p.get("cooldown", "0") + "s |")
    (ROOT / "docs/abilities.md").write_text("\n".join(out) + "\n")


def keystones():
    src = (CORE / "Keystone.java").read_text()
    pattern = re.compile(r'reg\("(\w+)", "([^"]+)",\s*((?:"[^"]*",?\s*)+)\);', re.S)
    out = ["# Keystones", "",
           "Keystones are the large nodes at the outer edge of each school. Each one changes a rule of",
           "the game, usually with a cost. Custom trees refer to them by id.", "",
           "| Keystone | Id | Effect |", "| --- | --- | --- |"]
    for m in pattern.finditer(src):
        kid, name, lines = m.groups()
        text = " ".join(re.findall(r'"([^"]*)"', lines))
        out.append("| **" + name + "** | `" + kid + "` | " + text + " |")
    (ROOT / "docs/keystones.md").write_text("\n".join(out) + "\n")


def stats():
    src = (CORE / "Stat.java").read_text()
    pattern = re.compile(r'reg\("(\w+)", "%s ([^"]+)", Kind\.(\w+), ([\d.]+), (true|false)\);')
    out = ["# Stats", "",
           "Stat ids for the `stats` field of a node. Percent stats are whole percents (`12` means +12%).",
           "Vanilla stats are applied as attribute modifiers, so other mods see them. The cap is the most a",
           "build can reach, however many nodes grant the stat.", "",
           "| Id | Shown as | Kind | Cap | Applied as |", "| --- | --- | --- | --- | --- |"]
    for sid, shown, kind, cap, vanilla in pattern.findall(src):
        cap = "none" if float(cap) == 0 else trim(cap) + ("%" if kind == "PERCENT" else "")
        out.append("| `%s` | +X %s | %s | %s | %s |" % (sid, shown, kind.lower(), cap,
                                                       "vanilla attribute" if vanilla == "true" else "Remnants"))
    (ROOT / "docs/stats.md").write_text("\n".join(out) + "\n")


if __name__ == "__main__":
    abilities()
    keystones()
    stats()
    print("docs/abilities.md, docs/keystones.md and docs/stats.md written")
