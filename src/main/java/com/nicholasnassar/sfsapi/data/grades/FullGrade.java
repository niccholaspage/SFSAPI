package com.nicholasnassar.sfsapi.data.grades;

import io.vertx.core.json.JsonObject;

import java.util.List;

public class FullGrade {
    private final List<GradeAssignment> assignments;

    public FullGrade(List<GradeAssignment> assignments) {
        this.assignments = assignments;
    }

    public List<GradeAssignment> getAssignments() {
        return assignments;
    }

    public JsonObject asJson() {
        return new JsonObject().put("assignments", GradeAssignment.asJsonArray(assignments));
    }
}
