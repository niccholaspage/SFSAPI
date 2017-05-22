package com.nicholasnassar.sfsapi.data.assignments

import com.nicholasnassar.sfsapi.data.links.AssignmentLink
import com.nicholasnassar.sfsapi.data.links.Link
import io.vertx.core.json.JsonObject

class AssignmentTaskList(id: String, val clazz: String, activity: String, val dateDue: String, val resources: Int) {
    val link: Link

    val activity: String

    init {
        link = AssignmentLink(id)

        this.activity = activity.replace("Assignment: ", "")
    }

    fun asJson(): JsonObject {
        return JsonObject().put("link", link.asJson()).put("class", clazz).put("activity", activity)
                .put("date_due", dateDue).put("resources", resources).put("resource", "")
    }
}
