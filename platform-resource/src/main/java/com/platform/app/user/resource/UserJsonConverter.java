package com.platform.app.user.resource;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.platform.app.common.json.EntityJsonConverter;
import com.platform.app.common.json.JsonReader;
import com.platform.app.common.utils.DateUtils;
import com.platform.app.platformUser.model.Admin;
import com.platform.app.platformUser.model.Customer;
import com.platform.app.platformUser.model.User;
import com.platform.app.platformUser.model.User.Roles;
import com.platform.app.platformUser.model.User.UserType;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class UserJsonConverter implements EntityJsonConverter<User> {

    @Override
    public User convertFrom(final String json) {
        final JsonObject jsonObject = JsonReader.readAsJsonObject(json);

        final User user = getUserInstance(jsonObject);
        user.setName(JsonReader.getStringOrNull(jsonObject, "name"));
        user.setEmail(JsonReader.getStringOrNull(jsonObject, "email"));
        user.setPassword(JsonReader.getStringOrNull(jsonObject, "password"));

        return user;
    }

    public List<Long> convertIds(String json) {
        List<Long> ids = new ArrayList<>();
        JsonArray jsonArray = JsonReader.readAsJsonArray(json);
        for (int i = 0; i < jsonArray.size(); i++) {
            ids.add(jsonArray.get(i).getAsLong());
        }
        return ids;
    }

    public List<String> convertEmails(String json) {
        List<String> emails = new ArrayList<>();
        JsonArray jsonArray = JsonReader.readAsJsonArray(json);
        for (int i = 0; i < jsonArray.size(); i++) {
            emails.add(jsonArray.get(i).getAsString());
        }
        return emails;
    }

    @Override
    public JsonElement convertToJsonElement(final User user) {
        final JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("id", user.getId());
        jsonObject.addProperty("name", user.getName());
        jsonObject.addProperty("email", user.getEmail());
        jsonObject.addProperty("type", user.getUserType().toString());
        final JsonArray roles = new JsonArray();
        for (final Roles role : user.getRoles()) {
            roles.add(new JsonPrimitive(role.toString()));
        }
        jsonObject.add("roles", roles);
        jsonObject.addProperty("createdAt", DateUtils.formatDateTime(user.getCreatedAt()));

        return jsonObject;
    }

    private User getUserInstance(final JsonObject userJson) {
        final UserType userType = UserType.valueOf(JsonReader.getStringOrNull(userJson, "type"));
        if (UserType.EMPLOYEE.equals(userType)) {
            return new Admin();
        }
        return new Customer();
    }

}