package com.platform.app.program.resource;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.platform.app.common.json.EntityJsonConverter;
import com.platform.app.common.json.JsonReader;
import com.platform.app.program.model.Application;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ApplicationJsonConverter implements EntityJsonConverter<Application> {

    @Override
    public Application convertFrom(String json) {
        JsonObject jsonObject = JsonReader.readAsJsonObject(json);
        Application app = new Application();
        app.setName(JsonReader.getStringOrNull(jsonObject, "name"));
        app.setDescription(JsonReader.getStringOrNull(jsonObject, "description"));
        app.setURL(JsonReader.getStringOrNull(jsonObject, "URL"));
        app.setInvitationURL(JsonReader.getStringOrNull(jsonObject, "invitationURL"));
        return app;
    }

    @Override
    public JsonElement convertToJsonElement(Application entity) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", entity.getApiKey().toString());
        jsonObject.addProperty("name", entity.getName());
        jsonObject.addProperty("description", entity.getDescription());
        jsonObject.addProperty("URL", entity.getURL());
        jsonObject.addProperty("invitationURL", entity.getInvitationURL());
        return jsonObject;
    }
}
