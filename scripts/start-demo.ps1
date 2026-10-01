param(
 [string]$TomcatHome = 'C:\apache-tomcat-10.1.57-windows-x64\apache-tomcat-10.1.57',
 [string]$JavaHome = 'C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot',
 [int]$Port = 18080,
 [ValidateSet('smtp','demo')][string]$OtpMode = 'smtp'
)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
$demoBase = Join-Path $projectRoot '.runtime'
if (Test-Path "$demoBase\tomcat.pid") {
 $oldPid = [int](Get-Content "$demoBase\tomcat.pid")
 $oldProcess = Get-CimInstance Win32_Process -Filter "ProcessId = $oldPid"
 if ($oldProcess -and $oldProcess.CommandLine.Contains($demoBase)) { Stop-Process -Id $oldPid -Force }
}
$expandedApp = [IO.Path]::GetFullPath((Join-Path $demoBase 'webapps\24162023_02'))
if (!$expandedApp.StartsWith([IO.Path]::GetFullPath($demoBase) + '\', [StringComparison]::OrdinalIgnoreCase)) { throw 'Unsafe generated app path.' }
if (Test-Path -LiteralPath $expandedApp) { Remove-Item -LiteralPath $expandedApp -Recurse -Force }
$authDir = Join-Path $env:USERPROFILE '.m2\repository\com\microsoft\sqlserver\mssql-jdbc_auth\12.8.1.x64'
if (!(Test-Path "$authDir\mssql-jdbc_auth-12.8.1.x64.dll")) { throw 'Run: mvn dependency:get -Dartifact=com.microsoft.sqlserver:mssql-jdbc_auth:12.8.1.x64:dll' }
foreach ($dir in @('conf','logs','temp','webapps','work')) { New-Item -ItemType Directory -Force -Path (Join-Path $demoBase $dir) | Out-Null }
Copy-Item -Path "$TomcatHome\conf\*" -Destination "$demoBase\conf" -Force
$server = @"
<Server port="-1"><Service name="Catalina"><Connector port="$Port" address="127.0.0.1" protocol="HTTP/1.1" connectionTimeout="20000" URIEncoding="UTF-8"/><Engine name="Catalina" defaultHost="localhost"><Host name="localhost" appBase="webapps" unpackWARs="true" autoDeploy="true"/></Engine></Service></Server>
"@
[IO.File]::WriteAllText("$demoBase\conf\server.xml",$server)
Copy-Item "$projectRoot\target\24162023_02.war" "$demoBase\webapps\24162023_02.war" -Force
if (!$env:DB_URL) { $env:DB_URL = 'jdbc:sqlserver://localhost:1433;databaseName=WebExam02_24162023;integratedSecurity=true;encrypt=true;trustServerCertificate=true;loginTimeout=5' }
$env:OTP_MODE = $OtpMode
python "$PSScriptRoot\start-tomcat.py" "$JavaHome" "$TomcatHome" "$demoBase" "$authDir"
if ($LASTEXITCODE -ne 0) { throw 'Tomcat launcher failed.' }
Write-Output "Tomcat: http://localhost:$Port/24162023_02/home"
if ($OtpMode -eq 'demo') { Write-Output "DEMO OTP is in $demoBase\logs\stderr.log" }
