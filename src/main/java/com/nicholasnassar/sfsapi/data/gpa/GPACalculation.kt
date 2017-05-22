package com.nicholasnassar.sfsapi.data.gpa

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

class GPACalculation(val classes: List<GPAClass>, val selectableTerms: Array<String>) {
    fun asJson(): JsonObject {
        val array = JsonArray()

        for (clazz in classes) {
            array.add(clazz.asJson())
        }

        val selectableTermsArray = JsonArray()

        for (previousTerm in selectableTerms) {
            selectableTermsArray.add(previousTerm)
        }

        return JsonObject().put("classes", array).put("selectable_terms", selectableTermsArray)
    }
}