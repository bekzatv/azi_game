package com.example.network

import org.junit.Assert.*
import org.junit.Test

class RoomCodeTest {
    @Test fun generatedCodesAreDistinctAndRoundTrip() {
        val codes = (1..1000).map { RoomCode.generate() }
        assertEquals(1000, codes.toSet().size)
        codes.forEach { assertEquals(it, RoomCode.normalize(it)) }
    }
    @Test fun invitationAndLowercaseAreAccepted() {
        val code = "AZI-ABCDEFGH23"
        assertEquals(code, RoomCode.normalize(RoomCode.invitation(code).lowercase()))
        assertEquals(code, RoomCode.normalize(" azi abcdefgh23 "))
    }
    @Test fun shortAmbiguousOrOverlongCodesAreRejected() {
        listOf("", "AZI-777", "AZI-ABCDEFGHI1", "AZI-ABCDEFGH234", "xAZI-ABCDEFGH23").forEach {
            assertNull(it, RoomCode.normalize(it))
        }
    }
}
