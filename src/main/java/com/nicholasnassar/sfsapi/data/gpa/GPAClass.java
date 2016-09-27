package com.nicholasnassar.sfsapi.data.gpa;

import io.vertx.core.json.JsonObject;

public class GPAClass {
    private final String name, letterGrade;

    private final double points;

    public GPAClass(String name, String letterGrade, double points) {
        this.name = name;

        this.letterGrade = letterGrade;

        this.points = points;
    }

    public JsonObject asJson() {
        return new JsonObject().put("name", name).put("letter_grade", letterGrade).put("points", points);
    }
}
