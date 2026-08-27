package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class CardVisualTheme(
    val id: Int,
    val nameKey: String,
    val descKey: String,
    val defaultName: String,
    val defaultDesc: String,
    val gradientColors: List<Color>,
    val accentColor: Color,
    val patternIcon: ImageVector,
    val badgeLabel: String
)

object CardVisualThemes {
    val themes = listOf(
        CardVisualTheme(
            id = 0,
            nameKey = "card_theme_rub_el_hizb",
            descKey = "card_theme_rub_el_hizb_desc",
            defaultName = "Émeraude Rub el Hizb",
            defaultDesc = "Étoile géométrique sacrée à 8 branches et polygones islamiques",
            gradientColors = listOf(Color(0xFF042016), Color(0xFF0E382A), Color(0xFF14533C)),
            accentColor = Color(0xFF34D399),
            patternIcon = Icons.Default.Star,
            badgeLabel = "Octagramme"
        ),
        CardVisualTheme(
            id = 1,
            nameKey = "card_theme_damascus_arabesque",
            descKey = "card_theme_damascus_arabesque_desc",
            defaultName = "Arabesque Damascène (Saphir)",
            defaultDesc = "Volutes entrelacées, arches polylobées et tracés floraux de Damas",
            gradientColors = listOf(Color(0xFF0B192C), Color(0xFF1E3E62), Color(0xFF183B56)),
            accentColor = Color(0xFF60A5FA),
            patternIcon = Icons.Default.FilterVintage,
            badgeLabel = "Arabesque"
        ),
        CardVisualTheme(
            id = 2,
            nameKey = "card_theme_andalusian_zellij",
            descKey = "card_theme_andalusian_zellij_desc",
            defaultName = "Zellij Alhambra (Indigo)",
            defaultDesc = "Mosaïque géométrique mauresque et entrelacs Girih d'Andalousie",
            gradientColors = listOf(Color(0xFF1A1A2E), Color(0xFF16213E), Color(0xFF0F3460)),
            accentColor = Color(0xFFA78BFA),
            patternIcon = Icons.Default.GridGoldenratio,
            badgeLabel = "Zellij"
        ),
        CardVisualTheme(
            id = 3,
            nameKey = "card_theme_kiswa_gold",
            descKey = "card_theme_kiswa_gold_desc",
            defaultName = "Kiswa & Or (Obsidienne)",
            defaultDesc = "Chevrons et motifs de tissage sacré dorés sur fond noir d'apparat",
            gradientColors = listOf(Color(0xFF111111), Color(0xFF1F1E1D), Color(0xFF2E2419)),
            accentColor = Color(0xFFFBBF24),
            patternIcon = Icons.Default.AutoAwesome,
            badgeLabel = "Kiswa"
        ),
        CardVisualTheme(
            id = 4,
            nameKey = "card_theme_hilal_dunes",
            descKey = "card_theme_hilal_dunes_desc",
            defaultName = "Hilal & Dunes (Ambre Saharien)",
            defaultDesc = "Croissants de lune célestes, étoiles et vagues de dunes chaudes",
            gradientColors = listOf(Color(0xFF2A150D), Color(0xFF431D0E), Color(0xFF632B10)),
            accentColor = Color(0xFFFB923C),
            patternIcon = Icons.Default.NightsStay,
            badgeLabel = "Hilal"
        ),
        CardVisualTheme(
            id = 5,
            nameKey = "card_theme_iznik_turquoise",
            descKey = "card_theme_iznik_turquoise_desc",
            defaultName = "Faïence Iznik (Turquoise)",
            defaultDesc = "Médaillons ottomans, palmettes et entrelacs marins d'Orient",
            gradientColors = listOf(Color(0xFF062A30), Color(0xFF0A444C), Color(0xFF116979)),
            accentColor = Color(0xFF2DD4BF),
            patternIcon = Icons.Default.Pattern,
            badgeLabel = "Iznik"
        )
    )

    fun getThemeById(id: Int): CardVisualTheme {
        return themes.find { it.id == id } ?: themes.first()
    }
}

