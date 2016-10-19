package com.nicholasnassar.sfsapi.data.links;

import io.vertx.core.json.JsonObject;

public class GradeLink extends Link {
    private final String classId, cgpId;

    public GradeLink(String classId, String cgpId) {
        super(LinkType.GRADE);

        this.classId = classId;

        this.cgpId = cgpId;
    }

    public String getClassId() {
        return classId;
    }

    public String getCgpId() {
        return cgpId;
    }

    public JsonObject asJson() {
        return new JsonObject().put("type", type.getName()).put("class_id", classId).put("cgp_id", cgpId);
    }
}
