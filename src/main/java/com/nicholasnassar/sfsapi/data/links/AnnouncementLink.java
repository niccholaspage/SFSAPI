package com.nicholasnassar.sfsapi.data.links;

import io.vertx.core.json.JsonObject;

public class AnnouncementLink extends Link {
    private final String id;

    public AnnouncementLink(String id) {
        super(LinkType.ANNOUNCEMENT);

        this.id = id;
    }

    @Override
    public JsonObject asJson() {
        return new JsonObject().put("type", type.getName()).put("id", id);
    }
}
