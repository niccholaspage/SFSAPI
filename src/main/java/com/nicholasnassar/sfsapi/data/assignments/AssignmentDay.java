package com.nicholasnassar.sfsapi.data.assignments;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

public class AssignmentDay {
    private final String date;

    private final List<AssignmentNameAndLink> assignments;

    public AssignmentDay(String date, List<AssignmentNameAndLink> assignments) {
        this.date = date;

        this.assignments = assignments;
    }

    public JsonObject asJson() {
        JsonObject obj = new JsonObject().put("date", date);

        JsonArray assignmentsArray = new JsonArray();

        for (AssignmentNameAndLink assignment : assignments) {
            assignmentsArray.add(assignment.asJson());
        }

        obj.put("assignments", assignmentsArray);

        return obj;
    }
}
