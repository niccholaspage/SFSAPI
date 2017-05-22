package com.nicholasnassar.sfsapi.data.assignments

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

class AssignmentDay(val date: String, val assignments: List<AssignmentNameAndLink>) {
    fun asJson(): JsonObject {
        val obj = JsonObject().put("date", date)

        val assignmentsArray = JsonArray()

        for (assignment in assignments) {
            assignmentsArray.add(assignment.asJson())
        }

        obj.put("assignments", assignmentsArray)

        return obj
    }
}