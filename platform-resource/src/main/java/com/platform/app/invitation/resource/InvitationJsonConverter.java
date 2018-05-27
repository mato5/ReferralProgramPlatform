package com.platform.app.invitation.resource;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.platform.app.common.json.EntityJsonConverter;
import com.platform.app.common.json.JsonReader;
import com.platform.app.geoIP.model.GeoIP;
import com.platform.app.invitation.model.Invitation;
import com.platform.app.platformUser.model.User;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.*;

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
        if (stringForm != null) {
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
        if (entity.getSent() != null) {
            jsonObject.addProperty("sent", entity.getSent().toString());
        } else {
            jsonObject.add("sent", null);
        }
        jsonObject.add("activatedLocation", geoIPJsonConverter.convertToJsonElement(entity.getActivatedLocation()));
        return jsonObject;
    }

    public JsonElement getTreeChart(List<Invitation> invitations, Set<User> users, String programName) {
        JsonObject treeChart = new JsonObject();
        JsonArray configArray = new JsonArray();
        configArray.add("config");
        JsonObject rootNode = new JsonObject();
        JsonObject rootName = new JsonObject();
        rootName.addProperty("name", programName);
        rootNode.add("text", rootName);
        treeChart.add("rootNode", rootNode);
        Map<String, String> configList = new LinkedHashMap<>();
        configArray.add("rootNode");
        int z = 0;
        User current;
        while (!users.isEmpty()) {

            for (Iterator<User> i = users.iterator(); i.hasNext(); ) {
                current = i.next();
                if (getChildren(current, invitations, users).isEmpty()) {
                    JsonObject node = new JsonObject();
                    final Long id = current.getId();
                    Invitation inv = invitations.stream().filter(x -> x.getToUserId().equals(id))
                            .findAny().orElse(null);
                    if (inv == null) {
                        //THIS IS ADMIN node
                        node.addProperty("parent", "rootNode");
                        JsonObject name = new JsonObject();
                        name.addProperty("name", current.getName());
                        node.add("text", name);
                    } else {
                        User parent = users.stream().filter(x -> x.getId().equals(inv.getByUserId()))
                                .findAny().orElse(null);
                        if (parent != null) {
                            node.addProperty("parent", "invitation" + inv.getByUserId());
                        } else {
                            node.addProperty("parent", "rootNode");
                        }
                        JsonObject name = new JsonObject();
                        name.addProperty("name", current.getName());
                        node.add("text", name);
                        invitations.removeIf(x -> x.getToUserId().equals(id));
                    }
                    if (inv != null) {
                        treeChart.add("invitation" + inv.getToUserId(), node);
                        configList.put(current.getEmail(), "invitation" + inv.getToUserId());
                    } else {
                        treeChart.add("invitation" + current.getId(), node);
                        configList.put(current.getEmail(), "invitation" + current.getId());
                    }
                    i.remove();
                    z++;
                }
            }
        }
        List<String> toReverse = new ArrayList<>(configList.values());
        Collections.reverse(toReverse);
        for (String item : toReverse) {
            configArray.add(item);
        }
        treeChart.add("chart_config", configArray);
        return treeChart;
    }

    private Set<User> getChildren(User user, List<Invitation> invitations, Set<User> users) {
        List<Invitation> temp = new ArrayList<>();
        for (Invitation inv : invitations) {
            if (inv.getByUserId().equals(user.getId())) {
                temp.add(inv);
            }
        }
        Set<User> children = new HashSet<>();
        for (Invitation inv : temp) {
            for (User u : users) {
                if (inv.getToUserId().equals(u.getId())) {
                    children.add(u);
                }
            }
        }
        return children;
    }

}
