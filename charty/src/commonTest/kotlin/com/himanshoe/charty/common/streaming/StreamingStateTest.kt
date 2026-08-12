package com.himanshoe.charty.common.streaming

import com.himanshoe.charty.common.config.Animation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class StreamingStateTest {
    private suspend fun StreamingState.growTo(
        maxScroll: Float,
        appended: Int,
    ) {
        onDataChanged(newMaxScroll = maxScroll, appended = appended, animation = Animation.Disabled)
    }

    @Test
    fun newState_followsFromTheStart() {
        val state = StreamingState()
        assertTrue(state.isFollowing)
        assertEquals(0, state.pendingCount)
        assertEquals(0f, state.currentScroll)
    }

    @Test
    fun detach_clearsFollowing() {
        val state = StreamingState()
        state.detach()
        assertFalse(state.isFollowing)
    }

    @Test
    fun newState_leavesTheDragToTheStreamingPan() {
        val state = StreamingState()
        assertFalse(state.crosshairOwnsDrag)
    }

    @Test
    fun crosshairOwningTheDrag_doesNotBlockProgrammaticScrolling() =
        runTest {
            val state = StreamingState()
            state.crosshairOwnsDrag = true
            state.growTo(maxScroll = 50f, appended = 50)
            state.scrollBy(-10f)
            assertEquals(40f, state.currentScroll)
        }

    @Test
    fun scrollBy_detachesAndMoves() =
        runTest {
            val state = StreamingState()
            state.growTo(maxScroll = 50f, appended = 50)
            state.scrollBy(-10f)
            assertFalse(state.isFollowing)
            assertEquals(40f, state.currentScroll)
        }

    @Test
    fun scrollBy_clampsAtStartOfData() =
        runTest {
            val state = StreamingState()
            state.growTo(maxScroll = 50f, appended = 50)
            state.scrollBy(-500f)
            assertEquals(0f, state.currentScroll)
        }

    @Test
    fun scrollBy_clampsAtMaxScroll() =
        runTest {
            val state = StreamingState()
            state.growTo(maxScroll = 50f, appended = 50)
            state.scrollBy(500f)
            assertEquals(50f, state.currentScroll)
        }

    @Test
    fun onDataChanged_whileFollowing_tracksLatestAndKeepsPendingAtZero() =
        runTest {
            val state = StreamingState()
            state.growTo(maxScroll = 10f, appended = 10)
            state.growTo(maxScroll = 15f, appended = 5)
            assertEquals(15f, state.currentScroll)
            assertEquals(15f, state.maxScroll)
            assertEquals(0, state.pendingCount)
            assertTrue(state.isFollowing)
        }

    @Test
    fun onDataChanged_whileDetached_holdsPositionAndAccumulatesPending() =
        runTest {
            val state = StreamingState()
            state.growTo(maxScroll = 20f, appended = 20)
            state.scrollBy(-15f)
            state.growTo(maxScroll = 23f, appended = 3)
            state.growTo(maxScroll = 27f, appended = 4)
            assertEquals(5f, state.currentScroll)
            assertEquals(7, state.pendingCount)
            assertFalse(state.isFollowing)
        }

    @Test
    fun onDataChanged_whileDetachedWithoutGrowth_leavesPendingUntouched() =
        runTest {
            val state = StreamingState()
            state.growTo(maxScroll = 20f, appended = 20)
            state.scrollBy(-5f)
            state.growTo(maxScroll = 20f, appended = 0)
            assertEquals(0, state.pendingCount)
            assertEquals(15f, state.currentScroll)
        }

    @Test
    fun jumpToLatest_resumesFollowingAndClearsPending() =
        runTest {
            val state = StreamingState()
            state.growTo(maxScroll = 30f, appended = 30)
            state.scrollBy(-25f)
            state.growTo(maxScroll = 34f, appended = 4)
            state.jumpToLatest(Animation.Disabled)
            assertTrue(state.isFollowing)
            assertEquals(0, state.pendingCount)
            assertEquals(34f, state.currentScroll)
        }

    @Test
    fun afterJumpToLatest_newDataIsFollowedAgain() =
        runTest {
            val state = StreamingState()
            state.growTo(maxScroll = 30f, appended = 30)
            state.scrollBy(-20f)
            state.jumpToLatest(Animation.Disabled)
            state.growTo(maxScroll = 36f, appended = 6)
            assertEquals(36f, state.currentScroll)
            assertEquals(0, state.pendingCount)
        }
}
