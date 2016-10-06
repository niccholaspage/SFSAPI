package com.nicholasnassar.sfsapi.data.assignments;

import com.nicholasnassar.sfsapi.data.links.Link;
import com.nicholasnassar.sfsapi.data.links.LinkType;
import io.vertx.core.json.JsonObject;

public class AssignmentAll {
    private final Link link;

    private final String due, clazz, assignment, notes, resources;

    public AssignmentAll(String id, String due, String clazz, String assignment, String notes, String resources) {
        link = new Link(LinkType.ASSIGNMENT, id);

        this.due = due;

        this.clazz = clazz;

        this.assignment = assignment;

        this.notes = notes;

        this.resources = resources;
    }

    public JsonObject asJson() {
        return new JsonObject().put("link", link.asJson()).put("due", due).put("class", clazz).put("assignment",
                assignment).put("notes", notes).put("resources", resources);
    }
}
