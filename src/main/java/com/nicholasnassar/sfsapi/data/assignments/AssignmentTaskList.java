package com.nicholasnassar.sfsapi.data.assignments;

import com.nicholasnassar.sfsapi.data.links.Link;
import com.nicholasnassar.sfsapi.data.links.LinkType;
import io.vertx.core.json.JsonObject;

public class AssignmentTaskList {
    private final Link link;

    private final String clazz, activity, dateDue;

    private final int resources;

    public AssignmentTaskList(String id, String clazz, String activity, String dateDue, int resources) {
        link = new Link(LinkType.ASSIGNMENT, id);

        this.clazz = clazz;

        this.activity = activity.replace("Assignment: ", "");

        this.dateDue = dateDue;

        this.resources = resources;
    }

    public JsonObject asJson() {
        return new JsonObject().put("link", link.asJson()).put("class", clazz).put("activity", activity)
                .put("date_due", dateDue).put("resources", resources);
    }
}
