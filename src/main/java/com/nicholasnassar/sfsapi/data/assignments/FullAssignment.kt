package com.nicholasnassar.sfsapi.data.assignments

import com.nicholasnassar.sfsapi.data.links.Resource
import io.vertx.core.json.JsonObject

class FullAssignment(val activity: String, val clazz: String?, val instructor: String?, val assigned: String?,
                     val due: String?, val possiblePoints: String?, val category: String?, val notes: String?,
                     val resources: List<Resource>) {
    fun asJson(): JsonObject {
        return JsonObject().put("activity", activity).put("class", clazz).put("instructor", instructor)
                .put("assigned", assigned).put("due", due).put("possible_points", possiblePoints)
                .put("category", category).put("notes", notes).put("resources", Resource.asJsonArray(resources))
    }
}
