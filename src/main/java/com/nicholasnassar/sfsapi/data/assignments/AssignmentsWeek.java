package com.nicholasnassar.sfsapi.data.assignments;

import io.vertx.core.json.JsonArray;

import java.util.List;

public class AssignmentsWeek {
    private final List<AssignmentDay> days;

    public AssignmentsWeek(List<AssignmentDay> days) {
        this.days = days;
    }

    public JsonArray asJsonArray() {
        JsonArray array = new JsonArray();

        for (AssignmentDay day : days) {
            array.add(day.asJson());
        }

        return array;
    }
}
