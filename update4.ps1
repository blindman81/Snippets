$path = "C:\Users\mendu\Snippets\app\src\main\java\com\android\snippets\ui\components\Shapes.kt"
$content = [System.IO.File]::ReadAllText($path)

$old1 = "val RoundedArrowRightShape = fitRoundedPolygon(RoundedPolygon(RoundedArrowRightVertices, RoundedArrowDirectionalRounding))"
$new1 = "val RoundedArrowRightShape = fitRoundedPolygon(RoundedPolygon(vertices = RoundedArrowRightVertices, perVertexRounding = RoundedArrowDirectionalRounding, centerX = 0.5f, centerY = 0.5f))"

$old2 = "val RoundedArrowLeftShape  = fitRoundedPolygon(RoundedPolygon(mirrorVerticesHorizontally(RoundedArrowRightVertices), RoundedArrowDirectionalRounding))"
$new2 = "val RoundedArrowLeftShape  = fitRoundedPolygon(RoundedPolygon(vertices = mirrorVerticesHorizontally(RoundedArrowRightVertices), perVertexRounding = RoundedArrowDirectionalRounding, centerX = 0.5f, centerY = 0.5f))"

$old3 = "val RoundedArrowDownShape  = fitRoundedPolygon(RoundedPolygon(rotateVertices90CW(RoundedArrowRightVertices),  RoundedArrowDirectionalRounding))"
$new3 = "val RoundedArrowDownShape  = fitRoundedPolygon(RoundedPolygon(vertices = rotateVertices90CW(RoundedArrowRightVertices), perVertexRounding = RoundedArrowDirectionalRounding, centerX = 0.5f, centerY = 0.5f))"

$old4 = "val RoundedArrowUpShape    = fitRoundedPolygon(RoundedPolygon(rotateVertices90CCW(RoundedArrowRightVertices), RoundedArrowDirectionalRounding))"
$new4 = "val RoundedArrowUpShape    = fitRoundedPolygon(RoundedPolygon(vertices = rotateVertices90CCW(RoundedArrowRightVertices), perVertexRounding = RoundedArrowDirectionalRounding, centerX = 0.5f, centerY = 0.5f))"

$content = $content.Replace($old1, $new1)
$content = $content.Replace($old2, $new2)
$content = $content.Replace($old3, $new3)
$content = $content.Replace($old4, $new4)

[System.IO.File]::WriteAllText($path, $content)
Write-Host "Success"
