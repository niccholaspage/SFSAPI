package com.nicholasnassar.sfsapi.data.grades

import com.nicholasnassar.sfsapi.data.links.Link
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

class GradeAssignment(val link: Link?, val dateDue: String, val category: String, val assignment: String,
                      val possiblePoints: String, val score: String, val percentage: String, val letterGrade: String,
                      val comments: String) {
    fun asJson(): JsonObject {
        return JsonObject().put("link", link?.asJson()).put("date_due", dateDue)
                .put("category", category).put("assignment", assignment).put("possible_points", possiblePoints)
                .put("score", score).put("percentage", percentage).put("letter_grade", letterGrade)
                .put("comments", comments)
    }

    companion object {
        fun asJsonArray(assignments: List<GradeAssignment>): JsonArray {
            val array = JsonArray()

            for (assignment in assignments) {
                array.add(assignment.asJson())
            }

            return array
        }
    }
}
