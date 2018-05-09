package com.platform.app.invitation.resource;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.platform.app.common.json.EntityJsonConverter;
import com.platform.app.common.json.JsonReader;
import com.platform.app.geoIP.model.GeoIP;
import com.platform.app.invitation.model.Invitation;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.LocalDateTime;

@ApplicationScoped
public class InvitationJsonConverter implements EntityJsonConverter<Invitation> {

    @Inject
    GeoIPJsonConverter geoIPJsonConverter;

    @Override
    public Invitation convertFrom(String json) {
        JsonObject jsonObject = JsonReader.readAsJsonObject(json);
        Invitation invitation = new Invitation();
        invitation.setProgramId(JsonReader.getLongOrNull(jsonObject, "programId"));
        invitation.setByUserId(JsonReader.getLongOrNull(jsonObject, "byUserId"));
        invitation.setToUserId(JsonReader.getLongOrNull(jsonObject, "toUserId"));
        invitation.setDeclined(Boolean.parseBoolean(JsonReader.getStringOrNull(jsonObject, "declined")));
        String stringForm = JsonReader.getStringOrNull(jsonObject, "activated");
        if (stringForm == null) {
            invitation.setActivated(null);
        } else {
            LocalDateTime activated = LocalDateTime.parse(stringForm);
            invitation.setActivated(activated);
        }
        stringForm = JsonReader.getStringOrNull(jsonObject, "sent");
        if (stringForm == null) {
            invitation.setSent(null);
        } else {
            LocalDateTime sent = LocalDateTime.parse(stringForm);
            invitation.setSent(sent);
        }
        invitation.setInvitationsLeft(JsonReader.getIntegerOrNull(jsonObject, "invitationsLeft"));
        JsonObject obj = jsonObject.getAsJsonObject("activatedLocation");
        GeoIP location;
        if (obj == null) {
            location = new GeoIP();
        } else {
            location = geoIPJsonConverter.convertFrom(obj.getAsString());
        }
        invitation.setActivatedLocation(location);
        return invitation;
    }

    @Override
    public JsonElement convertToJsonElement(Invitation entity) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", entity.getId());
        jsonObject.addProperty("programId", entity.getProgramId());
        jsonObject.addProperty("byUserId", entity.getByUserId());
        jsonObject.addProperty("toUserId", entity.getToUserId());
        jsonObject.addProperty("declined", entity.isDeclined());
        jsonObject.addProperty("invitationsLeft", entity.getInvitationsLeft());
        if (entity.getActivated() != null) {
            jsonObject.addProperty("activated", entity.getActivated().toString());
        } else {
            jsonObject.add("activated", null);
        }
        jsonObject.add("activatedLocation", geoIPJsonConverter.convertToJsonElement(entity.getActivatedLocation()));
        return jsonObject;
    }

}
