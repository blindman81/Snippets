$path = "C:\Users\mendu\Snippets\app\src\main\java\com\android\snippets\ui\DetailScreen.kt"
$content = [System.IO.File]::ReadAllText($path)

# Normalize newlines for matching
$content = $content -replace "`r`n", "`n"

$c1_old = "                onAdd = { name, color, style -> viewModel.updateSnippets(photo.id, name, color, style) },"
$c1_new = "                onAdd = { name, color, style, shape -> viewModel.updateSnippets(photo.id, name, color, style, shape) },"

$c2_old = "                            forcedColor = viewModel.getSnippetColor(snippet),
                            forcedStyle = viewModel.getSnippetStyle(snippet)
                        )"
$c2_new = "                            forcedColor = viewModel.getSnippetColor(snippet),
                            forcedStyle = viewModel.getSnippetStyle(snippet),
                            forcedShape = viewModel.getSnippetShape(snippet)
                        )"

$content = $content.Replace($c1_old, $c1_new)
$content = $content.Replace($c2_old, $c2_new)

[System.IO.File]::WriteAllText($path, $content)
Write-Host "Success"
