package com.nicholasnassar.sfsapi.data.grades

import com.nicholasnassar.sfsapi.data.links.GradeLink
import io.vertx.core.json.JsonObject

class Grade(val link: GradeLink, val term: String, val clazz: String, val instructor: String?, val score: String) {
    fun asJson(): JsonObject {
        return JsonObject().put("link", link.asJson()).put("term", term).put("class", clazz)
                .put("instructor", instructor).put("score", score)
    }
}