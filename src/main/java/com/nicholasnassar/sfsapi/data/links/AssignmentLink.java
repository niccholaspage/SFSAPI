package com.nicholasnassar.sfsapi.data.links;

import io.vertx.core.json.JsonObject;

public class AssignmentLink extends Link {
    private final String id;

    public AssignmentLink(String id) {
        super(LinkType.ASSIGNMENT);

        this.id = id;
    }

    @Override
    public JsonObject asJson() {
        return new JsonObject().put("type", type.getName()).put("id", id);
    }
}
