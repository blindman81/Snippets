$transcriptPath = "C:\Users\mendu\.gemini\antigravity\brain\5f632548-145c-4036-a352-1f1c3e2c07e7\.system_generated\logs\transcript.jsonl"
$lines = Get-Content $transcriptPath -Raw
$jsonLines = $lines -split "`n"

$diffOutput = ""
foreach ($line in $jsonLines) {
    if ($line -match '@@ -298,658') {
        $data = $line | ConvertFrom-Json
        $diffOutput = $data.content
        break
    }
}

if ($diffOutput) {
    $diffLines = $diffOutput -split "`n"
    $extracted = @()
    $inDiff = $false
    foreach ($l in $diffLines) {
        if ($l -match '@@ -298,658') {
            $inDiff = $true
            continue
        }
        if ($inDiff) {
            if ($l -eq '[diff_block_end]') { break }
            if ($l.StartsWith('-')) {
                $extracted += $l.Substring(1)
            }
            elseif ($l.StartsWith(' ')) {
                $extracted += $l.Substring(1)
            }
            elseif (-not $l.StartsWith('+')) {
                $extracted += $l
            }
        }
    }
    
    $extracted | Set-Content "C:\Users\mendu\Snippets\extracted.txt" -Encoding UTF8
    Write-Host ("Extracted " + $extracted.Length + " lines.")
}
