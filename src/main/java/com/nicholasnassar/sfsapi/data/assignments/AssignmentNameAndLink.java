package com.nicholasnassar.sfsapi.data.assignments;

import com.nicholasnassar.sfsapi.data.links.Link;
import io.vertx.core.json.JsonObject;

public class AssignmentNameAndLink {
    private final String name;

    private final Link link;

    public AssignmentNameAndLink(String name, Link link) {
        this.name = name;

        this.link = link;
    }

    public JsonObject asJson() {
        return new JsonObject().put("name", name).put("link", link.asJson());
    }
}
