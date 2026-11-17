package com.thejiltedalchemist.lunchroulette

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WheelGeometryTest {

    /** Where the centre of slice [index] ends up (canvas degrees, 0 = 3 o'clock, clockwise) after rotating by [rotation]. */
    private fun centreAfterRotation(index: Int, count: Int, rotation: Float): Float {
        val centre = WheelGeometry.sliceAngle(count) * (index + 0.5f) + rotation
        return ((centre % 360f) + 360f) % 360f
    }

    @Test
    fun winnerSliceCentreLandsUnderPointer_forEveryWheelSize() {
        for (count in 1..12) {
            for (index in 0 until count) {
                val rotation = WheelGeometry.rotationForWinner(index, count)
                assertEquals(
                    "count=$count index=$index",
                    WheelGeometry.POINTER_ANGLE,
                    centreAfterRotation(index, count, rotation),
                    0.01f
                )
            }
        }
    }

    @Test
    fun rotationIsNormalisedToOneTurn() {
        for (count in 1..12) {
            for (index in 0 until count) {
                val rotation = WheelGeometry.rotationForWinner(index, count)
                assertTrue("count=$count index=$index rotation=$rotation", rotation >= 0f && rotation < 360f)
            }
        }
    }

    @Test
    fun fourSliceExample() {
        // Slice 0 spans 0-90 degrees (centre 45); bringing 45 to 270 needs a 225 degree turn.
        assertEquals(225f, WheelGeometry.rotationForWinner(0, 4), 0.001f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsOutOfRangeIndex() {
        WheelGeometry.rotationForWinner(3, 3)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsEmptyWheel() {
        WheelGeometry.sliceAngle(0)
    }
}
