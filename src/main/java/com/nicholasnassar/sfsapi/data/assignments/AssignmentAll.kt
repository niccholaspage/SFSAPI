package com.nicholasnassar.sfsapi.data.assignments

import com.nicholasnassar.sfsapi.data.links.AssignmentLink
import com.nicholasnassar.sfsapi.data.links.Link
import io.vertx.core.json.JsonObject

class AssignmentAll(id: String, val due: String, val clazz: String, val assignment: String, val notes: String,
                    val resources: String) {
    val link: Link

    init {
        link = AssignmentLink(id)
    }

    fun asJson(): JsonObject {
        return JsonObject().put("link", link.asJson()).put("due", due).put("class", clazz).put("assignment",
                assignment).put("notes", notes).put("resources", resources)
    }
}