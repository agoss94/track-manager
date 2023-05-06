package com.github.agoss94.track.manager;

import java.time.LocalTime;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;

/**
 * A track is a timetable of non-overlapping events. As such a track maps points
 * in time to events.
 */
public class Track {

    /**
     * The timetable is represented by a map.
     */
    private final NavigableMap<LocalTime, Event> track;

    /**
     * Creates an empty track.
     */
    public Track() {
        track = new TreeMap<>();
    }

    /**
     * Associates the start time with the given event. Returns the previous mapping
     * for the key or {@code null} if none was present.
     *
     * @param start the start time.
     * @param e     the event.
     * @return the previously associated event.
     * @throws NullPointerException if the start time or event is {@code null}.
     */
    public void put(LocalTime start, Event e) {
        Objects.requireNonNull(start);
        Objects.requireNonNull(e);
        if (endPrevious(start).isAfter(start)) {
            throw new PreviousEventException(
                    "Cannot add event to track. There is an ongoing event until " + endPrevious(start));
        }
        LocalTime nextIn = track.higherKey(start);
        if (nextIn != null) {
            if (e.isOpenEnd()) {
                throw new FutureEventException("Cannot add an open end event before a future event.");
            } else if (nextIn.isBefore(start.plus(e.getDuration()))) {
                throw new FutureEventException(
                        String.format("Cannot add event of %smin at %s, because the next event starts at %s",
                                e.getDuration().toMinutes(), start, nextIn));
            }
        }
        track.put(start, e);
    }

    /**
     * Returns the end time for the event, which started equal to or directly before
     * the given time. If the previous event is open end {@link LocalTime#MAX} is
     * returned. If the track is empty {@link LocalTime#MIN} is returned.
     *
     * @param time the given time.
     * @return the end time the previous event or {@link LocalTime#MIN} if the track
     *         is empty or {@link LocalTime#MAX} if the last event is open end.
     * @throws NullPointerException if time is {@code null}.
     */
    public LocalTime endPrevious(LocalTime time) {
        Objects.requireNonNull(time);
        Entry<LocalTime, Event> entry = track.floorEntry(time);
        if (entry != null) {
            LocalTime start = entry.getKey();
            Event event = entry.getValue();
            return event.isOpenEnd() ? LocalTime.MAX : start.plus(event.getDuration());
        } else {
            return LocalTime.MIN;
        }
    }
}