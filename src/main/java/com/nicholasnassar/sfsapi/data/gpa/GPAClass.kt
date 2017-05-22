package com.nicholasnassar.sfsapi.data.gpa

import io.vertx.core.json.JsonObject

class GPAClass(val name: String, val letterGrade: String, val points: String) {
    fun asJson(): JsonObject {
        return JsonObject().put("name", name).put("letter_grade", letterGrade).put("points", points)
    }
}
