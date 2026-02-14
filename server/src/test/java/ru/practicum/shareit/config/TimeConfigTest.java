package ru.practicum.shareit.config;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeConfigTest {

    @Test
    void clock_returnsSystemClock() {
        TimeConfig config = new TimeConfig();

        Clock clock = config.clock();

        assertNotNull(clock);
        assertTrue(clock.instant().isBefore(Instant.now().plusSeconds(1)));
    }
}

