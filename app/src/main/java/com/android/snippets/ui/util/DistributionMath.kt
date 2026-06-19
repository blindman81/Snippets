package com.android.snippets.ui.util

import kotlin.random.Random

/**
 * Result data class containing the calculated polar coordinates for a snippet.
 */
data class OrbitalPosition(
    val radius: Float,
    val baseAngle: Float,
    val layer: Int
)

/**
 * Utility class to centralize the "Layered Orbital Distribution" algorithm used to scatter snippet pills
 * consistently across the Detail, Memory, and MediaSaver screens without overlaps.
 */
object DistributionMath {

    /**
     * Calculates the orbital position (radius and angle) for a given snippet index.
     *
     * @param index The index of the snippet in the list.
     * @param safeMin The inner boundary radius.
     * @param safeMax The outer boundary radius.
     * @param random A kotlin.random.Random instance (typically seeded with a hash) for deterministic jitter.
     * @param isMediaSaver Flag to adjust spacing for high-resolution Canvas exports.
     * @param scaleFactor A multiplier used by MediaSaver to scale bounds relative to image resolution.
     */
    fun calculateOrbitalPosition(
        index: Int,
        safeMin: Float,
        safeMax: Float,
        random: Random,
        isMediaSaver: Boolean = false,
        scaleFactor: Float = 1f
    ): OrbitalPosition {
        val isFirst = index == 0

        val minRadius = safeMin.coerceAtLeast(0f)
        val maxRadius = safeMax.coerceAtLeast(minRadius)
        val availableRadius = (maxRadius - minRadius).coerceAtLeast(0f)
        val ringStep = availableRadius.coerceAtMost(if (isMediaSaver) 40f * scaleFactor else 40f)

        // Keep the base radius inside the caller's safe band so edge clamping does not collapse
        // multiple snippets onto the same visual slot.
        val baseRadius = if (availableRadius > 0f) {
            minRadius + (availableRadius / 2f)
        } else {
            minRadius
        }
        
        // Stagger snippets across two distinct rings with wide separation (±40)
        // Indices 1, 4, 5 on inner ring; 2, 3, 6, 7 on outer ring
        val radiusOffset = when (index) {
            1, 4, 5 -> -ringStep
            2, 3, 6, 7 -> ringStep
            else -> 0f
        }
        val radius = if (isFirst) 0f else (baseRadius + radiusOffset).coerceIn(minRadius, maxRadius)
 
        // Small organic wiggle (-8 to +8 degrees) so it doesn't look purely mechanical
        val jitter = if (isFirst) 0f else (random.nextFloat() * 16f - 8f)
 
        // Refined orbital distribution to maximize separation between neighbors
        val baseAngle = when (index) {
            0 -> 90f   // Bottom Center (Fixed)
            1 -> 270f  // Top Center (Inner Ring)
            2 -> 325f  // Upper Right (Outer Ring)
            3 -> 215f  // Upper Left (Outer Ring)
            4 -> 35f   // Lower Right (Inner Ring)
            5 -> 145f  // Lower Left (Inner Ring)
            6 -> 290f  // High Right (Outer Ring)
            7 -> 250f  // High Left (Outer Ring)
            else -> ((index * 137.5f) % 360f) // Golden angle for any more
        } + jitter




        return OrbitalPosition(radius, baseAngle, if (isFirst) 0 else 1)
    }

    /**
     * Determines the visual scaling factor for floating snippet clouds.
     * Shrinks snippets smoothly as more are added (1 to 6+) to fit the screen.
     */
    fun getCloudScalingFactor(totalCount: Int): Float {
        return 1.1f
    }

    /**
     * Determines the scaling factor for snippets in the grid layout.
     */
    fun getGridScalingFactor(totalCount: Int): Float {
        return 0.72f
    }
}
