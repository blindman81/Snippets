$path = "C:\Users\mendu\Snippets\app\src\main\java\com\android\snippets\ui\DetailComponents.kt"
$content = [System.IO.File]::ReadAllText($path)

$content = $content.Replace("com.android.snippets.ui.components.snippetShapeFor", "com.android.snippets.ui.snippetShapeFor")
$content = $content.Replace("com.android.snippets.ui.components.snippetShapeLabel", "com.android.snippets.ui.snippetShapeLabel")
$content = $content.Replace("TextOverflow.Ellipsis", "androidx.compose.ui.text.style.TextOverflow.Ellipsis")

[System.IO.File]::WriteAllText($path, $content)
Write-Host "Success"
