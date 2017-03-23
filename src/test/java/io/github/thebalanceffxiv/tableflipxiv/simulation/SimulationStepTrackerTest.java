package io.github.thebalanceffxiv.tableflipxiv.simulation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class SimulationStepTrackerTest {
    private SimulationStepTracker testInstance;

    @BeforeEach
    void setUp() {
        testInstance = new SimulationStepTracker();

    }

    @AfterEach
    void tearDown() {
        testInstance = null;
    }

    @Test
    void FirstTimeStepShouldBeZero() {
        assertEquals(Duration.ZERO, testInstance.getTime());
    }

    @Test
    void FirstTimeStepPreviousDurationShouldBeZero() {
        assertEquals(Duration.ZERO, testInstance.getLastStepDuration());
    }

    @Test
    void ShouldBeAbleToCreateOffsetToCurrent() {
        Duration testDurationOrigin = Duration.ofMillis(new Random().nextInt(50000) + 1);
        Duration testDurationOffset = Duration.ofMillis(new Random().nextInt(50000) + 1);

        testInstance.timedEvent("testEvent", testDurationOrigin);
        testInstance.run();

        assertEquals(testDurationOrigin.plus(testDurationOffset), testInstance.createFutureOffset(testDurationOffset));
    }

    @Test
    void ShouldHaltWithoutEntries() throws InterruptedException {
        Thread testThread = new Thread(testInstance);
        testThread.start();
        testThread.join(2000);
        assertFalse(testThread.isAlive());
    }

    @Test
    void ShouldCallEventCallbacks() {
        // TODO: hacky way
        final int[] callCount = {0};
        testInstance.subscribe("testEvent", () -> {
            callCount[0]++;
            return Collections.emptyList();
        });
        testInstance.timedEvent("testEvent", Duration.ofSeconds(2));

        testInstance.run();
        assertEquals(1, callCount[0]);
    }

    @Test
    void ShouldTackTimesCorrectly() {
        Duration testDuration = Duration.ofMillis(new Random().nextInt(50000) + 1);

        testInstance.timedEvent("testEvent", testDuration);

        assertEquals(Duration.ZERO, testInstance.getTime());
        assertEquals(Duration.ZERO, testInstance.getLastStepDuration());

        testInstance.run();

        assertEquals(testDuration, testInstance.getTime());
        assertEquals(testDuration, testInstance.getLastStepDuration());
    }
}