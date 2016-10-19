package com.nicholasnassar.sfsapi.data.links;

import io.vertx.core.json.JsonObject;

public abstract class Link {
    protected final LinkType type;

    public Link(LinkType type) {
        this.type = type;
    }

    public abstract JsonObject asJson();
}
