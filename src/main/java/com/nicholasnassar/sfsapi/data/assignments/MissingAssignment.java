package com.nicholasnassar.sfsapi.data.assignments;

import com.nicholasnassar.sfsapi.data.links.Link;
import io.vertx.core.json.JsonObject;

public class MissingAssignment {
    private final Link link;

    private final String dateDue, clazz, assignment, possiblePoints, score, comments;

    public MissingAssignment(Link link, String dateDue, String clazz, String assignment,
                             String possiblePoints, String score, String comments) {
        this.link = link;

        this.dateDue = dateDue;

        this.clazz = clazz;

        this.assignment = assignment;

        this.possiblePoints = possiblePoints;

        this.score = score;

        this.comments = comments;
    }

    public JsonObject asJson() {
        return new JsonObject().put("link", link.asJson()).put("date_due", dateDue).put("class", clazz)
                .put("assignment", assignment).put("possible_points", possiblePoints).put("score", score)
                .put("comments", comments);
    }
}
