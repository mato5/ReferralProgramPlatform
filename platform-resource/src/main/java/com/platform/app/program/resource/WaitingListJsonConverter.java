package com.platform.app.program.resource;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.platform.app.common.json.EntityJsonConverter;
import com.platform.app.common.json.JsonReader;
import com.platform.app.program.model.WaitingList;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public class WaitingListJsonConverter implements EntityJsonConverter<WaitingList> {

    @Override
    public WaitingList convertFrom(String json) {
        JsonObject jsonObject = JsonReader.readAsJsonObject(json);
        Gson gson = new Gson();
        WaitingList list = new WaitingList();
        Map<Long, Instant> sortedMap = gson.fromJson(jsonObject.get("list"), LinkedHashMap.class);
        for (Map.Entry<Long, Instant> entry : sortedMap.entrySet()) {
            list.subscribe(entry.getKey(), entry.getValue());
        }
        return list;
    }

    @Override
    public JsonElement convertToJsonElement(WaitingList entity) {
        JsonObject jsonObject = new JsonObject();
        Gson gson = new Gson();
        String sortedJson = gson.toJson(entity.getOrderedList());
        jsonObject.addProperty("list", sortedJson);
        return jsonObject;
    }
}
