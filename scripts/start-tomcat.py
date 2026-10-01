"""Launch an isolated demo Tomcat without opening a console window."""
import os
import subprocess
import sys
from pathlib import Path

java_home, tomcat_home, base, auth_dir = map(Path, sys.argv[1:])
args = [
    str(java_home / "bin/java.exe"), "-Dfile.encoding=UTF-8",
    f"-Djava.library.path={auth_dir}", f"-Dcatalina.home={tomcat_home}",
    f"-Dcatalina.base={base}", f"-Djava.io.tmpdir={base / 'temp'}",
    "-classpath", f"{tomcat_home / 'bin/bootstrap.jar'};{tomcat_home / 'bin/tomcat-juli.jar'}",
    "org.apache.catalina.startup.Bootstrap", "start",
]
with (base / "logs/stdout.log").open("wb") as stdout, (base / "logs/stderr.log").open("wb") as stderr:
    process = subprocess.Popen(args, stdout=stdout, stderr=stderr, stdin=subprocess.DEVNULL,
                               creationflags=subprocess.CREATE_NO_WINDOW, env=dict(os.environ))
(base / "tomcat.pid").write_text(str(process.pid))
print(f"Tomcat PID: {process.pid}")
