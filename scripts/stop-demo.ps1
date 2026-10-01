$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
$demoBase = Join-Path $projectRoot '.runtime'
if (Test-Path "$demoBase\tomcat.pid") {
 $demoPid = [int](Get-Content "$demoBase\tomcat.pid")
 $process = Get-CimInstance Win32_Process -Filter "ProcessId = $demoPid"
 if ($process -and $process.CommandLine.Contains($demoBase)) {
  Stop-Process -Id $demoPid
  Write-Output "Stopped project Tomcat PID $demoPid"
 }
}