@Composable
fun IslamicCardPatternBackground(
    themeIndex: Int,
    modifier: Modifier = Modifier,
    alphaMultiplier: Float = 1.0f
) {
    val theme = remember(themeIndex) { CardVisualThemes.getThemeById(themeIndex) }
    
    Canvas(modifier = modifier.fillMaxSize()) {
        when (theme.id) {
            0 -> drawRubElHizbPattern(theme.accentColor, alphaMultiplier)
            1 -> drawDamasceneArabesquePattern(theme.accentColor, alphaMultiplier)
            2 -> drawAndalusianZellijPattern(theme.accentColor, alphaMultiplier)
            3 -> drawKiswaGoldPattern(theme.accentColor, alphaMultiplier)
            4 -> drawHilalDunesPattern(theme.accentColor, alphaMultiplier)
            5 -> drawIznikTurquoisePattern(theme.accentColor, alphaMultiplier)
            else -> drawRubElHizbPattern(theme.accentColor, alphaMultiplier)
        }
    }
}

// 1. Rub el Hizb (Octagram Geometry & Islamic Star Rosette)
private fun DrawScope.drawRubElHizbPattern(accentColor: Color, alphaMultiplier: Float) {
    val width = size.width
    val height = size.height
    val strokeColor = accentColor.copy(alpha = 0.12f * alphaMultiplier)
    val fillColor = accentColor.copy(alpha = 0.04f * alphaMultiplier)
    val stroke = Stroke(width = 1.5f)

    // Center Large Rosette Motif
    val centerX = width * 0.72f
    val centerY = height * 0.5f
    val mainRadius = height * 0.45f

    drawOctagram(centerX, centerY, mainRadius, strokeColor, fillColor, stroke)
    drawOctagram(centerX, centerY, mainRadius * 0.65f, strokeColor, fillColor, stroke)
    drawOctagram(centerX, centerY, mainRadius * 0.35f, strokeColor, fillColor, stroke)
    drawCircle(strokeColor, radius = mainRadius * 0.85f, center = Offset(centerX, centerY), style = stroke)
    drawCircle(strokeColor, radius = mainRadius * 0.18f, center = Offset(centerX, centerY), style = stroke)

    // Corner Small Rosettes
    val cornerRadius = height * 0.22f
    drawOctagram(0f, 0f, cornerRadius, strokeColor, fillColor, stroke)
    drawOctagram(0f, height, cornerRadius, strokeColor, fillColor, stroke)
    drawOctagram(width, 0f, cornerRadius, strokeColor, fillColor, stroke)
    drawOctagram(width, height, cornerRadius, strokeColor, fillColor, stroke)

    // Subtle connecting diagonal grid
    val step = 60f
    var x = -width
    while (x < width * 2) {
        drawLine(
            color = strokeColor.copy(alpha = 0.05f * alphaMultiplier),
            start = Offset(x, 0f),
            end = Offset(x + height, height),
            strokeWidth = 1f
        )
        drawLine(
            color = strokeColor.copy(alpha = 0.05f * alphaMultiplier),
            start = Offset(x, height),
            end = Offset(x + height, 0f),
            strokeWidth = 1f
        )
        x += step
    }
}

private fun DrawScope.drawOctagram(
    cx: Float,
    cy: Float,
    r: Float,
    strokeColor: Color,
    fillColor: Color,
    stroke: Stroke
) {
    if (r <= 0) return
    val squareHalf = r * 0.7071f
    
    // Draw first square
    val path1 = Path().apply {
        moveTo(cx - squareHalf, cy - squareHalf)
        lineTo(cx + squareHalf, cy - squareHalf)
        lineTo(cx + squareHalf, cy + squareHalf)
        lineTo(cx - squareHalf, cy + squareHalf)
        close()
    }
    drawPath(path1, fillColor)
    drawPath(path1, strokeColor, style = stroke)

    // Draw second 45-degree rotated square
    val path2 = Path().apply {
        moveTo(cx, cy - r)
        lineTo(cx + r, cy)
        lineTo(cx, cy + r)
        lineTo(cx - r, cy)
        close()
    }
    drawPath(path2, fillColor)
    drawPath(path2, strokeColor, style = stroke)
}

