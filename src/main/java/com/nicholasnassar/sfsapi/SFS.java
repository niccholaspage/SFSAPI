package com.nicholasnassar.sfsapi;

import com.nicholasnassar.sfsapi.data.Announcement;
import com.nicholasnassar.sfsapi.data.LoginResult;
import com.nicholasnassar.sfsapi.data.NewsFeedItem;
import com.nicholasnassar.sfsapi.data.assignments.*;
import com.nicholasnassar.sfsapi.data.gpa.GPACalculation;
import com.nicholasnassar.sfsapi.data.gpa.GPAClass;
import com.nicholasnassar.sfsapi.data.grades.*;
import com.nicholasnassar.sfsapi.data.links.LetterGrade;
import com.nicholasnassar.sfsapi.data.links.Link;
import com.nicholasnassar.sfsapi.data.links.LinkType;
import com.nicholasnassar.sfsapi.data.links.Resource;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SFS {
    private static final String COOKIE_URL = "www.teacherease.com";

    private static final String BASE_URL = "https://www.teacherease.com/";

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.101 Safari/537.36";

    private static final String CURRENT_TERM = "2016-17 Q1";

    private final DecimalFormat gpaTruncate;

    private final CloseableHttpAsyncClient client;

    public SFS() {
        client = HttpAsyncClientBuilder.create().setUserAgent(USER_AGENT)
                .setRedirectStrategy(new LaxRedirectStrategy()).build();

        client.start();

        gpaTruncate = new DecimalFormat("#.###");

        gpaTruncate.setRoundingMode(RoundingMode.DOWN);
    }

    private HttpContext generateCookieContext(String value) {
        HttpContext localContext = new HttpClientContext();

        BasicClientCookie cookie = new BasicClientCookie("ASP.NET_SessionId", value);

        cookie.setVersion(0);

        cookie.setDomain(COOKIE_URL);

        cookie.setPath("/");

        cookie.setSecure(false);

        cookie.setExpiryDate(null);

        BasicCookieStore tempStore = new BasicCookieStore();

        tempStore.addCookie(cookie);

        localContext.setAttribute(HttpClientContext.COOKIE_STORE, tempStore);

        return localContext;

    }

    public CompletableFuture<LoginResult> login(String email, String password) {
        HttpPost post = new HttpPost(BASE_URL + "common/LoginResponse.aspx");

        List<NameValuePair> formParameters = new ArrayList<>();

        formParameters.add(new BasicNameValuePair("email", email));
        formParameters.add(new BasicNameValuePair("password", password));
        formParameters.add(new BasicNameValuePair("bRememberEmail", "remember"));

        try {
            post.setEntity(new UrlEncodedFormEntity(formParameters));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        ApacheCompletableFuture<HttpResponse> future = new ApacheCompletableFuture<>();

        HttpClientContext context = new HttpClientContext();

        BasicCookieStore store = new BasicCookieStore();

        context.setCookieStore(store);

        client.execute(post, context, future);

        return future.handle((httpResponse, throwable) -> {
            if (throwable != null) {
                return new LoginResult(null, "Couldn't log you in with those details.");
            } else {
                return this.fetchDocument(httpResponse);
            }
        }).thenApply(result -> {
            if (result instanceof Document) {
                Document document = (Document) result;

                String title = document.title();

                if (title.equals("TeacherEase: Student Main")) {
                    return new LoginResult(store.getCookies().get(0).getValue(), null);
                } else {
                    return new LoginResult(null, "Couldn't log you in with those details.");
                }
            } else {
                return (LoginResult) result;
            }
        });
    }

    private Document fetchDocument(HttpResponse response) {
        try {
            return Jsoup.parse(EntityUtils.toString(response.getEntity()));
        } catch (IOException e) {
            e.printStackTrace();

            return null;
        }
    }

    public CompletableFuture<List<NewsFeedItem>> fetchNewsFeed(String cookie) {
        HttpGet get = new HttpGet(BASE_URL + "parents/main_classic.aspx?");

        ApacheCompletableFuture<HttpResponse> future = new ApacheCompletableFuture<>();

        client.execute(get, generateCookieContext(cookie), future);

        return future.thenApply(this::fetchDocument).thenApply(document -> {
            List<NewsFeedItem> items = new ArrayList<>();

            Element element = document.select("div#NewsFeedContentContainer").first();

            for (Element div : element.getElementsByTag("div")) {
                if (div.id().startsWith("NewsFeedItem_")) {
                    Element item = div.select("div").first();

                    Element headerLink = item.select("div.newsfeedheader > div.newsfeedheadertext > a").first();

                    Elements contentLinks = item.select("div.newsfeedcontent > a");

                    Link link = null;

                    if (contentLinks.isEmpty()) {
                        String linkHref = handleLinkHref(headerLink);

                        link = LinkType.generateLink(linkHref);
                    } else {
                        String linkHref = handleLinkHref(contentLinks.first());

                        link = LinkType.generateLink(linkHref);
                    }

                    String className = headerLink.text();

                    String time = item.select("div.newsfeedheader > div[style='float:right;'] > div").first().text();

                    String details = item.select("div.newsfeedcontent").first().text();

                    items.add(new NewsFeedItem(className, time, details, link));
                }
            }

            return items;
        });
    }

    private String handleLinkHref(Element element) {
        String iFrameStuff = "javascript:jte.dom.iframe.Add('";

        String teacherEaseDOMStuff = "avascript:teacherease.dom.iframe.Add('";

        String linkHref;

        if (element.hasAttr("onclick")) {
            linkHref = element.attr("onclick");
        } else {
            linkHref = element.attr("href");
        }

        if (linkHref != null && linkHref.contains(iFrameStuff)) {
            linkHref = linkHref.replace(iFrameStuff, "").replace("');", "");
        }

        if (linkHref != null && linkHref.contains(teacherEaseDOMStuff)) {
            linkHref = linkHref.replace(teacherEaseDOMStuff, "").replace("');", "");
        }

        return linkHref;
    }

    public CompletableFuture<FullGrade> fetchGradeDetails(String cookie, String id) {
        HttpGet get = new HttpGet(BASE_URL + "parents/StudentProgressView.aspx?ClassID=" + id);

        ApacheCompletableFuture<HttpResponse> future = new ApacheCompletableFuture<>();

        client.execute(get, generateCookieContext(cookie), future);

        return future.thenApply(this::fetchDocument).thenApply(document -> {
            Elements noScores = document.select("td[colspan=7]");

            if (noScores.text().equals("Sorry, no assignment/scores for this class.")) {
                return new FullGrade(new ArrayList<>());
            }

            Element table = document.select("table.ViewAllTable > tbody").first();

            List<GradeAssignment> gradeAssignments = new ArrayList<>();

            for (int i = 1; i < table.children().size(); i++) {
                Element row = table.child(i);

                Element assignmentElement;

                String dateDue, category, assignment, possiblePoints, score, percentage, letterGrade, comments;

                if (row.children().size() == 8) {
                    dateDue = trim(row.child(0).text());
                    category = trim(row.child(1).text());
                    assignmentElement = row.child(2);
                    assignment = trim(assignmentElement.text());
                    possiblePoints = trim(row.child(3).text());
                    score = trim(row.child(4).text());
                    percentage = trim(row.child(5).text());
                    letterGrade = trim(row.child(6).text());
                    comments = trim(row.child(7).text());
                } else {
                    dateDue = trim(row.child(0).text());
                    category = null;
                    assignmentElement = row.child(1);
                    assignment = trim(assignmentElement.text());
                    possiblePoints = trim(row.child(2).text());
                    score = trim(row.child(3).text());
                    percentage = trim(row.child(4).text());
                    letterGrade = trim(row.child(5).text());
                    comments = trim(row.child(6).text());
                }

                String assignmentId = null;

                try {
                    assignmentId = assignmentElement.select("a").attr("href")
                            .replace("../parents/AssignmentView.aspx?TestNameID=", "");

                    assignmentId = assignmentId.substring(0, assignmentId.indexOf("&"));
                } catch (Exception e) {

                }

                Link link;

                if (assignmentId.isEmpty()) {
                    link = null;
                } else {
                    link = new Link(LinkType.ASSIGNMENT, assignmentId);
                }

                GradeAssignment gradeAssignment = new GradeAssignment(link, dateDue, category, assignment,
                        possiblePoints, score, percentage, letterGrade, comments);

                gradeAssignments.add(gradeAssignment);
            }

            return new FullGrade(gradeAssignments);
        });
    }

    private String trim(String text) {
        text = text.replace("\u00A0", "");

        return text.isEmpty() ? null : text;
    }

    public CompletableFuture<Grades> fetchGrades(String cookie) {
        HttpGet get = new HttpGet(BASE_URL + "parents/StudentProgressSummary_classic.aspx?");

        ApacheCompletableFuture<HttpResponse> future = new ApacheCompletableFuture<>();

        client.execute(get, generateCookieContext(cookie), future);

        return future.thenApply(this::fetchDocument).thenApply(document -> {
            List<Grade> grades = new ArrayList<>();

            Elements tables = document.select("table.ViewAllTable > tbody");

            List<Element> tableRows = tables.get(0).children();

            for (int i = 1; i < tableRows.size(); i++) {
                Element element = tableRows.get(i);

                Element currentScore = element.child(3);

                String id = currentScore.select("a").attr("href");

                id = id.substring(id.indexOf("=") + 1);

                id = id.substring(0, id.indexOf("&"));

                grades.add(new Grade(id, element.child(0).text(),
                        element.child(1).text(), element.child(2).text(), currentScore.text()));
            }

            List<MissingAssignment> missingWork = new ArrayList<>();

            if (tables.size() > 1) {
                Element missingWorkTable = tables.get(1);

                for (Element tableRow : missingWorkTable.children()) {
                    if (tableRow.child(0).text().equals("This student has no missing work.")) {
                        break;
                    }

                    String dateDue = trim(tableRow.child(0).text());
                    String className = trim(tableRow.child(1).text());
                    String assignmentName = trim(tableRow.child(2).text());
                    Link link = LinkType.generateLink(tableRow.child(2).select("a").attr("href"));
                    String possiblePoints = trim(tableRow.child(3).text());
                    String score = trim(tableRow.child(4).text());
                    String comments = trim(tableRow.child(5).text());
                    missingWork.add(new MissingAssignment(link, dateDue, className, assignmentName,
                            possiblePoints, score, comments));
                }
            }

            /* Test missing work assignments
            missingWork.add(new MissingAssignment(new Link(LinkType.ASSIGNMENT, "123"), "9/30/2016", "Test Class 1",
                    "Test Assignment", "5", "5", "Some comments"));
            missingWork.add(new MissingAssignment(new Link(LinkType.ASSIGNMENT, "1243"), "9/31/2016", "Test Class 2",
                    "Test Assignment 2", "5", "5", "Some comments"));*/

            return new Grades(grades, missingWork);
        });
    }

    public CompletableFuture<GPACalculation> fetchGPA(String cookie) {
        return fetchGrades(cookie).thenApply(grades -> {
            List<Grade> allGrades = grades.getGrades();

            int classesCount = 0;

            double gpa = 0;

            double maxGPA = 0;

            List<GPAClass> classes = new ArrayList<>();

            for (Grade grade : allGrades) {
                String score = grade.getScore();

                if (score.equals("Details") || !grade.getTerm().equals(CURRENT_TERM)) {
                    continue;
                }

                classesCount++;

                String letterGradeFromScore = score.substring(score.indexOf(" = ") + 3);

                LetterGrade letterGrade = LetterGrade.getLetterGrade(letterGradeFromScore);

                GradeScale scale = GradeScale.getScale(grade.getClazz());

                double classScore = scale.getScore(letterGrade);

                gpa += classScore;

                maxGPA += scale.getScore(LetterGrade.A_PLUS);

                classes.add(new GPAClass(grade.getClazz(), letterGrade.getName(), classScore + ""));
            }

            gpa /= classesCount;

            maxGPA /= classesCount;

            gpa = Double.parseDouble(gpaTruncate.format(gpa));

            maxGPA = Double.parseDouble(gpaTruncate.format(maxGPA));

            classes.add(new GPAClass("Total", "Your GPA: " + gpa, "Max: " + maxGPA));

            return new GPACalculation(classes);
        });
    }

    public CompletableFuture<List<Assignment>> fetchAssignmentsInTaskList(String cookie) {
        HttpGet get = new HttpGet(BASE_URL + "parents/AssignmentViewAll_Calendar.aspx?showrecentassignments=true&datemode=3");

        ApacheCompletableFuture<HttpResponse> future = new ApacheCompletableFuture<>();

        client.execute(get, generateCookieContext(cookie), future);

        return future.thenApply(this::fetchDocument).thenApply(document -> {
            List<Assignment> assignments = new ArrayList<>();

            Element tableBody = document.select("table#tblMain > tbody").first();

            String previousName = null;

            int classSpan = 0;

            try {
                for (Element element : tableBody.children()) {
                    String name;

                    Element activity, dateDue, resource;

                    if (classSpan != 0) {
                        name = previousName;

                        activity = element.child(0);

                        dateDue = element.child(1);

                        resource = element.child(2);

                        classSpan--;
                    } else {
                        Element classElement = element.child(0);

                        String rowSpan = classElement.attr("rowspan");

                        if (rowSpan != null && !rowSpan.isEmpty()) {
                            classSpan = Integer.parseInt(rowSpan) - 1;

                            previousName = classElement.text();
                        }

                        name = classElement.text();

                        activity = element.child(1);

                        dateDue = element.child(2);

                        resource = element.child(3);
                    }

                    if (!resource.text().isEmpty()) {
                        resource = resource.getElementsByClass("resourcedescriptioncell").get(0);
                    }

                    String id = activity.select("a").attr("href").replace("../parents/AssignmentView.aspx?TestNameID=", "");

                    assignments.add(new Assignment(id, name, activity.text(), "Due " + dateDue.text(),
                            resource.text().isEmpty() ? "" : "Resource: " + resource.text()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return assignments;
        });
    }

    public CompletableFuture<AssignmentsWeek> fetchAssignmentsInWeek(String cookie) {
        HttpGet get = new HttpGet(BASE_URL + "parents/AssignmentViewAll_Calendar.aspx?&showrecentassignments=true&datemode=2");

        ApacheCompletableFuture<HttpResponse> future = new ApacheCompletableFuture<>();

        client.execute(get, generateCookieContext(cookie), future);

        return future.thenApply(this::fetchDocument).thenApply(document -> {
            Element tableBody = document.select("table#tblMain > tbody").first();

            List<AssignmentDay> days = new ArrayList<>();

            for (Element tableRow : tableBody.children()) {
                String date = tableRow.child(0).text();

                List<AssignmentNameAndLink> assignments = new ArrayList<>();

                for (Element assignment : tableRow.select("div.assignment")) {
                    String assignmentId = assignment.id().substring(1);

                    String assignmentName = assignment.text();

                    assignments.add(new AssignmentNameAndLink(assignmentName, new Link(LinkType.ASSIGNMENT,
                            assignmentId)));
                }

                days.add(new AssignmentDay(date, assignments));
            }

            return new AssignmentsWeek(days);
        });
    }

    public CompletableFuture<List<AssignmentAll>> fetchAllAssignments(String cookie) {
        HttpGet get = new HttpGet(BASE_URL
                + "parents/AssignmentViewAll_Calendar.aspx?&showrecentassignments=true&datemode=4");

        ApacheCompletableFuture<HttpResponse> future = new ApacheCompletableFuture<>();

        client.execute(get, generateCookieContext(cookie), future);

        return future.thenApply(this::fetchDocument).thenApply(document -> {
            Element tableBody = document.select("table#tblMain > tbody").first();

            List<AssignmentAll> assignments = new ArrayList<>();

            for (Element tableRow : tableBody.children()) {
                if (tableRow.children().size() == 1) {
                    //Totals - TODO?
                    continue;
                }

                String due = tableRow.child(0).text();

                String clazz = tableRow.child(1).text();

                Element assignment = tableRow.child(2);

                String assignmentId = assignment.child(0).attr("href")
                        .replace("../parents/AssignmentView.aspx?TestNameID=", "");

                String notes = tableRow.child(3).text();

                String resources = tableRow.child(4).text();

                assignments.add(new AssignmentAll(assignmentId, due, clazz, assignment.text(), notes, resources));
            }

            return assignments;
        });
    }

    public CompletableFuture<FullAssignment> fetchFullAssignment(String cookie, String id) {
        HttpGet get = new HttpGet(BASE_URL + "parents/AssignmentView.aspx?TestNameID=" + id);

        ApacheCompletableFuture<HttpResponse> future = new ApacheCompletableFuture<>();

        client.execute(get, generateCookieContext(cookie), future);

        return future.thenApply(this::fetchDocument).thenApply(document -> {
            String activity = document.select("h2#titleHeader").text().replace("Assignment: ", "");

            String clazz = null, instructor = null, assigned = null, due = null, possiblePoints = null;

            String category = null, notes = null, resourceName = null, resourceLink = null;

            List<Resource> resources = new ArrayList<>();

            for (Element resourceElement : document.select("table#ResourceTbl > tbody > tr")) {
                Element link = resourceElement.getElementsByTag("a").first();

                resources.add(new Resource(link.text(), link.attr("href")));
            }

            for (Element tableRow : document.select("table#DetailsTbl > tbody > tr")) {
                int childrenSize = tableRow.children().size();

                if (childrenSize != 2) {
                    continue;
                }

                String key = tableRow.child(0).text();

                key = key.substring(0, key.length() - 1);

                String value = tableRow.child(1).text();

                if (key.equalsIgnoreCase("class")) {
                    clazz = value;
                } else if (key.equalsIgnoreCase("instructor")) {
                    instructor = value;
                } else if (key.equalsIgnoreCase("assigned")) {
                    assigned = value;
                } else if (key.equalsIgnoreCase("due")) {
                    due = value;
                } else if (key.equalsIgnoreCase("possible points")) {
                    possiblePoints = value;
                } else if (key.equalsIgnoreCase("category")) {
                    category = value;
                } else if (key.equalsIgnoreCase("notes")) {
                    notes = value;
                }
            }

            return new FullAssignment(activity, clazz, instructor, assigned, due, possiblePoints, category, notes,
                    resources);
        });
    }

    public CompletableFuture<Announcement> fetchAnnouncement(String cookie, String id) {
        HttpGet get = new HttpGet(BASE_URL + "parents/AnnouncementView.aspx?AnnouncementID=" + id);

        ApacheCompletableFuture<HttpResponse> future = new ApacheCompletableFuture<>();

        client.execute(get, generateCookieContext(cookie), future);

        return future.thenApply(this::fetchDocument).thenApply(document -> {
            Element tableBody = document.select("table.ViewAllTable > tbody").first();

            Elements rows = tableBody.select("tr");

            String from = rows.first().select("td").last().text();

            String to = rows.get(1).select("td").last().html();

            to = to.replace("<br>", ", ");

            String date = rows.get(2).select("td").last().text();

            String subject = rows.get(3).select("td").last().text();

            String description = rows.last().select("td").first().html();

            return new Announcement(from, to, date, subject, description);
        });
    }

    public void close() {
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}