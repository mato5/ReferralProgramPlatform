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
import java.time.format.DateTimeParseException;

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
        invitation.setInvitationsLeft(JsonReader.getIntegerOrNull(jsonObject, "invitationsLeft"));
        try {
            LocalDateTime activated = LocalDateTime.parse((JsonReader.getStringOrNull(jsonObject, "activated")));
            invitation.setActivated(activated);
        } catch (DateTimeParseException ex) {
            invitation.setActivated(null);
        }
        GeoIP location = geoIPJsonConverter.convertFrom(jsonObject.getAsJsonObject("activatedLocation").getAsString());
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
        jsonObject.addProperty("activated", entity.getActivated().toString());
        jsonObject.add("activatedLocation", geoIPJsonConverter.convertToJsonElement(entity.getActivatedLocation()));
        return jsonObject;
    }

}