// 2. Damascene Arabesque (Flowing Curves & Interlaced Arches)
private fun DrawScope.drawDamasceneArabesquePattern(accentColor: Color, alphaMultiplier: Float) {
    val width = size.width
    val height = size.height
    val strokeColor = accentColor.copy(alpha = 0.14f * alphaMultiplier)
    val stroke = Stroke(width = 1.6f, cap = StrokeCap.Round)

    // Series of Islamic multi-cusped arches across background
    val archCount = 5
    val archWidth = width / archCount
    for (i in 0..archCount) {
        val startX = i * archWidth
        val archPath = Path().apply {
            moveTo(startX, height)
            lineTo(startX, height * 0.4f)
            cubicTo(
                startX, height * 0.15f,
                startX + archWidth * 0.5f, height * 0.05f,
                startX + archWidth * 0.5f, height * 0.05f
            )
            cubicTo(
                startX + archWidth * 0.5f, height * 0.05f,
                startX + archWidth, height * 0.15f,
                startX + archWidth, height * 0.4f
            )
            lineTo(startX + archWidth, height)
        }
        drawPath(archPath, strokeColor, style = stroke)
    }

    // Interlaced flowing Arabesque spirals in the right focal area
    val focalX = width * 0.75f
    val focalY = height * 0.45f
    for (layer in 1..4) {
        val r = layer * 32f
        val spiralPath = Path()
        var first = true
        for (step in 0..60) {
            val angle = step * (2 * PI.toFloat() / 30f)
            val currR = r + (sin(angle * 3f) * 12f)
            val px = focalX + cos(angle) * currR
            val py = focalY + sin(angle) * currR
            if (first) {
                spiralPath.moveTo(px, py)
                first = false
            } else {
                spiralPath.lineTo(px, py)
            }
        }
        spiralPath.close()
        drawPath(spiralPath, strokeColor.copy(alpha = (0.16f - layer * 0.025f) * alphaMultiplier), style = stroke)
    }
}

// 3. Andalusian Zellij (Moorish Girih Tile Mosaic & Interlaced Polygons)
private fun DrawScope.drawAndalusianZellijPattern(accentColor: Color, alphaMultiplier: Float) {
    val width = size.width
    val height = size.height
    val strokeColor = accentColor.copy(alpha = 0.13f * alphaMultiplier)
    val fillColor = accentColor.copy(alpha = 0.04f * alphaMultiplier)
    val stroke = Stroke(width = 1.4f)

    val tileSize = 70f
    val cols = (width / tileSize).toInt() + 2
    val rows = (height / tileSize).toInt() + 2

    for (c in 0..cols) {
        for (r in 0..rows) {
            val cx = c * tileSize
            val cy = r * tileSize
            val isEven = (c + r) % 2 == 0

            if (isEven) {
                // 8-pointed star tile
                drawOctagram(cx, cy, tileSize * 0.42f, strokeColor, fillColor, stroke)
            } else {
                // Intersecting cross / diamond tile
                val path = Path().apply {
                    moveTo(cx, cy - tileSize * 0.35f)
                    lineTo(cx + tileSize * 0.35f, cy)
                    lineTo(cx, cy + tileSize * 0.35f)
                    lineTo(cx - tileSize * 0.35f, cy)
                    close()
                }
                drawPath(path, strokeColor, style = stroke)
                drawCircle(strokeColor, radius = tileSize * 0.15f, center = Offset(cx, cy), style = stroke)
            }
        }
    }
}

// 4. Kiswa & Gold (Embroidered Chevron Bands & Sacred Knotwork)
private fun DrawScope.drawKiswaGoldPattern(accentColor: Color, alphaMultiplier: Float) {
    val width = size.width
    val height = size.height
    val strokeColor = accentColor.copy(alpha = 0.16f * alphaMultiplier)
    val stroke = Stroke(width = 1.8f)

    // Diagonal Sacred Chevrons running continuously across the card
    val chevronStep = 45f
    var y = -height
    while (y < height * 2) {
        val path = Path().apply {
            moveTo(0f, y)
            var currentX = 0f
            var up = true
            while (currentX < width) {
                val nextX = currentX + 35f
                val nextY = if (up) y - 20f else y + 20f
                lineTo(nextX, nextY)
                currentX = nextX
                up = !up
            }
        }
        drawPath(path, strokeColor.copy(alpha = 0.11f * alphaMultiplier), style = stroke)
        y += chevronStep
    }

    // Horizontal embroidered border frame
    val topY = 24f
    val botY = height - 24f
    drawLine(strokeColor, Offset(16f, topY), Offset(width - 16f, topY), strokeWidth = 2f)
    drawLine(strokeColor.copy(alpha = 0.08f * alphaMultiplier), Offset(16f, topY + 4f), Offset(width - 16f, topY + 4f), strokeWidth = 1f)
    
    drawLine(strokeColor, Offset(16f, botY), Offset(width - 16f, botY), strokeWidth = 2f)
    drawLine(strokeColor.copy(alpha = 0.08f * alphaMultiplier), Offset(16f, botY - 4f), Offset(width - 16f, botY - 4f), strokeWidth = 1f)

    // Ornamental gold crest on top right
    val crestX = width - 48f
    val crestY = 48f
    drawOctagram(crestX, crestY, 20f, strokeColor, accentColor.copy(alpha = 0.06f * alphaMultiplier), stroke)
}

