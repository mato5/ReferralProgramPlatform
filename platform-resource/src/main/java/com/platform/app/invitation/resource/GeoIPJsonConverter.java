package com.platform.app.invitation.resource;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.platform.app.common.json.EntityJsonConverter;
import com.platform.app.common.json.JsonReader;
import com.platform.app.geoIP.model.GeoIP;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GeoIPJsonConverter implements EntityJsonConverter<GeoIP> {

    @Override
    public GeoIP convertFrom(String json) {
        JsonObject jsonObject = JsonReader.readAsJsonObject(json);
        GeoIP geoIP = new GeoIP();
        if (json == null) {
            return geoIP;
        }
        geoIP.setCity(JsonReader.getStringOrNull(jsonObject, "city"));
        geoIP.setIpAddress(JsonReader.getStringOrNull(jsonObject, "ipAddress"));
        geoIP.setLatitude(JsonReader.getStringOrNull(jsonObject, "latitude"));
        geoIP.setLongitude(JsonReader.getStringOrNull(jsonObject, "longitude"));
        return geoIP;
    }

    @Override
    public JsonElement convertToJsonElement(GeoIP entity) {
        JsonObject jsonObject = new JsonObject();
        if (entity == null) {
            return jsonObject;
        }
        jsonObject.addProperty("city", entity.getCity());
        jsonObject.addProperty("ipAddress", entity.getIpAddress());
        jsonObject.addProperty("latitude", entity.getLatitude());
        jsonObject.addProperty("longitude", entity.getLongitude());
        return jsonObject;
    }

}
