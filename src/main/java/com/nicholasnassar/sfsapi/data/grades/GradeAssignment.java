package com.nicholasnassar.sfsapi.data.grades;

import com.nicholasnassar.sfsapi.data.links.Link;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

public class GradeAssignment {
    private final Link link;

    private final String dateDue, category, assignment, possiblePoints, score, percentage, letterGrade, comments;

    public GradeAssignment(Link link, String dateDue, String category, String assignment, String possiblePoints, String score,
                           String percentage, String letterGrade, String comments) {
        this.link = link;

        this.dateDue = dateDue;

        this.category = category;

        this.assignment = assignment;

        this.possiblePoints = possiblePoints;

        this.score = score;

        this.percentage = percentage;

        this.letterGrade = letterGrade;

        this.comments = comments;
    }

    public JsonObject asJson() {
        return new JsonObject().put("link", link == null ? null : link.asJson()).put("date_due", dateDue)
                .put("category", category).put("assignment", assignment).put("possible_points", possiblePoints)
                .put("score", score).put("percentage", percentage).put("letter_grade", letterGrade)
                .put("comments", comments);
    }

    public static JsonArray asJsonArray(List<GradeAssignment> assignments) {
        JsonArray array = new JsonArray();

        for (GradeAssignment assignment : assignments) {
            array.add(assignment.asJson());
        }

        return array;
    }
}
