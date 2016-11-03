package com.nicholasnassar.sfsapi.web;

import com.nicholasnassar.sfsapi.SFS;
import com.nicholasnassar.sfsapi.data.NewsFeedItem;
import com.nicholasnassar.sfsapi.data.assignments.AssignmentAll;
import com.nicholasnassar.sfsapi.data.assignments.AssignmentTaskList;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.Scanner;

public class WebHandler {
    private final Vertx vertx;

    private final SFS api;

    private WebHandler() {
        vertx = Vertx.vertx();

        api = new SFS();

        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());

        router.get("/").handler(ctx -> ctx.response().end("Welcome to the SFS API!"));

        router.post("/login").handler(ctx -> {
            JsonObject json = ctx.getBodyAsJson();

            if (!json.containsKey("email") && !json.containsKey("password")) {
                ctx.fail(400);

                return;
            }

            setupHeaders(ctx);

            api.login(json.getString("email"), json.getString("password")).thenAccept(result -> {
                JsonObject response = new JsonObject();

                if (result.getError() != null) {
                    response.put("error", result.getError());
                } else {
                    response.put("cookie", result.getResult());
                }

                ctx.response().end(response.encode());
            });
        });

        router.post("/newsfeed").handler(ctx -> {
            if (!processContext(ctx)) {
                return;
            }

            JsonObject json = ctx.getBodyAsJson();

            api.fetchNewsFeed(json.getString("cookie")).thenAccept(newsFeedItems -> {
                JsonObject response = new JsonObject();

                JsonArray newsFeed = new JsonArray();

                for (NewsFeedItem item : newsFeedItems) {
                    newsFeed.add(item.asJson());
                }

                response.put("news_feed", newsFeed);

                ctx.response().end(response.encode());
            });
        });

        router.post("/grades").handler(ctx -> {
            if (!processContext(ctx, "show_all_classes")) {
                return;
            }

            JsonObject json = ctx.getBodyAsJson();

            api.fetchGrades(json.getString("cookie"), json.getBoolean("show_all_classes")).thenAccept(grades -> {
                ctx.response().end(grades.asJson().encode());
            });
        });

        router.post("/gpa").handler(ctx -> {
            if (!processContext(ctx)) {
                return;
            }

            JsonObject json = ctx.getBodyAsJson();

            String term = null;

            if (json.containsKey("term")) {
                term = json.getString("term");
            }

            api.fetchGPA(json.getString("cookie"), term).thenAccept(gpaCalculation -> {
                ctx.response().end(gpaCalculation.asJson().encode());
            });
        });

        router.post("/assignments").handler(ctx -> {
            if (!processContext(ctx, "mode")) {
                return;
            }

            JsonObject json = ctx.getBodyAsJson();

            String mode = json.getString("mode");

            if (mode.equals("task_list")) {
                api.fetchAssignmentsInTaskList(json.getString("cookie")).thenAccept(assignments -> {
                    JsonObject response = new JsonObject();

                    JsonArray assignmentsJson = new JsonArray();

                    for (AssignmentTaskList assignment : assignments) {
                        assignmentsJson.add(assignment.asJson());
                    }

                    response.put("assignments", assignmentsJson);

                    ctx.response().end(response.encode());
                });
            } else if (mode.equals("week")) {
                api.fetchAssignmentsInWeek(json.getString("cookie")).thenAccept(assignmentsWeek -> {
                    ctx.response().end(assignmentsWeek.asJsonArray().encode());
                });
            } else if (mode.equals("all")) {
                String clazz;

                if (json.containsKey("class")) {
                    clazz = json.getString("class");
                } else {
                    clazz = null;
                }

                api.fetchAllAssignments(json.getString("cookie"), clazz).thenAccept(allAssignments -> {
                    JsonObject response = new JsonObject();

                    JsonArray assignmentsJson = new JsonArray();

                    for (AssignmentAll assignment : allAssignments) {
                        assignmentsJson.add(assignment.asJson());
                    }

                    response.put("assignments", assignmentsJson);

                    ctx.response().end(response.encode());
                });
            }
        });

        router.post("/fullassignment").handler(ctx -> {
            JsonObject json = ctx.getBodyAsJson();

            if (!json.containsKey("cookie") || !json.containsKey("id")) {
                ctx.fail(400);

                return;
            }

            processContext(ctx);

            api.fetchFullAssignment(json.getString("cookie"), json.getString("id")).thenAccept(fullAssignment -> {
                ctx.response().end(fullAssignment.asJson().encode());
            });
        });

        router.post("/gradedetails").handler(ctx -> {
            if (!processContext(ctx, "class_id", "cgp_id")) {
                return;
            }

            JsonObject json = ctx.getBodyAsJson();

            api.fetchGradeDetails(json.getString("cookie"), json.getString("class_id"), json.getString("cgp_id")).thenAccept(fullGrade -> {
                ctx.response().end(fullGrade.asJson().encode());
            });
        });

        router.post("/announcement").handler(ctx -> {
            if (!processContext(ctx, "id")) {
                return;
            }

            JsonObject json = ctx.getBodyAsJson();

            api.fetchAnnouncement(json.getString("cookie"), json.getString("id")).thenAccept(announcement -> {
                ctx.response().end(announcement.asJson().encode());
            });
        });

        router.post("/reportcard").handler(ctx -> {
            if (!processContext(ctx)) {
                return;
            }

            ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/pdf");

            JsonObject json = ctx.getBodyAsJson();

            api.fetchReportCard(json.getString("cookie")).thenAccept(html -> {
                ctx.response().end(html);
            });
        });

        vertx.createHttpServer().requestHandler(router::accept).listen(5000);

        System.out.println("Website started!");
    }

    private boolean processContext(RoutingContext ctx, String... parameters) {
        JsonObject json = ctx.getBodyAsJson();

        if (!json.containsKey("cookie")) {
            ctx.fail(400);

            return false;
        }

        for (String parameter : parameters) {
            if (!json.containsKey(parameter)) {
                ctx.fail(400);

                return false;
            }
        }

        setupHeaders(ctx);

        return true;
    }

    private void setupHeaders(RoutingContext ctx) {
        ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8");
    }

    private void stop() {
        System.out.println("Stopping...");

        vertx.close();

        api.close();
    }

    public static void main(String[] args) {
        WebHandler handler = new WebHandler();

        Scanner scanner = new Scanner(System.in);

        String line;

        while ((line = scanner.nextLine()) != null) {
            if (line.equals("stop")) {
                break;
            }
        }

        handler.stop();
    }
}
