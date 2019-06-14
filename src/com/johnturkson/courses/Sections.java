package com.johnturkson.courses;

import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Sections {
    public static Optional<Section> getSection(String subject, String course, String code) {
        String query = "https://courses.students.ubc.ca/cs/courseschedule?" +
                "pname=subjarea&" +
                "tname=subj-section&" +
                "dept=" + subject.toUpperCase().trim() + "&" +
                "course=" + course.toUpperCase().trim() + "&" +
                "section=" + code.toUpperCase().trim();
        
        String response = HttpClient.newHttpClient()
                .sendAsync(HttpRequest.newBuilder()
                                .uri(URI.create(query))
                                .GET()
                                .build(),
                        HttpResponse.BodyHandlers.ofString())
                .join()
                .body();
        
        Pattern sectionDetailsPattern = Pattern.compile("<h4>" +
                "(?<subject>[^\\s]+)\\s" +
                "(?<code>[^\\s]+)\\s" +
                "(?<name>[^<]+)\\s" +
                "\\((?<activity>[^)]*)\\)" +
                "</h4>" +
                ".+?" +
                "<b>Term (?<term>\\d+)</b>");
        Matcher sectionDetailsMatcher = sectionDetailsPattern.matcher(response);
        
        if (sectionDetailsMatcher.find()) {
            try {
                return Optional.of(Section.newBuilder()
                        .subject(sectionDetailsMatcher.group("subject").trim())
                        .course(sectionDetailsMatcher.group("code").trim())
                        .code(sectionDetailsMatcher.group("name").trim())
                        .activity(sectionDetailsMatcher.group("activity").trim())
                        .term(Integer.parseInt(sectionDetailsMatcher.group("term")))
                        .schedule(parseSectionSchedule(response))
                        .instructor(parseSectionInstructor(response))
                        .url(new URL(query))
                        .build());
            } catch (MalformedURLException e) {
                throw new UncheckedIOException(e);
            }
        } else {
            return Optional.empty();
        }
    }
    
    public static List<Section> getSections(String subject, String course) {
        List<Section> sections = new ArrayList<>();
        String query = "https://courses.students.ubc.ca/cs/courseschedule?" +
                "pname=subjarea&" +
                "tname=subj-course&" +
                "dept=" + subject.trim() + "&" +
                "course=" + course.trim();
        
        String response = HttpClient.newHttpClient()
                .sendAsync(HttpRequest.newBuilder()
                        .uri(URI.create(query))
                        .GET()
                        .build(), HttpResponse.BodyHandlers.ofString())
                .join()
                .body();
        
        Pattern sectionPattern = Pattern.compile("<tr[^>]+>" +
                "<td>(?<status>[^<]*)</td>" +
                "<td><a href=" +
                "(?<url>[^\\s]+)[^>]*>" +
                "(?<subject>[^\\s]+)\\s" +
                "(?<course>[^\\s]+)\\s" +
                "(?<code>[^<]+)" +
                "</a></td>" +
                "<td>(?<activity>[^<]*)</td>" +
                "<td>(?<term>\\d+)</td>");
        Matcher sectionMatcher = sectionPattern.matcher(response);
        
        while (sectionMatcher.find()) {
            String details = HttpClient.newHttpClient()
                    .sendAsync(HttpRequest.newBuilder()
                                    .uri(URI.create("https://courses.students.ubc.ca" +
                                            sectionMatcher.group("url").replace("&amp;", "&")))
                                    .GET()
                                    .build(),
                            HttpResponse.BodyHandlers.ofString())
                    .join()
                    .body();
            
            try {
                sections.add(Section.newBuilder()
                        .subject(sectionMatcher.group("subject").trim())
                        .course(sectionMatcher.group("course").trim())
                        .code(sectionMatcher.group("code").trim())
                        .activity(sectionMatcher.group("activity").trim())
                        .term(Integer.parseInt(sectionMatcher.group("term").trim()))
                        .schedule(parseSectionSchedule(details))
                        .instructor(parseSectionInstructor(details))
                        .url(new URL("https://courses.students.ubc.ca" +
                                sectionMatcher.group("url").replace("&amp;", "&")))
                        .build());
            } catch (MalformedURLException e) {
                throw new UncheckedIOException(e);
            }
        }
        
        return sections;
    }
    
    public static List<Section> getSections(Course course) {
        return getSections(course.getSubject(), course.getCode());
    }
    
    private static Map<Time, Location> parseSectionSchedule(String details) {
        Pattern sectionSchedulePattern = Pattern.compile("<td>(?<term>\\d+)</td>" +
                "<td>(?<days>[^<]*)</td>" +
                "<td>(?<startHour>[0-9]|[0-1][0-9]|[2][0-3]):(?<startMinute>[0-5][0-9])</td>" +
                "<td>(?<endHour>[0-9]|[0-1][0-9]|[2][0-3]):(?<endMinute>[0-5][0-9])</td>" +
                "<td>(?<name>[^<]*)</td>" +
                "<td>(?:<[^>]*buildingID=(?<code>[^&]*)[^>]*>)?(?<room>[^<]*)(?:<[^>]*>)?</td>");
        Matcher sectionScheduleMatcher = sectionSchedulePattern.matcher(details);
        
        Map<Time, Location> schedule = new LinkedHashMap<>();
        while (sectionScheduleMatcher.find()) {
            schedule.put(Time.newBuilder()
                            .days(Arrays.asList(sectionScheduleMatcher.group("days").trim().split(" ")))
                            .startHour(Integer.parseInt(sectionScheduleMatcher.group("startHour")))
                            .startMinute(Integer.parseInt(sectionScheduleMatcher.group("startMinute")))
                            .endHour(Integer.parseInt(sectionScheduleMatcher.group("endHour")))
                            .endMinute(Integer.parseInt(sectionScheduleMatcher.group("endMinute")))
                            .build(),
                    Location.newBuilder()
                            .name(sectionScheduleMatcher.group("name").trim())
                            .code(Objects.requireNonNullElse(sectionScheduleMatcher.group("code"), "").trim())
                            .room(sectionScheduleMatcher.group("room").trim())
                            .build());
        }
        
        return schedule;
    }
    
    private static String parseSectionInstructor(String details) {
        Pattern instructorPattern = Pattern.compile("<td>Instructor:\\s*</td>" +
                "<td>(?:<[^>]*>)?(?<instructor>[^<]*)(?:<[^>]*>)?</td>");
        Matcher instructorMatcher = instructorPattern.matcher(details);
        return instructorMatcher.find() ?
                instructorMatcher.group("instructor").trim() : "";
    }
}
