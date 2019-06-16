package com.johnturkson.courses;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
                        .build(), HttpResponse.BodyHandlers.ofString())
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
        Set<Section> sections = new LinkedHashSet<>();
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
                            .build(), HttpResponse.BodyHandlers.ofString())
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
        
        return new ArrayList<>(sections);
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
                    .build(), Location.newBuilder()
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
        return instructorMatcher.find() ? instructorMatcher.group("instructor").trim() : "";
    }
    
    public static void exportAsJSON(List<Section> courses, Path location) {
        try {
            Files.writeString(location, new LinkedHashSet<>(courses).stream()
                    .map(Section::toJSON)
                    .map(s -> "\t" + s)
                    .map(s -> s.replace("\n", "\n\t"))
                    .collect(Collectors.joining(",\n", "[\n", "\n]")));
            
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    public static List<Section> importFromJSON(Path location) {
        try {
            String json = Files.readString(location);
            Pattern sectionPattern = Pattern.compile("(?s)\\{\\s*" +
                    "\"subject\":\\s*\"(?<subject>[^\"]*)\",\\s*" +
                    "\"course\":\\s*\"(?<course>[^\"]*)\",\\s*" +
                    "\"code\":\\s*\"(?<code>[^\"]*)\",\\s*" +
                    "\"activity\":\\s*\"(?<activity>[^\"]*)\",\\s*" +
                    "\"term\":\\s*(?<term>\\d+),\\s*" +
                    "\"schedule\":\\s*\\[\\s*(?<schedule>(?:(?:\\{.*?})(?:,.*?)*)?)\\s*],\\s*" +
                    "\"instructor\":\\s*\"(?<instructor>[^\"]*)\",\\s*" +
                    "\"url\":\\s*\"(?<url>[^\"]*)\"\\s*" +
                    "}");
            Matcher sectionMatcher = sectionPattern.matcher(json);
            return extractSections(sectionMatcher, "JSON");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    public static void exportAsCSV(List<Section> courses, Path location) {
        try {
            Files.writeString(location, new LinkedHashSet<>(courses).stream()
                    .map(Section::toCSV)
                    .collect(Collectors.joining("\n")));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    public static List<Section> importFromCSV(Path location) {
        try {
            String json = Files.readString(location);
            Pattern sectionPattern = Pattern.compile("\"(?<subject>[^\"]*)\"," +
                    "\"(?<course>[^\"]*)\"," +
                    "\"(?<code>[^\"]*)\"," +
                    "\"(?<activity>[^\"]*)\"," +
                    "\"(?<term>\\d+)\"," +
                    "\"(?<schedule>[^\"]*)\"," +
                    "\"(?<instructor>[^\"]*)\"," +
                    "\"(?<url>[^\"]*)\"");
            Matcher sectionMatcher = sectionPattern.matcher(json);
            return extractSections(sectionMatcher, "CSV");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    private static List<Section> extractSections(Matcher sectionMatcher, String format) {
        Set<Section> sections = new LinkedHashSet<>();
        while (sectionMatcher.find()) {
            Map<Time, Location> schedule;
            Pattern schedulePattern;
            Matcher scheduleMatcher;
            if (format.equals("JSON")) {
                schedulePattern = Pattern.compile("\\{\\s*" +
                        "\"days\":\\s*\"(?<days>[^\"]*)\",\\s*" +
                        "\"startHour\":\\s*(?<startHour>[0]?[0-9]|[1][0-9]|[2][0-3]),\\s*" +
                        "\"startMinute\":\\s*(?<startMinute>[0]?[0-9]|[1-5][0-9]),\\s*" +
                        "\"endHour\":\\s*(?<endHour>[0]?[0-9]|[1][0-9]|[2][0-3]),\\s*" +
                        "\"endMinute\":\\s*(?<endMinute>[0]?[0-9]|[1-5][0-9]),\\s*" +
                        "\"name\":\\s*\"(?<name>[^\"]*)\",\\s*" +
                        "\"code\":\\s*\"(?<code>[^\"]*)\",\\s*" +
                        "\"room\":\\s*\"(?<room>[^\"]*)\"\\s*}");
                scheduleMatcher = schedulePattern.matcher(sectionMatcher.group("schedule"));
                schedule = parseSchedule(scheduleMatcher);
            } else if (format.equals("CSV")) {
                schedulePattern = Pattern.compile("(?<days>[^,]*)," +
                        "(?<startHour>[0]?[0-9]|[1][0-9]|[2][0-3])," +
                        "(?<startMinute>[0]?[0-9]|[1-5][0-9])," +
                        "(?<endHour>[0]?[0-9]|[1][0-9]|[2][0-3])," +
                        "(?<endMinute>[0]?[0-9]|[1-5][0-9])," +
                        "(?<name>[^,]*)," +
                        "(?<code>[^,]*)," +
                        "(?<room>[^,]*)");
                scheduleMatcher = schedulePattern.matcher(sectionMatcher.group("schedule"));
                schedule = parseSchedule(scheduleMatcher);
            } else {
                throw new IllegalArgumentException("Unsupported format: " + format);
            }
            
            try {
                sections.add(Section.newBuilder()
                        .subject(sectionMatcher.group("subject"))
                        .course(sectionMatcher.group("course"))
                        .code(sectionMatcher.group("code"))
                        .activity(sectionMatcher.group("activity"))
                        .term(Integer.parseInt(sectionMatcher.group("term")))
                        .schedule(schedule)
                        .instructor(sectionMatcher.group("instructor"))
                        .url(new URL(sectionMatcher.group("url")))
                        .build());
            } catch (MalformedURLException e) {
                throw new UncheckedIOException(e);
            }
        }
        return new ArrayList<>(sections);
    }
    
    private static Map<Time, Location> parseSchedule(Matcher scheduleMatcher) {
        Map<Time, Location> schedule = new LinkedHashMap<>();
        while (scheduleMatcher.find()) {
            schedule.put(Time.newBuilder()
                    .days(Arrays.stream(scheduleMatcher.group("days")
                            .split(" "))
                            .collect(Collectors.toList()))
                    .startHour(Integer.parseInt(scheduleMatcher.group("startHour")))
                    .startMinute(Integer.parseInt(scheduleMatcher.group("startMinute")))
                    .endHour(Integer.parseInt(scheduleMatcher.group("endHour")))
                    .endMinute(Integer.parseInt(scheduleMatcher.group("endMinute")))
                    .build(), Location.newBuilder()
                    .name(scheduleMatcher.group("name"))
                    .code(scheduleMatcher.group("code"))
                    .room(scheduleMatcher.group("room"))
                    .build());
        }
        return schedule;
    }
}
