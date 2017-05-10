package com.nicholasnassar.sfsapi;

import com.nicholasnassar.sfsapi.data.Announcement;
import com.nicholasnassar.sfsapi.data.LoginResult;
import com.nicholasnassar.sfsapi.data.NewsFeedItem;
import com.nicholasnassar.sfsapi.data.assignments.*;
import com.nicholasnassar.sfsapi.data.gpa.GPACalculation;
import com.nicholasnassar.sfsapi.data.gpa.GPAClass;
import com.nicholasnassar.sfsapi.data.grades.*;
import com.nicholasnassar.sfsapi.data.links.*;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SFS {
    private static final String COOKIE_URL = "www.teacherease.com";

    private static final String BASE_URL = "https://www.teacherease.com/";

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.101 Safari/537.36";

    private static final String CURRENT_YEAR = "2016-17";

    private static final String CURRENT_TERM = "Q4";

    private static final String[] PAST_TERMS = {"Q1", "Q2", "Q3", "Q4"};

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
                System.out.println("Couldn't log someone in:");

                throwable.printStackTrace();

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
        HttpGet get = new HttpGet(BASE_URL + "parents/main.aspx?");

        ApacheCompletableFuture<HttpResponse> future = new ApacheCompletableFuture<>();

        client.execute(get, generateCookieContext(cookie), future);

        return future.thenApply(this::fetchDocument).thenApply(document -> {
            List<NewsFeedItem> items = new ArrayList<>();

            try {
                Element element = document.select("div.feed-background").first();

                for (Element div : element.getElementsByTag("div")) {
                    if (div.id().startsWith("NewsFeedItem_")) {
                        Element item = div.select("div").first();

                        //Element headerLink = item.select("div.newsfeedheader > div.newsfeedheadertext > a").first();
                        Element linkAndTime = div.select("div.text-right").first();

                        Element className = linkAndTime.select("span.title").first();

                        Element time = linkAndTime.select("span.time-ago").first();

                        Elements contentLinks = item.select("div.col-xs-9 > a");

                        Link link = null;

                        if (!contentLinks.isEmpty()) {
                            String linkHref = handleLinkHref(contentLinks.first());

                            link = LinkType.generateLink(linkHref);
                        }

                        String details = item.select("div.col-xs-9").first().text();

                        items.add(new NewsFeedItem(className.text(), time.text(), details, link));
                    }
                }
            } catch (Exception e) {
                handleExceptionWithDocument(document, e);
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

    public CompletableFuture<FullGrade> fetchGradeDetails(String cookie, String classId, String cgpId) {
        HttpGet get = new HttpGet(BASE_URL + "parents/StudentProgressView.aspx?ClassID=" + classId + "&CGPID=" + cgpId);

        ApacheCompletableFuture<HttpResponse> future = new ApacheCompletableFuture<>();

        client.execute(get, generateCookieContext(cookie), future);

        return future.thenApply(this::fetchDocument).thenApply(document -> {
            Elements noScores = document.select("td[colspan=7]");

            if (noScores.text().equals("Sorry, no assignment/scores for this class.")) {
                return new FullGrade(new ArrayList<>());
            }

            List<GradeAssignment> gradeAssignments = new ArrayList<>();

            try {
                Element table = document.select("table.ViewAllTable > tbody").first();

                double totalPoints = 0, totalScore = 0;

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

                        if (assignment != null && assignment.equals("Total")) {
                            possiblePoints = totalPoints + "";

                            score = totalScore + "";
                        } else {
                            try {
                                double assignmentPoints = Double.parseDouble(possiblePoints);
                                double assignmentScore = Double.parseDouble(score);

                                totalPoints += assignmentPoints;
                                totalScore += assignmentScore;
                            } catch (Exception e) {
                                //Parse errors mean that this assignment shouldn't be counted
                            }
                        }
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
                        link = new AssignmentLink(assignmentId);
                    }

                    GradeAssignment gradeAssignment = new GradeAssignment(link, dateDue, category, assignment,
                            possiblePoints, score, percentage, letterGrade, comments);

                    gradeAssignments.add(gradeAssignment);
                }
            } catch (Exception e) {
                handleExceptionWithDocument(document, e);
            }

            return new FullGrade(gradeAssignments);
        });
    }

    private String trim(String text) {
        text = text.replace("\u00A0", "");

        return text.isEmpty() ? null : text;
    }

    public CompletableFuture<Grades> fetchGrades(String cookie, boolean showAllClasses) {
        HttpGet get = new HttpGet(BASE_URL + "parents/StudentProgressSummary_classic.aspx?bCGPActive="
                + (showAllClasses ? "NaN" : "1"));

        ApacheCompletableFuture<HttpResponse> future = new ApacheCompletableFuture<>();

        client.execute(get, generateCookieContext(cookie), future);

        return future.thenApply(this::fetchDocument).thenApply(document -> {
            List<Grade> grades = new ArrayList<>();

            List<MissingAssignment> missingWork = new ArrayList<>();

            try {
                Elements tables = document.select("table.ViewAllTable > tbody");

                List<Element> tableRows = tables.get(0).children();

                for (int i = 1; i < tableRows.size(); i++) {
                    Element element = tableRows.get(i);

                    Element currentScore = element.child(3);

                    String id = currentScore.select("a").attr("href");

                    GradeLink link = (GradeLink) LinkType.generateLink(id);

                    String term = element.child(0).text();

                    term = trim(term.replace(CURRENT_YEAR, "").trim());

                    grades.add(new Grade(link, term, trim(element.child(1).text()), trim(element.child(2).text()),
                            trim(currentScore.text())));
                }

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
            } catch (Exception e) {
                handleExceptionWithDocument(document, e);
            }

            /* Test missing work assignments
            missingWork.add(new MissingAssignment(new Link(LinkType.ASSIGNMENT, "123"), "9/30/2016", "Test Class 1",
                    "Test Assignment", "5", "5", "Some comments"));
            missingWork.add(new MissingAssignment(new Link(LinkType.ASSIGNMENT, "1243"), "9/31/2016", "Test Class 2",
                    "Test Assignment 2", "5", "5", "Some comments"));*/

            return new Grades(grades, missingWork);
        });
    }

    public CompletableFuture<GPACalculation> fetchGPA(String cookie, String term) {
        return fetchGrades(cookie, true).thenApply(grades -> {
            List<Grade> allGrades = grades.getGrades();

            List<GPAClass> classes = new ArrayList<>();

            try {
                double classesCount = 0;

                double gpa = 0;

                double maxGPA = 0;

                String calculateTerm = term == null ? CURRENT_TERM : term;

                for (Grade grade : allGrades) {
                    String score = grade.getScore();

                    if (score.equals("Details") || score.equals("P") || !grade.getTerm().equals(calculateTerm)) {
                        continue;
                    }

                    boolean halfCredit = grade.getClazz().startsWith("PE ") || grade.getClazz().endsWith(" Flex");

                    if (halfCredit) {
                        classesCount += 0.5;
                    } else {
                        classesCount++;
                    }

                    String letterGradeFromScore = score.substring(score.indexOf(" = ") + 3);

                    LetterGrade letterGrade = LetterGrade.getLetterGrade(letterGradeFromScore);

                    GradeScale scale = GradeScale.getScale(grade.getClazz());

                    double classScore = scale.getScore(letterGrade);

                    if (halfCredit) {
                        classScore /= 2;
                    }

                    gpa += classScore;

                    maxGPA += halfCredit ? scale.getScore(LetterGrade.A_PLUS) / 2 : scale.getScore(LetterGrade.A_PLUS);

                    classes.add(new GPAClass(grade.getClazz(), letterGrade.getName(), classScore + ""));
                }

                if (classesCount == 0) {
                    return new GPACalculation(Arrays.asList(new GPAClass("No classes yet", "", "")), PAST_TERMS);
                }

                gpa /= classesCount;

                maxGPA /= classesCount;

                gpa = Double.parseDouble(gpaTruncate.format(gpa));

                maxGPA = Double.parseDouble(gpaTruncate.format(maxGPA));

                classes.add(new GPAClass("Total", "Your GPA: " + gpa, "Max: " + maxGPA));
            } catch (Exception e) {
                System.out.println("Error calculating GPA!");

                e.printStackTrace();
            }

            return new GPACalculation(classes, PAST_TERMS);
        });
    }

    public CompletableFuture<List<AssignmentTaskList>> fetchAssignmentsInTaskList(String cookie) {
        HttpGet get = new HttpGet(BASE_URL + "parents/AssignmentViewAll.aspx?&datemode=3");

        ApacheCompletableFuture<HttpResponse> future = new ApacheCompletableFuture<>();

        client.execute(get, generateCookieContext(cookie), future);

        return future.thenApply(this::fetchDocument).thenApply(document -> {
            try {
                List<AssignmentTaskList> assignments = new ArrayList<>();

                Element mainContainer = document.select("div#mainContainer").first();

                for (Element element : mainContainer.select("div.curricula-item")) {
                    Element activity = element.getElementsByTag("h4").first().select("a").first();

                    Element due = element.select("div.due").first();

                    String name = due.text().substring(0, due.text().indexOf(" Due: "));

                    String dueDate = due.text().substring(due.text().indexOf(" Due: ") + 6);

                    int resources = element.select("i.cmg.small.resources").size();

                    String id = activity.attr("href").replace("AssignmentView.aspx?TestNameID=", "");

                    assignments.add(new AssignmentTaskList(id, name, activity.text(), "Due " + dueDate, resources));
                }

                return assignments;
            } catch (Exception e) {
                handleExceptionWithDocument(document, e);

                return new ArrayList<AssignmentTaskList>();
            }
        });
    }

    private void handleExceptionWithDocument(Document document, Exception exception) {
        System.out.println("Exception thrown on:");
        System.out.println(document.html());
        exception.printStackTrace();
    }

    public CompletableFuture<AssignmentsWeek> fetchAssignmentsInMonth(String cookie) {
        HttpGet get = new HttpGet(BASE_URL + "parents/AssignmentViewAll_Calendar.aspx?&showrecentassignments=true&datemode=1");

        ApacheCompletableFuture<HttpResponse> future = new ApacheCompletableFuture<>();

        client.execute(get, generateCookieContext(cookie), future);

        return future.thenApply(this::fetchDocument).thenApply(document -> {
            List<AssignmentDay> days = new ArrayList<>();

            try {
                Element tableBody = document.select("table#tblMain.calendar.ViewAllTableGrid > tbody").first();

                for (Element tableRow : tableBody.children()) {
                    for (Element tableData : tableRow.children()) {
                        if (tableData.hasClass("inactiveday")) {
                            continue;
                        }

                        String date = tableData.select("span.datelabel").text();

                        Elements assignmentsInDay = tableData.select("div.assignment");

                        List<AssignmentNameAndLink> assignments = new ArrayList<>();

                        for (Element assignment : assignmentsInDay) {
                            String assignmentId = assignment.id().substring(1);

                            String assignmentName = assignment.text();

                            assignments.add(new AssignmentNameAndLink(assignmentName, new AssignmentLink(assignmentId)));
                        }

                        days.add(new AssignmentDay(date, assignments));
                    }
                }
            } catch (Exception e) {
                handleExceptionWithDocument(document, e);
            }

            return new AssignmentsWeek(days);
        });
    }

    public CompletableFuture<AssignmentsWeek> fetchAssignmentsInWeek(String cookie) {
        HttpGet get = new HttpGet(BASE_URL + "parents/AssignmentViewAll_Calendar.aspx?&showrecentassignments=true&datemode=2");

        ApacheCompletableFuture<HttpResponse> future = new ApacheCompletableFuture<>();

        client.execute(get, generateCookieContext(cookie), future);

        return future.thenApply(this::fetchDocument).thenApply(document -> {
            List<AssignmentDay> days = new ArrayList<>();

            try {
                Element tableBody = document.select("table#tblMain > tbody").first();

                for (Element tableRow : tableBody.children()) {
                    String date = tableRow.child(0).text();

                    List<AssignmentNameAndLink> assignments = new ArrayList<>();

                    for (Element assignment : tableRow.select("div.assignment")) {
                        String assignmentId = assignment.id().substring(1);

                        String assignmentName = assignment.text();

                        assignments.add(new AssignmentNameAndLink(assignmentName, new AssignmentLink(assignmentId)));
                    }

                    days.add(new AssignmentDay(date, assignments));
                }
            } catch (Exception e) {
                handleExceptionWithDocument(document, e);
            }

            return new AssignmentsWeek(days);
        });
    }

    public CompletableFuture<List<AssignmentAll>> fetchAllAssignments(String cookie, String filteredClass) {
        HttpGet get = new HttpGet(BASE_URL
                + "parents/AssignmentViewAll_Calendar.aspx?&showrecentassignments=true&datemode=4");

        ApacheCompletableFuture<HttpResponse> future = new ApacheCompletableFuture<>();

        client.execute(get, generateCookieContext(cookie), future);

        return future.thenApply(this::fetchDocument).thenApply(document -> {
            List<AssignmentAll> assignments = new ArrayList<>();

            try {
                Element tableBody = document.select("table#tblMain > tbody").first();

                for (Element tableRow : tableBody.children()) {
                    if (tableRow.children().size() == 1) {
                        //Totals - TODO?
                        continue;
                    }

                    String due = tableRow.child(0).text();

                    String clazz = tableRow.child(1).text();

                    if (filteredClass == null || filteredClass.equals(clazz)) {
                        Element assignment = tableRow.child(2);

                        String assignmentId = assignment.child(0).attr("href")
                                .replace("../parents/AssignmentView.aspx?TestNameID=", "");

                        String notes = tableRow.child(3).text();

                        String resources = tableRow.child(4).text();

                        assignments.add(new AssignmentAll(assignmentId, due, clazz, assignment.text(), notes, resources));
                    }
                }
            } catch (Exception e) {
                handleExceptionWithDocument(document, e);
            }

            return assignments;
        });
    }

    public CompletableFuture<FullAssignment> fetchFullAssignment(String cookie, String id) {
        HttpGet get = new HttpGet(BASE_URL + "parents/AssignmentView.aspx?TestNameID=" + id);

        ApacheCompletableFuture<HttpResponse> future = new ApacheCompletableFuture<>();

        client.execute(get, generateCookieContext(cookie), future);

        return future.thenApply(this::fetchDocument).thenApply(document -> {
            try {
                String activity = document.select("span.pull-left").text().replace("Assignment: ", "");

                List<Resource> resources = new ArrayList<>();

                for (Element resourceElement : document.select("table#ResourceTbl > tbody > tr")) {
                    Element link = resourceElement.getElementsByTag("a").first();

                    resources.add(new Resource(link.text(), link.attr("href")));
                }

                String clazz = getTextFromId(document, "ClassName");

                String instructor = getTextFromId(document, "InstructorName");

                String assigned = getTextFromId(document, "DateAssigned");

                String due = getTextFromId(document, "DateDue");

                String possiblePoints = getTextFromId(document, "PossiblePoints");

                String category = getTextFromId(document, "Category");

                String notes = getTextFromId(document, "AssignmentNotes");

                return new FullAssignment(activity, clazz, instructor, assigned, due, possiblePoints, category, notes,
                        resources);
            } catch (Exception e) {
                handleExceptionWithDocument(document, e);

                return new FullAssignment(null, null, null, null, null, null, null, null, new ArrayList<>());
            }
        });
    }

    private String getTextFromId(Document document, String id) {
        Elements elements = document.getElementsByAttributeValue("id", id);

        if (elements.isEmpty()) {
            return null;
        } else {
            return elements.text();
        }
    }

    public CompletableFuture<Announcement> fetchAnnouncement(String cookie, String id) {
        HttpGet get = new HttpGet(BASE_URL + "parents/AnnouncementView.aspx?AnnouncementID=" + id);

        ApacheCompletableFuture<HttpResponse> future = new ApacheCompletableFuture<>();

        client.execute(get, generateCookieContext(cookie), future);

        return future.thenApply(this::fetchDocument).thenApply(document -> {
            String from = document.select("div#AnnouncementFrom").first().text();

            String to = document.select("div#AnnouncementTo").first().text();

            to = to.replace("<br>", ", ");

            String date = document.select("div#AnnouncementDate").first().text();

            String subject = document.select("div#AnnouncementSubject").first().text();

            String description = document.select("div#AnnouncementBody").first().html();

            return new Announcement(from, to, date, subject, description);
        });
    }

    public CompletableFuture<String> fetchReportCard(String cookie) {
        HttpGet get = new HttpGet(BASE_URL + "parents/PRViewAll.aspx");

        ApacheCompletableFuture<HttpResponse> future = new ApacheCompletableFuture<>();

        client.execute(get, generateCookieContext(cookie), future);

        return future.thenApply(this::fetchDocument).thenCompose(document -> {
            Elements tableDatas = document.getElementsByTag("td");

            for (Element tableData : tableDatas) {
                if (tableData.text().equals("view")) {
                    ApacheCompletableFuture<HttpResponse> secondFuture = new ApacheCompletableFuture<>();

                    String url = tableData.select("td > a").attr("href");

                    url = url.replace("../", BASE_URL);

                    client.execute(new HttpGet(url), generateCookieContext(cookie), secondFuture);

                    return secondFuture;
                }
            }

            return null;
        }).thenApply(response -> {
            try {
                return fromInputStream(response.getEntity().getContent());
            } catch (IOException e) {
                return "error!";
            }
        });
    }

    private String fromInputStream(InputStream stream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];

        int length;

        while ((length = stream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }

        return result.toString("windows-1252");
    }

    public void close() {
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}