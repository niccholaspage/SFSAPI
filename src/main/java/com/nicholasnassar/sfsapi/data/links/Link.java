package com.nicholasnassar.sfsapi.data.links;

import io.vertx.core.json.JsonObject;

public class Link {
    private final LinkType type;

    private final String id;

    public Link(LinkType type, String id) {
        this.type = type;

        this.id = id;
    }

    public JsonObject asJson() {
        return new JsonObject().put("type", type.getName()).put("id", id);
    }
}
