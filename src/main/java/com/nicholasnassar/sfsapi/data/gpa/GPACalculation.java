package com.nicholasnassar.sfsapi.data.gpa;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

public class GPACalculation {
    private final List<GPAClass> classes;

    public GPACalculation(List<GPAClass> classes) {
        this.classes = classes;
    }

    public JsonObject asJson() {
        JsonArray array = new JsonArray();

        for (GPAClass clazz : classes) {
            array.add(clazz.asJson());
        }

        return new JsonObject().put("classes", array);
    }
}
