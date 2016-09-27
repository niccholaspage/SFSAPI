package com.nicholasnassar.sfsapi.data.assignments;

import io.vertx.core.json.JsonObject;

public class FullAssignment {
    private final String activity, clazz, instructor, assigned, due, possiblePoints, category, notes, resourceName, resourceLink;

    public FullAssignment(String activity, String clazz, String instructor, String assigned, String due,
                          String possiblePoints, String category, String notes, String resourceName, String resourceLink) {
        this.activity = activity;

        this.clazz = clazz;

        this.instructor = instructor;

        this.assigned = assigned;

        this.due = due;

        this.possiblePoints = possiblePoints;

        this.category = category;

        this.notes = notes;

        this.resourceName = resourceName;

        this.resourceLink = resourceLink;
    }

    public JsonObject asJson() {
        return new JsonObject().put("activity", activity).put("class", clazz).put("instructor", instructor)
                .put("assigned", assigned).put("due", due).put("possible_points", possiblePoints)
                .put("category", category).put("notes", notes).put("resource_name", resourceName)
                .put("resource_link", resourceLink);
    }
}
