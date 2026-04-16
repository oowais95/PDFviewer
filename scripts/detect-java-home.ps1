$ErrorActionPreference = 'Stop'
$line = java -XshowSettings:properties -version 2>&1 | Select-String 'java.home'
if (-not $line) { exit 1 }
$home = $line.ToString().Split('=', 2)[1].Trim()
if ([string]::IsNullOrWhiteSpace($home)) { exit 1 }
Write-Output $home
