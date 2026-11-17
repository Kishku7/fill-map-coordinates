"""Metadata single-source (D12): the canonical issue-tracker URL, stamped into every manifest.

This is THE one place the URL lives. `stamp` writes it into all cell manifests (Fabric
contact.issues, NeoForge + Forge issueTrackerURL, plugin.yml website). `check` fails (exit 1) if
any manifest disagrees -- run it in the audit and before publish so the URL can never drift.

The manifests are the committed EDIT SURFACE and live under _codegen/cell_templates/<loader>/ (the
cell dirs are 100% generated + gitignored), so this operates on the templates, not the built cells.

Usage: python scripts/_metadata.py stamp   |   python scripts/_metadata.py check
"""
import sys, os, re, json

CANON_ISSUES = "https://github.com/Kishku7/mod_support/issues"
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
MODE = sys.argv[1] if len(sys.argv) > 1 else "check"

CT = os.path.join(ROOT, "_codegen", "cell_templates")
FABRIC = os.path.join(CT, "Fabric", "src", "main", "resources", "fabric.mod.json")
NEOFORGE = os.path.join(CT, "NeoForge", "src", "main", "resources", "META-INF", "neoforge.mods.toml")
FORGE = os.path.join(CT, "Forge", "src", "main", "resources", "META-INF", "mods.toml")

bad = []
stamped = 0


def _read(p):
    return open(p, encoding="utf-8").read()


def _write(p, t):
    open(p, "w", encoding="utf-8", newline="\n").write(t)


# ---- Fabric fabric.mod.json : contact.issues ----
j = json.loads(_read(FABRIC))
cur = (j.get("contact") or {}).get("issues")
if cur != CANON_ISSUES:
    if MODE == "stamp":
        j.setdefault("contact", {})["issues"] = CANON_ISSUES
        _write(FABRIC, json.dumps(j, indent=2) + "\n"); stamped += 1
    else:
        bad.append((FABRIC, cur))

# ---- NeoForge neoforge.mods.toml : issueTrackerURL ----
t = _read(NEOFORGE)
m = re.search(r'issueTrackerURL\s*=\s*"([^"]*)"', t)
cur = m.group(1) if m else None
if cur != CANON_ISSUES:
    if MODE == "stamp":
        if m:
            t = t[:m.start()] + 'issueTrackerURL = "%s"' % CANON_ISSUES + t[m.end():]
        else:  # insert after the license line
            t = re.sub(r'(license\s*=\s*"[^"]*"\s*\n)', r'\1issueTrackerURL = "' + CANON_ISSUES + '"\n', t, count=1)
        _write(NEOFORGE, t); stamped += 1
    else:
        bad.append((NEOFORGE, cur))

# ---- Forge mods.toml : issueTrackerURL (same key as NeoForge; classic mods.toml path) ----
t = _read(FORGE)
m = re.search(r'issueTrackerURL\s*=\s*"([^"]*)"', t)
cur = m.group(1) if m else None
if cur != CANON_ISSUES:
    if MODE == "stamp":
        if m:
            t = t[:m.start()] + 'issueTrackerURL = "%s"' % CANON_ISSUES + t[m.end():]
        else:  # insert after the license line
            t = re.sub(r'(license\s*=\s*"[^"]*"\s*\n)', r'\1issueTrackerURL = "' + CANON_ISSUES + '"\n', t, count=1)
        _write(FORGE, t); stamped += 1
    else:
        bad.append((FORGE, cur))

if MODE == "stamp":
    print("stamped %d manifest(s) to %s" % (stamped, CANON_ISSUES))
else:
    if bad:
        print("METADATA CHECK FAILED -- %d manifest(s) not at canonical issue URL:" % len(bad))
        for m2, c in bad:
            print("  %s: %s" % (os.path.relpath(m2, ROOT), c))
        sys.exit(1)
    print("metadata OK: all 3 manifests at %s" % CANON_ISSUES)
