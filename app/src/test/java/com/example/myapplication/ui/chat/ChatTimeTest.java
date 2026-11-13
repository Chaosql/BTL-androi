package com.example.myapplication.ui.chat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class ChatTimeTest {
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-11T17:05:00Z"), ZoneOffset.UTC);

    @Test public void todayUsesVietnamCalendarDate() {
        assertEquals("00:01", ChatUi.time("2026-11-11T17:01:00Z", clock));
        assertEquals("00:01", ChatUi.time("2026-11-12T00:01:00+07:00", clock));
    }

    @Test public void previousDayIncludesFullDateAfterVietnamMidnight() {
        assertEquals("23:59, 11/11/2026", ChatUi.time("2026-11-11T16:59:00Z", clock));
    }

    @Test public void previousYearAndUtcWithoutOffsetIncludeFullDate() {
        assertEquals("07:00, 31/12/2025", ChatUi.time("2025-12-31 00:00:00", clock));
    }

    @Test public void missingAndInvalidTimestampsStayEmpty() {
        assertEquals("", ChatUi.time(null, clock));
        assertEquals("", ChatUi.time("invalid", clock));
    }
}
