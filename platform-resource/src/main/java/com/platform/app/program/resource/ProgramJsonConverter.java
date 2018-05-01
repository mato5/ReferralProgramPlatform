package com.platform.app.program.resource;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.platform.app.common.json.EntityJsonConverter;
import com.platform.app.common.json.JsonReader;
import com.platform.app.platformUser.model.User;
import com.platform.app.program.model.Application;
import com.platform.app.program.model.Program;
import com.platform.app.program.model.WaitingList;
import com.platform.app.user.resource.UserJsonConverter;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
public class ProgramJsonConverter implements EntityJsonConverter<Program> {

    @Inject
    ApplicationJsonConverter applicationJsonConverter;

    @Inject
    UserJsonConverter userJsonConverter;

    @Inject
    WaitingListJsonConverter waitingListJsonConverter;

    @Override
    public Program convertFrom(String json) {
        JsonObject jsonObject = JsonReader.readAsJsonObject(json);
        Program program = new Program();
        program.setName(JsonReader.getStringOrNull(jsonObject, "name"));
        Set<User> admins = new HashSet<>();
        JsonArray array = jsonObject.getAsJsonArray("admins");
        for (int i = 0; i < array.size(); i++) {
            admins.add(userJsonConverter.convertFrom(array.get(i).getAsString()));
        }
        program.setAdmins(admins);
        Set<User> customers = new HashSet<>();
        array = jsonObject.getAsJsonArray("activeCustomers");
        for (int i = 0; i < array.size(); i++) {
            customers.add(userJsonConverter.convertFrom(array.get(i).getAsString()));
        }
        program.setActiveCustomers(customers);
        Set<Application> apps = new HashSet<>();
        array = jsonObject.getAsJsonArray("activeApplications");
        for (int i = 0; i < array.size(); i++) {
            apps.add(applicationJsonConverter.convertFrom(array.get(i).getAsString()));
        }
        program.setActiveApplications(apps);
        WaitingList list = waitingListJsonConverter.convertFrom(JsonReader.getStringOrNull(jsonObject, "waitingList"));
        program.setWaitingList(list);
        return program;
    }

    @Override
    public JsonElement convertToJsonElement(Program entity) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", entity.getName());
        jsonObject.add("admins", userJsonConverter.convertToJsonElement(new ArrayList<>(entity.getAdmins())));
        jsonObject.add("activeCustomers", userJsonConverter.convertToJsonElement(new ArrayList<>(entity.getActiveCustomers())));
        jsonObject.add("activeApplications", applicationJsonConverter.convertToJsonElement(new ArrayList<>(entity.getActiveApplications())));
        jsonObject.add("waitingList", waitingListJsonConverter.convertToJsonElement(entity.getWaitingList()));
        return jsonObject;
    }

}
