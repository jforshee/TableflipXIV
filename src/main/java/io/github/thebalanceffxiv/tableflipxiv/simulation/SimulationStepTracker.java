package io.github.thebalanceffxiv.tableflipxiv.simulation;

import com.google.common.collect.HashMultimap;
import io.github.thebalanceffxiv.tableflipxiv.helpers.CompletableFutureHelper;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

// TODO: needs to be concurrency safe
public class SimulationStepTracker implements Runnable {
    private Duration time = Duration.ZERO;
    private Duration lastStepDuration = Duration.ZERO;

    private PriorityQueue<TimedEvent> timedEvents = new PriorityQueue<>();
    private HashMultimap<String, Supplier<List<TimedEvent>>> eventSubscribers = HashMultimap.create();

    /**
     * @return Current Step Time
     */
    public Duration getTime() {
        return time;
    }

    /**
     * @return Last simulation step eventTime
     */
    public Duration getLastStepDuration() {
        return lastStepDuration;
    }

    /**
     * Helper function to generate Duration timestamps relative to the current simulation time step
     * @param offset Offset from current step time
     * @return offset step time
     */
    public Duration createFutureOffset(Duration offset) {
        return time.plus(offset);
    }

    /**
     * Inject a new event for the simulation to keep track of
     * @param event Event identifier
     * @param time Simulation time to be notified
     */
    public void timedEvent(String event, Duration time) {
        timedEvents.offer(new TimedEvent(event, time));
    }

    /**
     * Subscribe a method to be executed when event occurs
     * @param event Event identifier
     * @param callback Callback function, returning a List of Events to be added to the tracker
     */
    public void subscribe(String event, Supplier<List<TimedEvent>> callback) {
        eventSubscribers.put(event, callback);
    }

    /**
     * Remove a method to be notified by specified event
     * @param event Event identifier
     * @param callback Callback function to be removed
     */
    public void unsubscribe(String event, Supplier<List<TimedEvent>> callback) {
        eventSubscribers.remove(event, callback);
    }

    @Override
    public void run() {
        // TODO: further kill conditions?
        while(!timedEvents.isEmpty()) {
            TimedEvent nextEvent = timedEvents.poll();

            lastStepDuration = nextEvent.eventTime.minus(time);
            time = nextEvent.eventTime;

            List<CompletableFuture<List<TimedEvent>>> promises = new LinkedList<>();
            eventSubscribers.get(nextEvent.event).forEach(supplier -> promises.add(CompletableFuture.supplyAsync(supplier)));
            CompletableFutureHelper.sequence(promises).thenAccept(lists -> lists.forEach(suppliedEvents -> suppliedEvents.forEach(timedEvent -> this.timedEvents.offer(timedEvent))));
        }
    }

    /**
     * Event Representation for Simulation Step tracking
     */
    public static class TimedEvent implements Comparable<TimedEvent> {
        public TimedEvent(String event, Duration eventTime) {
            this.eventTime = eventTime;
            this.event = event;
        }

        private Duration eventTime;
        private String event;

        /**
         * @return Time the Event is Scheduled in the simulation
         */
        public Duration getEventTime() {
            return eventTime;
        }

        /**
         * @return Event
         */
        public String getEvent() {
            return event;
        }

        @Override
        public int compareTo(TimedEvent other) {
            return this.eventTime.compareTo(other.eventTime);
        }
    }
}
