package com.nicholasnassar.sfsapi.data.assignments;

import com.nicholasnassar.sfsapi.data.links.AssignmentLink;
import com.nicholasnassar.sfsapi.data.links.Link;
import io.vertx.core.json.JsonObject;

public class AssignmentTaskList {
    private final Link link;

    private final String clazz, activity, dateDue;

    private final int resources;

    public AssignmentTaskList(String id, String clazz, String activity, String dateDue, int resources) {
        link = new AssignmentLink(id);

        this.clazz = clazz;

        this.activity = activity.replace("Assignment: ", "");

        this.dateDue = dateDue;

        this.resources = resources;
    }

    public JsonObject asJson() {
        return new JsonObject().put("link", link.asJson()).put("class", clazz).put("activity", activity)
                .put("date_due", dateDue).put("resources", resources).put("resource", "");
    }
}
