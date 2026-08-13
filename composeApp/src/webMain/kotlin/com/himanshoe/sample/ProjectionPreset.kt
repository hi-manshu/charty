@file:Suppress("MagicNumber")

package com.himanshoe.sample

import com.himanshoe.charty3d.projection.Projection3D

/**
 * The viewing angles the 3D playground screens offer, shared so the presets a reader learns on one
 * chart mean the same thing on the next.
 */
internal enum class ProjectionPreset(
    val label: String,
    val projection: Projection3D,
) {
    Default(label = "Default", projection = Projection3D.Default),
    Isometric(label = "Isometric", projection = Projection3D.Isometric),
    Subtle(label = "Subtle", projection = Projection3D.Subtle),
    Dramatic(label = "Dramatic", projection = Projection3D.Dramatic),
    Custom(label = "Custom", projection = Projection3D.Default),
}
