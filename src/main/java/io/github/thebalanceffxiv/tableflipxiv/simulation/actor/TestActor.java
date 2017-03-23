package io.github.thebalanceffxiv.tableflipxiv.simulation.actor;

import io.github.thebalanceffxiv.tableflipxiv.simulation.SimulationStepTracker;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

public class TestActor {
    private final SimulationStepTracker stepTracker;

    public TestActor(SimulationStepTracker stepTracker) {
        this.stepTracker = stepTracker;

        stepTracker.subscribe("gcd", this::onGCD);

        stepTracker.timedEvent("gcd", Duration.ZERO);
    }

    private List<SimulationStepTracker.TimedEvent> onGCD() {

        // TODO: Better interface pattern
        SimulationStepTracker.TimedEvent nextGCD = new SimulationStepTracker.TimedEvent("gcd", stepTracker.createFutureOffset(Duration.ofMillis(2500)));

        return Collections.singletonList(nextGCD);
    }
}
