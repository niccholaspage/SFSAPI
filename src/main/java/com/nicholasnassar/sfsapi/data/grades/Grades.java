package com.nicholasnassar.sfsapi.data.grades;

import com.nicholasnassar.sfsapi.data.assignments.MissingAssignment;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

public class Grades {
    private final List<Grade> grades;

    private final List<MissingAssignment> missingWork;

    public Grades(List<Grade> grades, List<MissingAssignment> missingWork) {
        this.grades = grades;

        this.missingWork = missingWork;
    }

    public List<Grade> getGrades() {
        return grades;
    }

    public JsonObject asJson() {
        JsonObject obj = new JsonObject();

        JsonArray gradesArray = new JsonArray();

        for (Grade grade : grades) {
            gradesArray.add(grade.asJson());
        }

        JsonArray missingJson = new JsonArray();

        for (MissingAssignment missingAssignment : missingWork) {
            missingJson.add(missingAssignment.asJson());
        }

        return obj.put("grades", gradesArray).put("missing_work", missingJson);
    }
}
