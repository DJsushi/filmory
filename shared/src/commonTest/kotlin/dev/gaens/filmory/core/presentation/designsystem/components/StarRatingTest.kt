package dev.gaens.filmory.core.presentation.designsystem.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StarRatingTest {
    @Test
    fun `stars fill the space beside the button up to the maximum size`() {
        val plan = planRatingLayout(available = 1000, button = 200, gap = 20, minStar = 100, maxStar = 150)

        assertEquals(RatingLayoutPlan(starSize = 150, isButtonBelow = false), plan)
    }

    @Test
    fun `the button goes below when stars beside it would be too small`() {
        val plan = planRatingLayout(available = 800, button = 300, gap = 20, minStar = 100, maxStar = 150)

        assertTrue(plan.isButtonBelow)
        assertEquals(150, plan.starSize)
    }

    @Test
    fun `stars beside the button may be exactly the minimum`() {
        val plan = planRatingLayout(available = 820, button = 300, gap = 20, minStar = 100, maxStar = 150)

        assertFalse(plan.isButtonBelow)
        assertEquals(100, plan.starSize)
    }

    @Test
    fun `a touch maps to the half star under it and never below one`() {
        assertEquals(1, halfStarsAt(x = -30f, starWidth = 100f))
        assertEquals(1, halfStarsAt(x = 10f, starWidth = 100f))
        assertEquals(2, halfStarsAt(x = 70f, starWidth = 100f))
        assertEquals(3, halfStarsAt(x = 120f, starWidth = 100f))
        assertEquals(10, halfStarsAt(x = 495f, starWidth = 100f))
        assertEquals(10, halfStarsAt(x = 900f, starWidth = 100f))
    }
}
