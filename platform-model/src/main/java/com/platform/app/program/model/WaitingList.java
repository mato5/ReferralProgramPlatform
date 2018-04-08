package com.platform.app.program.model;

import javax.persistence.*;
import java.util.*;

/**
 * A class representing a waiting list for a program, where key represents time the user subscribed and value their ID
 */
@Embeddable
public class WaitingList {

    @ElementCollection
    @OrderBy
    private SortedMap<Date, Long> list = new TreeMap<>();

    public SortedMap<Date, Long> getList() {
        return list;
    }

    public void setList(SortedMap<Date, Long> list) {
        this.list = list;
    }

    public void subscribe(Long id) {
        Date subscriptionTime = new Date();
        if (!list.containsValue(id)) {
            list.put(subscriptionTime, id);
        }
    }

    public List<Long> getOrderedIds(){
        return new ArrayList<>(list.values());
    }

    public void unsubscribe(Long id) {
        if (id == null) {
            return;
        }
        Date toBeRemoved = null;
        for (Map.Entry<Date, Long> entry : list.entrySet()) {
            if (entry.getValue().equals(id)) {
                toBeRemoved = entry.getKey();
            }
        }
        if (toBeRemoved != null) {
            list.remove(toBeRemoved);
        }
    }

}
