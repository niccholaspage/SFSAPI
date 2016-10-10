package com.nicholasnassar.sfsapi.data.assignments;

import com.nicholasnassar.sfsapi.data.links.Resource;
import io.vertx.core.json.JsonObject;

import java.util.List;

public class FullAssignment {
    private final String activity, clazz, instructor, assigned, due, possiblePoints, category, notes;

    private final List<Resource> resources;

    public FullAssignment(String activity, String clazz, String instructor, String assigned, String due,
                          String possiblePoints, String category, String notes, List<Resource> resources) {
        this.activity = activity;

        this.clazz = clazz;

        this.instructor = instructor;

        this.assigned = assigned;

        this.due = due;

        this.possiblePoints = possiblePoints;

        this.category = category;

        this.notes = notes;

        this.resources = resources;
    }

    public JsonObject asJson() {
        return new JsonObject().put("activity", activity).put("class", clazz).put("instructor", instructor)
                .put("assigned", assigned).put("due", due).put("possible_points", possiblePoints)
                .put("category", category).put("notes", notes).put("resources", Resource.asJsonArray(resources));
    }
}
