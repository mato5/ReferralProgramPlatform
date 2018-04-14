package com.platform.app.program.model;

import javax.persistence.*;
import java.time.Instant;
import java.util.*;

/**
 * A class representing a waiting list for a program, where key represents time the user subscribed and value their ID
 */
@Embeddable
public class WaitingList {

    @ElementCollection
    @OrderBy
    private SortedMap<Instant, Long> list = new TreeMap<>();

    private int internalCounter = 1;

    public SortedMap<Instant, Long> getList() {
        return list;
    }

    public void setList(SortedMap<Instant, Long> list) {
        this.list = list;
    }

    public void subscribe(Long id) {
        Instant subscriptionTime = Instant.now();
        if (!list.containsValue(id)) {
            if (list.containsKey(subscriptionTime)) {
                if (internalCounter > 100000) {
                    internalCounter = 1;
                }
                subscriptionTime = subscriptionTime.plusNanos(internalCounter);
                internalCounter++;
            }
            list.put(subscriptionTime, id);
        }
    }

    public List<Long> getOrderedIds() {
        return new ArrayList<>(list.values());
    }

    public void unsubscribe(Long id) {
        if (id == null) {
            return;
        }
        Instant toBeRemoved = null;
        for (Map.Entry<Instant, Long> entry : list.entrySet()) {
            if (entry.getValue().equals(id)) {
                toBeRemoved = entry.getKey();
            }
        }
        if (toBeRemoved != null) {
            list.remove(toBeRemoved);
        }
    }

}

