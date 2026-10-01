"""Static exam checks; no database changes."""
import json
import re
from pathlib import Path

root = Path(__file__).resolve().parent.parent
java = list((root / "src/main/java").rglob("*.java"))
all_java = list((root / "src").rglob("*.java"))
violations = []
for path in all_java:
    for name in re.findall(r"\b(?:class|interface|enum|record)\s+(\w+)",path.read_text(encoding="utf-8")):
        if not name.endswith("_24162023"):
            violations.append(f"{path.name}: {name}")
assert not violations, violations
views = list((root / "src/main/webapp").rglob("*.jsp*"))
for path in views:
    content = path.read_text(encoding="utf-8")
    assert not re.search(r"<%(?!@|--)",content), f"Scriptlet: {path}"
    assert not re.search(r"java\.sql|DriverManager|prepareStatement",content), f"JDBC in view: {path}"
for path in (root / "src/main/java/vn/edu/webexam/controller").glob("*.java"):
    assert not re.search(r"prepareStatement|DriverManager|SELECT\s|INSERT\s|UPDATE\s|DELETE\s",path.read_text(encoding="utf-8")), path
for path in all_java:
    assert not re.search(r"javax\.servlet|org\.springframework|hibernate|jakarta\.persistence|createStatement\(",path.read_text(encoding="utf-8")), path
result = dict(main_java_files=len(java),test_java_files=len(all_java)-len(java),naming_violations=violations,
              jsp_scriptlets=0,jdbc_in_views=0,sql_or_jdbc_in_controllers=0,forbidden_frameworks=0)
print(json.dumps(result,indent=2))
