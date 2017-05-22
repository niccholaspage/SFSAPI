package com.nicholasnassar.sfsapi.data.grades

import io.vertx.core.json.JsonObject

class FullGrade(val assignments: List<GradeAssignment>) {
    fun asJson(): JsonObject {
        return JsonObject().put("assignments", GradeAssignment.asJsonArray(assignments))
    }
}