// 5. Desert Hilal & Star Constellations
private fun DrawScope.drawHilalDunesPattern(accentColor: Color, alphaMultiplier: Float) {
    val width = size.width
    val height = size.height
    val strokeColor = accentColor.copy(alpha = 0.15f * alphaMultiplier)
    val stroke = Stroke(width = 1.5f)

    // Elegant Crescent Moon (Hilal) in upper right area
    val moonCenterX = width * 0.78f
    val moonCenterY = height * 0.38f
    val moonRadius = height * 0.28f

    // Outer moon circle
    drawCircle(
        color = strokeColor.copy(alpha = 0.22f * alphaMultiplier),
        radius = moonRadius,
        center = Offset(moonCenterX, moonCenterY),
        style = stroke
    )
    // Inner offset moon arc creates crescent illusion
    drawCircle(
        color = strokeColor.copy(alpha = 0.15f * alphaMultiplier),
        radius = moonRadius * 0.82f,
        center = Offset(moonCenterX + moonRadius * 0.38f, moonCenterY - moonRadius * 0.15f),
        style = stroke
    )

    // Star inside crescent
    val starX = moonCenterX + moonRadius * 0.45f
    val starY = moonCenterY - moonRadius * 0.05f
    drawOctagram(starX, starY, moonRadius * 0.26f, strokeColor, accentColor.copy(alpha = 0.06f * alphaMultiplier), stroke)

    // Flowing Sand Dune Waves along the lower half
    for (w in 0..3) {
        val baseY = height * (0.6f + w * 0.12f)
        val wavePath = Path().apply {
            moveTo(0f, baseY)
            cubicTo(
                width * 0.25f, baseY - 24f + (w * 8f),
                width * 0.65f, baseY + 20f - (w * 6f),
                width, baseY - 10f
            )
        }
        drawPath(wavePath, strokeColor.copy(alpha = (0.16f - w * 0.03f) * alphaMultiplier), style = stroke)
    }

    // Starfield Points
    val stars = listOf(
        Offset(width * 0.15f, height * 0.2f),
        Offset(width * 0.35f, height * 0.15f),
        Offset(width * 0.52f, height * 0.28f),
        Offset(width * 0.90f, height * 0.18f),
        Offset(width * 0.60f, height * 0.75f),
        Offset(width * 0.22f, height * 0.78f)
    )
    for (pt in stars) {
        drawCircle(strokeColor.copy(alpha = 0.28f * alphaMultiplier), radius = 2.5f, center = pt)
        drawLine(strokeColor.copy(alpha = 0.20f * alphaMultiplier), Offset(pt.x - 5f, pt.y), Offset(pt.x + 5f, pt.y), strokeWidth = 1f)
        drawLine(strokeColor.copy(alpha = 0.20f * alphaMultiplier), Offset(pt.x, pt.y - 5f), Offset(pt.x, pt.y + 5f), strokeWidth = 1f)
    }
}

// 6. Ottoman Iznik (Medallions & Floral Turquoise Tracery)
private fun DrawScope.drawIznikTurquoisePattern(accentColor: Color, alphaMultiplier: Float) {
    val width = size.width
    val height = size.height
    val strokeColor = accentColor.copy(alpha = 0.15f * alphaMultiplier)
    val fillColor = accentColor.copy(alpha = 0.04f * alphaMultiplier)
    val stroke = Stroke(width = 1.5f)

    // Central & Secondary Medallions (Gül)
    val cx = width * 0.74f
    val cy = height * 0.48f
    val r = height * 0.38f

    // 12-lobed medallion flower
    val petalCount = 12
    for (i in 0 until petalCount) {
        val angle = i * (2 * PI.toFloat() / petalCount)
        val px = cx + cos(angle) * (r * 0.7f)
        val py = cy + sin(angle) * (r * 0.7f)
        drawCircle(strokeColor, radius = r * 0.28f, center = Offset(px, py), style = stroke)
    }
    drawCircle(strokeColor, radius = r, center = Offset(cx, cy), style = stroke)
    drawCircle(strokeColor, radius = r * 0.45f, center = Offset(cx, cy), style = stroke)
    drawOctagram(cx, cy, r * 0.3f, strokeColor, fillColor, stroke)

    // Corner decorative border arches
    drawCircle(strokeColor, radius = height * 0.35f, center = Offset(0f, 0f), style = stroke)
    drawCircle(strokeColor, radius = height * 0.35f, center = Offset(0f, height), style = stroke)
}
