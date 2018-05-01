package com.platform.app.program.resource;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.platform.app.common.json.EntityJsonConverter;
import com.platform.app.common.json.JsonReader;
import com.platform.app.program.model.WaitingList;

import java.util.SortedSet;
import java.util.TreeSet;

public class WaitingListJsonConverter implements EntityJsonConverter<WaitingList> {

    @Override
    public WaitingList convertFrom(String json) {
        JsonObject jsonObject = JsonReader.readAsJsonObject(json);
        Gson gson = new Gson();
        WaitingList list = new WaitingList();
        SortedSet<String> set = gson.fromJson(jsonObject.get("list"), TreeSet.class);
        list.setList(set);
        return list;
    }

    @Override
    public JsonElement convertToJsonElement(WaitingList entity) {
        JsonObject jsonObject = new JsonObject();
        Gson gson = new Gson();
        String sortedJson = gson.toJson(entity.getList());
        jsonObject.addProperty("list", sortedJson);
        return jsonObject;
    }
}
