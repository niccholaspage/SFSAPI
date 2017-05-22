package com.nicholasnassar.sfsapi.data.grades

import com.nicholasnassar.sfsapi.data.assignments.MissingAssignment
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

class Grades(val grades: List<Grade>, val missingWork: List<MissingAssignment>) {
    fun asJson(): JsonObject {
        val obj = JsonObject()

        val gradesArray = JsonArray()

        for (grade in grades) {
            gradesArray.add(grade.asJson())
        }

        val missingJson = JsonArray()

        for (missingAssignment in missingWork) {
            missingJson.add(missingAssignment.asJson())
        }

        return obj.put("grades", gradesArray).put("missing_work", missingJson)
    }
}
