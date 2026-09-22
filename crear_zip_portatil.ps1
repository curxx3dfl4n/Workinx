# Script de creacion de ZIP Portatil Limpio sin node_modules ni target
Write-Host 'Creando paquete ZIP portatil y limpio de WorkInX...' -ForegroundColor Green
 = 'c:\xampp\htdocs\WorkInX'
 = 'c:\xampp\htdocs\WorkInX_Portatil_Limpio.zip'

if (Test-Path ) { Remove-Item  -Force }

Add-Type -AssemblyName System.IO.Compression.FileSystem
 = [System.IO.Compression.CompressionLevel]::Optimal

 = Get-ChildItem -Path  -Recurse | Where-Object {
    .FullName -notmatch '\\node_modules(\\|$)' -and
    .FullName -notmatch '\\target(\\|$)' -and
    .FullName -notmatch '\\.git(\\|$)' -and
    .FullName -notmatch '\\dist(\\|$)' -and
    .FullName -notmatch '\\.idea(\\|$)'
}

 = [System.IO.Compression.ZipFile]::Open(, 'Create')

foreach ( in ) {
    if (-not .PSIsContainer) {
         = .FullName.Substring(.Length + 1)
        [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile(, .FullName, , ) | Out-Null
    }
}

.Dispose()
Write-Host '¡ZIP Portatil Creado Exitosamente en: c:\xampp\htdocs\WorkInX_Portatil_Limpio.zip!' -ForegroundColor Yellow
