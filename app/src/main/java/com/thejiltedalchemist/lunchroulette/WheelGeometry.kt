package com.thejiltedalchemist.lunchroulette

/**
 * Pure wheel math, kept free of Android types so it can be unit tested on the JVM.
 *
 * [RouletteView] draws slice `i` clockwise from the 3 o'clock position, starting at `i * sliceAngle`.
 * The pointer sits at 12 o'clock, which is 270° in that same coordinate system.
 */
object WheelGeometry {

    /** Angle of the pointer (12 o'clock) in canvas degrees, measured clockwise from 3 o'clock. */
    const val POINTER_ANGLE = 270f

    fun sliceAngle(count: Int): Float {
        require(count > 0) { "count must be positive" }
        return 360f / count
    }

    /**
     * View rotation (degrees, clockwise, in [0, 360)) that places the centre of slice [index]
     * directly under the pointer.
     */
    fun rotationForWinner(index: Int, count: Int): Float {
        require(index in 0 until count) { "index $index out of range for $count slices" }
        val raw = POINTER_ANGLE - sliceAngle(count) * (index + 0.5f)
        return ((raw % 360f) + 360f) % 360f
    }
}
