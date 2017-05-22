package com.nicholasnassar.sfsapi.data.assignments

import io.vertx.core.json.JsonArray

class AssignmentsWeek(val days: List<AssignmentDay>) {
    fun asJsonArray(): JsonArray {
        val array = JsonArray()

        for (day in days) {
            array.add(day.asJson())
        }

        return array
    }
}
