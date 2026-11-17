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

    @Test
    fun threeSliceHardCodedExpectations() {
        // Slices span 0-120, 120-240, 240-360 with centres 60, 180, 300. Each centre is turned to 270.
        assertEquals(210f, WheelGeometry.rotationForWinner(0, 3), 0.001f)
        assertEquals(90f, WheelGeometry.rotationForWinner(1, 3), 0.001f)
        assertEquals(330f, WheelGeometry.rotationForWinner(2, 3), 0.001f)
    }

    @Test
    fun singleSliceWheel() {
        // The only slice spans the whole wheel with its centre at 180; turning it to 270 is a 90 degree turn.
        assertEquals(90f, WheelGeometry.rotationForWinner(0, 1), 0.001f)
    }

    @Test
    fun pointerIsInsideTheWinningSlice_notOnAnEdge() {
        // Uses the slice start/width RouletteView draws with, and checks the pointer falls strictly inside.
        for (count in 1..12) {
            val width = WheelGeometry.sliceAngle(count)
            for (index in 0 until count) {
                val rotation = WheelGeometry.rotationForWinner(index, count)
                val start = WheelGeometry.sliceStartAngle(index, count)
                val offsetIntoSlice = (((WheelGeometry.POINTER_ANGLE - rotation - start) % 360f) + 360f) % 360f
                assertTrue("count=$count index=$index offset=$offsetIntoSlice", offsetIntoSlice > width * 0.25f)
                assertTrue("count=$count index=$index offset=$offsetIntoSlice", offsetIntoSlice < width * 0.75f)
            }
        }
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
