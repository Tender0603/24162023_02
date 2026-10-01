"""Package source and documents only; never include local runtime, logs or credentials."""
from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED

root = Path(__file__).resolve().parent.parent
output = root / "24162023_02.zip"
files = [root / "pom.xml",root / "database.sql",root / ".gitignore",*root.glob("*.md")]
for directory in ("src","scripts"):
    files.extend(p for p in (root / directory).rglob("*") if p.is_file() and "__pycache__" not in p.parts and not p.name.endswith(".local.properties"))
with ZipFile(output,"w",ZIP_DEFLATED) as archive:
    for path in sorted(set(files)):
        archive.write(path,path.relative_to(root))
with ZipFile(output) as archive:
    assert archive.testzip() is None
    assert "pom.xml" in archive.namelist()
    assert "database.sql" in archive.namelist()
print(f"{output}: {len(files)} files; {output.stat().st_size} bytes; ZIP integrity verified")
