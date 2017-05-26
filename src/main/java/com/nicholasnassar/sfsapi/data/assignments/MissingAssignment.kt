package com.nicholasnassar.sfsapi.data.assignments

import com.nicholasnassar.sfsapi.data.links.Link
import io.vertx.core.json.JsonObject

class MissingAssignment(val link: Link, val dateDue: String?, val clazz: String?, val assignment: String?,
                        val possiblePoints: String?, val score: String?, val comments: String?) {

    fun asJson(): JsonObject {
        return JsonObject().put("link", link.asJson()).put("date_due", dateDue).put("class", clazz)
                .put("assignment", assignment).put("possible_points", possiblePoints).put("score", score)
                .put("comments", comments)
    }
}
