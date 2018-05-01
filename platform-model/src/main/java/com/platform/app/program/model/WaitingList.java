package com.platform.app.program.model;

import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.OrderBy;
import java.time.Instant;
import java.util.*;

/**
 * A class representing a waiting list for a program, where key represents time the user subscribed and value their ID
 */
@Embeddable
public class WaitingList {

    @ElementCollection
    @OrderBy
    private SortedSet<String> list = new TreeSet<>(TimeComparator);

    public SortedSet<String> getList() {
        return list;
    }

    public void setList(SortedSet<String> list) {
        this.list = list;
    }

    public void subscribe(Long id) {
        Instant subscriptionTime = Instant.now();
        String item = subscriptionTime.toString() + "/" + id;
        list.add(item);
    }

    public List<Long> getOrderedIds() {
        List<Long> ids = new ArrayList<>();
        for (String item : list) {
            String[] parts = item.split("/");
            ids.add(Long.valueOf(parts[1]));
        }
        return ids;
    }

    public void unsubscribe(Long id) {
        if (id == null) {
            return;
        }
        String toBeRemoved = null;
        for (String item : list) {
            String[] parts = item.split("/");
            if (id.equals(Long.valueOf(parts[1]))) {
                toBeRemoved = item;
                break;
            }
        }
        list.remove(toBeRemoved);
    }

    public Map<Long, Instant> getOrderedList() {
        Map<Long, Instant> result = new LinkedHashMap<>();
        for (String item : list) {
            String[] parts1 = item.split("/");
            Instant i1 = Instant.parse(parts1[0]);
            Long id1 = Long.valueOf(parts1[1]);
            result.put(id1, i1);
        }
        return result;
    }

    private static Comparator<String> TimeComparator = new Comparator<String>() {
        @Override
        public int compare(String s1, String s2) {
            String[] parts1 = s1.split("/");
            String[] parts2 = s2.split("/");
            Instant i1 = Instant.parse(parts1[0]);
            Instant i2 = Instant.parse(parts2[0]);
            if (i2.isAfter(i1)) {
                return -1;
            } else if (i1.isAfter(i2)) {
                return 1;
            } else if (s1.equals(s2)) {
                return 0;
            } else {
                Long id1 = Long.valueOf(parts1[1]);
                Long id2 = Long.valueOf(parts2[1]);
                if (id2 >= id1) {
                    return -1;
                } else return 1;
            }
        }
    };

}

