package com.nicholasnassar.sfsapi.data.links

import io.vertx.core.json.JsonObject

abstract class Link(val type: LinkType) {
    abstract fun asJson(): JsonObject
}
