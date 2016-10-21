package com.nicholasnassar.sfsapi.data.gpa;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

public class GPACalculation {
    private final List<GPAClass> classes;

    private final String[] selectableTerms;

    public GPACalculation(List<GPAClass> classes, String[] selectableTerms) {
        this.classes = classes;

        this.selectableTerms = selectableTerms;
    }

    public JsonObject asJson() {
        JsonArray array = new JsonArray();

        for (GPAClass clazz : classes) {
            array.add(clazz.asJson());
        }

        JsonArray selectableTermsArray = new JsonArray();

        for (String previousTerm : selectableTerms) {
            selectableTermsArray.add(previousTerm);
        }

        return new JsonObject().put("classes", array).put("selectable_terms", selectableTermsArray);
    }
}
