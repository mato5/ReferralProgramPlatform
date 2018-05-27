package com.platform.app.program.resource;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.platform.app.common.json.EntityJsonConverter;
import com.platform.app.common.json.JsonReader;
import com.platform.app.program.model.WaitingList;

import javax.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;

@ApplicationScoped
public class WaitingListJsonConverter implements EntityJsonConverter<WaitingList> {

    @Override
    public WaitingList convertFrom(String json) {
        if (json == null) {
            return new WaitingList();
        }
        JsonObject jsonObject = JsonReader.readAsJsonObject(json);
        Gson gson = new Gson();
        WaitingList list = new WaitingList();
        Map<Long, LocalDateTime> sortedMap = gson.fromJson(jsonObject.get("list"), LinkedHashMap.class);
        for (Map.Entry<Long, LocalDateTime> entry : sortedMap.entrySet()) {
            Instant instant = entry.getValue().atZone(ZoneId.systemDefault()).toInstant();
            list.subscribe(entry.getKey(), instant);
        }
        return list;
    }

    @Override
    public JsonElement convertToJsonElement(WaitingList entity) {
        JsonObject jsonObject = new JsonObject();
        JsonArray orderArray = new JsonArray();
        Gson gson = new Gson();
        Map<Long, LocalDateTime> result = new LinkedHashMap<>();
        for (Map.Entry<Long, Instant> entry : entity.getOrderedList().entrySet()) {
            LocalDateTime subscriptionTime = LocalDateTime.ofInstant(entry.getValue(), ZoneId.systemDefault());
            result.put(entry.getKey(), subscriptionTime);
            orderArray.add(entry.getKey());
        }
        JsonElement sortedJson = gson.toJsonTree(result);
        jsonObject.add("list", sortedJson);
        jsonObject.add("order", orderArray);
        jsonObject.addProperty("size", entity.getList().size());
        return jsonObject;
    }
}
