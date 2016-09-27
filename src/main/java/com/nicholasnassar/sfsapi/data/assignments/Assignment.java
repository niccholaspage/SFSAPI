package com.nicholasnassar.sfsapi.data.assignments;

import com.nicholasnassar.sfsapi.data.links.Link;
import com.nicholasnassar.sfsapi.data.links.LinkType;
import io.vertx.core.json.JsonObject;

public class Assignment {
    private final Link link;

    private final String clazz, activity, dateDue, resource;

    public Assignment(String id, String clazz, String activity, String dateDue, String resource) {
        link = new Link(LinkType.ASSIGNMENT, id);

        this.clazz = clazz;

        this.activity = activity.replace("Assignment: ", "");

        this.dateDue = dateDue;

        this.resource = resource;
    }

    public JsonObject asJson() {
        return new JsonObject().put("link", link.asJson()).put("class", clazz).put("activity", activity)
                .put("date_due", dateDue).put("resource", resource);
    }
}
