package com.nicholasnassar.sfsapi.web

import com.nicholasnassar.sfsapi.SFS
import io.vertx.core.Vertx
import io.vertx.core.http.HttpHeaders
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import java.util.*

class WebHandler {
    val vertx: Vertx = Vertx.vertx()

    val api: SFS = SFS()

    init {
        val router = Router.router(vertx)

        router.route().handler(BodyHandler.create())

        router.get("/").handler { ctx -> ctx.response().end("Welcome to the SFS API!") }

        router.post("/login").handler { ctx ->
            val json = ctx.bodyAsJson

            if (!json.containsKey("email") && !json.containsKey("password")) {
                ctx.fail(400)

                return@handler
            }

            setupHeaders(ctx)

            api.login(json.getString("email"), json.getString("password")).thenAccept { result ->
                val response = JsonObject()

                if (result.error != null) {
                    response.put("error", result.error)
                } else {
                    response.put("cookie", result.result)
                }

                ctx.response().end(response.encode())
            }
        }

        router.post("/newsfeed").handler { ctx ->
            if (!processContext(ctx)) {
                return@handler
            }

            val json = ctx.bodyAsJson

            api.fetchNewsFeed(json.getString("cookie")).thenAccept { newsFeedItems ->
                val response = JsonObject()

                val newsFeed = JsonArray()

                for (item in newsFeedItems) {
                    newsFeed.add(item.asJson())
                }

                response.put("news_feed", newsFeed)

                ctx.response().end(response.encode())
            }
        }

        router.post("/grades").handler { ctx ->
            if (!processContext(ctx, "show_all_classes")) {
                return@handler
            }

            val json = ctx.bodyAsJson

            api.fetchGrades(json.getString("cookie"), json.getBoolean("show_all_classes")!!).thenAccept { grades -> ctx.response().end(grades.asJson().encode()) }
        }

        router.post("/gpa").handler { ctx ->
            if (!processContext(ctx)) {
                return@handler
            }

            val json = ctx.bodyAsJson

            var term: String? = null

            if (json.containsKey("term")) {
                term = json.getString("term")
            }

            api.fetchGPA(json.getString("cookie"), term).thenAccept { gpaCalculation -> ctx.response().end(gpaCalculation.asJson().encode()) }
        }

        router.post("/assignments").handler { ctx ->
            if (!processContext(ctx, "mode")) {
                return@handler
            }

            val json = ctx.bodyAsJson

            val mode = json.getString("mode")

            if (mode == "task_list") {
                api.fetchAssignmentsInTaskList(json.getString("cookie")).thenAccept { assignments ->
                    val response = JsonObject()

                    val assignmentsJson = JsonArray()

                    for (assignment in assignments) {
                        assignmentsJson.add(assignment.asJson())
                    }

                    response.put("assignments", assignmentsJson)

                    ctx.response().end(response.encode())
                }
            } else if (mode == "week") {
                api.fetchAssignmentsInWeek(json.getString("cookie")).thenAccept { assignmentsWeek -> ctx.response().end(assignmentsWeek.asJsonArray().encode()) }
            } else if (mode == "month") {
                api.fetchAssignmentsInMonth(json.getString("cookie")).thenAccept { assignmentsMonth -> ctx.response().end(JsonObject().put("month", assignmentsMonth.asJsonArray()).encode()) }
            } else if (mode == "all") {
                val clazz: String?

                if (json.containsKey("class")) {
                    clazz = json.getString("class")
                } else {
                    clazz = null
                }

                api.fetchAllAssignments(json.getString("cookie"), clazz).thenAccept { allAssignments ->
                    val response = JsonObject()

                    val assignmentsJson = JsonArray()

                    for (assignment in allAssignments) {
                        assignmentsJson.add(assignment.asJson())
                    }

                    response.put("assignments", assignmentsJson)

                    ctx.response().end(response.encode())
                }
            }
        }

        router.post("/fullassignment").handler { ctx ->
            val json = ctx.bodyAsJson

            if (!json.containsKey("cookie") || !json.containsKey("id")) {
                ctx.fail(400)

                return@handler
            }

            processContext(ctx)

            api.fetchFullAssignment(json.getString("cookie"), json.getString("id")).thenAccept { fullAssignment -> ctx.response().end(fullAssignment.asJson().encode()) }
        }

        router.post("/gradedetails").handler { ctx ->
            if (!processContext(ctx, "class_id", "cgp_id")) {
                return@handler
            }

            val json = ctx.bodyAsJson

            api.fetchGradeDetails(json.getString("cookie"), json.getString("class_id"), json.getString("cgp_id")).thenAccept { fullGrade -> ctx.response().end(fullGrade.asJson().encode()) }
        }

        router.post("/announcement").handler { ctx ->
            if (!processContext(ctx, "id")) {
                return@handler
            }

            val json = ctx.bodyAsJson

            api.fetchAnnouncement(json.getString("cookie"), json.getString("id")).thenAccept { announcement -> ctx.response().end(announcement.asJson().encode()) }
        }

        router.post("/reportcard").handler { ctx ->
            if (!processContext(ctx)) {
                return@handler
            }

            ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/pdf")

            val json = ctx.bodyAsJson

            api.fetchReportCard(json.getString("cookie")).thenAccept { html -> ctx.response().end(html) }
        }

        vertx.createHttpServer().requestHandler(router::accept).listen(5000)

        println("Website started!")
    }

    private fun processContext(ctx: RoutingContext, vararg parameters: String): Boolean {
        val json = ctx.bodyAsJson

        if (!json.containsKey("cookie")) {
            ctx.fail(400)

            return false
        }

        for (parameter in parameters) {
            if (!json.containsKey(parameter)) {
                ctx.fail(400)

                return false
            }
        }

        setupHeaders(ctx)

        return true
    }

    private fun setupHeaders(ctx: RoutingContext) {
        ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
    }

    fun stop() {
        println("Stopping...")

        vertx.close()

        api.close()
    }
}

fun main(args: Array<String>) {
    val handler = WebHandler()

    val scanner = Scanner(System.`in`)

    while (scanner.hasNextLine()) {
        if (scanner.nextLine() == "stop") {
            break
        }
    }

    handler.stop()
}