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

public class Courses {
    public static Optional<Course> getCourse(String subject, String code) {
        String query = "https://courses.students.ubc.ca/cs/courseschedule?" +
                "pname=subjarea&" +
                "tname=subj-course&" +
                "dept=" + subject.trim() + "&" +
                "course=" + code.trim();
        
        String response = HttpClient.newHttpClient()
                .sendAsync(HttpRequest.newBuilder()
                        .uri(URI.create(query))
                        .GET()
                        .build(), HttpResponse.BodyHandlers.ofString())
                .join()
                .body();
        
        Pattern courseDetailsPattern = Pattern.compile("<[^>]*>" +
                "(?<subject>[^\\s]+)\\s" +
                "(?<code>[^\\s]+)\\s" +
                "(?<name>[^<]+)" +
                "</[^>]*>" +
                "<p>(?<description>[^<]*)</p>" +
                ".+?" +
                "<p>Credits:\\s(?<credits>\\d+)</p>");
        Matcher courseDetailsMatcher = courseDetailsPattern.matcher(response);
        
        if (courseDetailsMatcher.find()) {
            try {
                return Optional.of(Course.newBuilder()
                        .subject(subject.trim())
                        .code(code.trim())
                        .name(courseDetailsMatcher.group("name").trim())
                        .description(courseDetailsMatcher.group("description")
                                .trim()
                                .replace("\\\\", "\\")
                                .replace("\\\"", "\""))
                        .credits(Integer.parseInt(courseDetailsMatcher.group("credits").trim()))
                        .url(new URL(query))
                        .build());
            } catch (MalformedURLException e) {
                throw new UncheckedIOException(e);
            }
        } else {
            return Optional.empty();
        }
    }
    
    public static List<Course> getCourses(Subject subject) {
        return getCourses(subject.getCode());
    }
    
    public static List<Course> getCourses(String subject) {
        Set<Course> courses = new LinkedHashSet<>();
        
        String response = HttpClient.newHttpClient()
                .sendAsync(HttpRequest.newBuilder()
                        .uri(URI.create("https://courses.students.ubc.ca/cs/courseschedule?" +
                                "pname=subjarea&" +
                                "tname=subj-department&" +
                                "dept=" + subject.trim()))
                        .GET()
                        .build(), HttpResponse.BodyHandlers.ofString())
                .join()
                .body();
        
        Pattern coursePattern = Pattern.compile("<tr[^>]*>" +
                "<td><a href=" +
                "(?<url>[^>]+)>" +
                "(?<subject>[^\\s]+)\\s" +
                "(?<code>[^<]+)</a></td>" +
                "<td>(?<name>[^<]+)</td></a>" +
                "</tr>");
        Matcher courseMatcher = coursePattern.matcher(response);
        
        while (courseMatcher.find()) {
            String details = HttpClient.newHttpClient()
                    .sendAsync(HttpRequest.newBuilder()
                                    .uri(URI.create("https://courses.students.ubc.ca" +
                                            courseMatcher.group("url").replace("&amp;", "&")))
                                    .GET()
                                    .build(),
                            HttpResponse.BodyHandlers.ofString())
                    .join()
                    .body();
            
            Pattern courseDetailsPattern = Pattern.compile("<[^>]*>" +
                    "(?<subject>[^\\s]+)\\s" +
                    "(?<code>[^\\s]+)\\s" +
                    "(?<name>[^<]+)" +
                    "</[^>]*>" +
                    "<p>(?<description>[^<]*)</p>" +
                    ".+?" +
                    "<p>Credits:\\s(?<credits>\\d+)</p>");
            Matcher courseDetailsMatcher = courseDetailsPattern.matcher(details);
            
            if (courseDetailsMatcher.find()) {
                try {
                    courses.add(Course.newBuilder()
                            .subject(courseMatcher.group("subject").trim())
                            .code(courseMatcher.group("code").trim())
                            .name(courseMatcher.group("name").trim())
                            .description(courseDetailsMatcher.group("description")
                                    .trim()
                                    .replace("\\\\", "\\")
                                    .replace("\\\"", "\""))
                            .credits(Integer.parseInt(courseDetailsMatcher.group("credits")))
                            .url(new URL("https://courses.students.ubc.ca" +
                                    courseMatcher.group("url").replace("&amp;", "&")))
                            .build());
                } catch (MalformedURLException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }
        
        return new ArrayList<>(courses);
    }
    
    public static void exportAsJSON(List<Course> courses, Path location) {
        try {
            Files.writeString(location, new LinkedHashSet<>(courses).stream()
                    .map(Course::toJSON)
                    .map(s -> "\t" + s)
                    .map(s -> s.replace("\n", "\n\t"))
                    .collect(Collectors.joining(",\n", "[\n", "\n]")));
            
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    public static List<Course> importFromJSON(Path location) {
        try {
            String json = Files.readString(location);
            Pattern coursePattern = Pattern.compile("\\{\\s*" +
                    "\"subject\":\\s*\"(?<subject>[^\"]*)\",\\s*" +
                    "\"code\":\\s*\"(?<code>[^\"]*)\",\\s*" +
                    "\"name\":\\s*\"(?<name>[^\"]*)\",\\s*" +
                    "\"description\":\\s*\"(?<description>.*)\",\\s*" +
                    "\"credits\":\\s*(?<credits>[^\"]*),\\s*" +
                    "\"url\":\\s*\"(?<url>[^\"]*)\"\\s*" +
                    "}");
            Matcher courseMatcher = coursePattern.matcher(json);
            return extractCourses(courseMatcher);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    public static void exportAsCSV(List<Course> courses, Path location) {
        try {
            Files.writeString(location, new LinkedHashSet<>(courses).stream()
                    .map(Course::toCSV)
                    .collect(Collectors.joining("\n")));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    public static List<Course> importFromCSV(Path location) {
        try {
            String json = Files.readString(location);
            Pattern coursePattern = Pattern.compile("\"(?<subject>[^\"]*)\"," +
                    "\"(?<code>[^\"]*)\"," +
                    "\"(?<name>[^\"]*)\"," +
                    "\"(?<description>.*)\"," +
                    "\"(?<credits>[^\"]*)\"," +
                    "\"(?<url>[^\"]*)\"");
            Matcher courseMatcher = coursePattern.matcher(json);
            return extractCourses(courseMatcher);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    private static List<Course> extractCourses(Matcher courseMatcher) {
        Set<Course> courses = new LinkedHashSet<>();
        try {
            while (courseMatcher.find()) {
                courses.add(Course.newBuilder()
                        .subject(courseMatcher.group("subject"))
                        .code(courseMatcher.group("code"))
                        .name(courseMatcher.group("name"))
                        .description(courseMatcher.group("description")
                                .replace("\\\\", "\\")
                                .replace("\\\"", "\""))
                        .credits(Integer.parseInt(courseMatcher.group("credits")))
                        .url(new URL(courseMatcher.group("url")))
                        .build());
            }
            return new ArrayList<>(courses);
        } catch (MalformedURLException e) {
            throw new UncheckedIOException(e);
        }
    }
}
