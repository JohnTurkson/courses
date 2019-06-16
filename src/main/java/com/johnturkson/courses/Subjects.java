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

public class Subjects {
    public static Optional<Subject> getSubjectByCode(String code) {
        return getSubjects().stream()
                .filter(s -> s.getCode().trim().equals(code.trim()))
                .findFirst();
    }
    
    public static Optional<Subject> getSubjectByName(String name) {
        return getSubjects().stream()
                .filter(s -> s.getName().trim().equals(name.trim()))
                .findFirst();
    }
    
    public static List<Subject> getSubjects() {
        Set<Subject> subjects = new LinkedHashSet<>();
        String response = HttpClient.newBuilder().build()
                .sendAsync(HttpRequest.newBuilder()
                        .uri(URI.create("https://courses.students.ubc.ca/cs/courseschedule?" +
                                "pname=subjarea&" +
                                "tname=subj-all-departments"))
                        .GET()
                        .build(), HttpResponse.BodyHandlers.ofString())
                .join()
                .body();
        
        Pattern subjectPattern = Pattern.compile("<tr[^>]*>" +
                "<td><a href=" +
                "(?<url>[^>]+)>" +
                "(?<code>[^<]+)" +
                "</a></td>" +
                "<td[^>]*>(?<name>[^<]+)</td>" +
                "<td[^>]*>(?<faculty>[^<]+)</td>" +
                "</tr>");
        Matcher subjectMatcher = subjectPattern.matcher(response);
        
        while (subjectMatcher.find()) {
            try {
                subjects.add(Subject.newBuilder()
                        .code(subjectMatcher.group("code").trim())
                        .name(subjectMatcher.group("name").trim())
                        .faculty(subjectMatcher.group("faculty").trim())
                        .url(new URL("https://courses.students.ubc.ca" +
                                subjectMatcher.group("url").replace("&amp;", "&")))
                        .build());
            } catch (MalformedURLException e) {
                throw new UncheckedIOException(e);
            }
        }
        return new ArrayList<>(subjects);
    }
    
    public static List<Subject> importFromJSON(Path location) {
        try {
            String json = Files.readString(location);
            Pattern subjectPattern = Pattern.compile("\\{\\s*" +
                    "\"code\":\\s*\"(?<code>[^\"]*)\",\\s*" +
                    "\"name\":\\s*\"(?<name>[^\"]*)\",\\s*" +
                    "\"faculty\":\\s*\"(?<faculty>[^\"]*)\",\\s*" +
                    "\"url\":\\s*\"(?<url>[^\"]*)\"\\s*" +
                    "}");
            Matcher subjectMatcher = subjectPattern.matcher(json);
            return extractSubjects(subjectMatcher);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    public static void exportAsJSON(List<Subject> subjects, Path location) {
        try {
            Files.writeString(location, new LinkedHashSet<>(subjects).stream()
                    .map(Subject::toJSON)
                    .map(s -> "\t" + s)
                    .map(s -> s.replace("\n", "\n\t"))
                    .collect(Collectors.joining(",\n", "[\n", "\n]")));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    public static List<Subject> importFromCSV(Path location) {
        try {
            String csv = Files.readString(location);
            Pattern subjectPattern = Pattern.compile("\"(?<code>[^\"]*)\"," +
                    "\"(?<name>[^\"]*)\"," +
                    "\"(?<faculty>[^\"]*)\"," +
                    "\"(?<url>[^\"]*)\"");
            Matcher subjectMatcher = subjectPattern.matcher(csv);
            return extractSubjects(subjectMatcher);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    public static void exportAsCSV(List<Subject> subjects, Path location) {
        try {
            Files.writeString(location, new LinkedHashSet<>(subjects).stream()
                    .map(Subject::toCSV)
                    .collect(Collectors.joining("\n")));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    private static List<Subject> extractSubjects(Matcher subjectMatcher) {
        Set<Subject> subjects = new LinkedHashSet<>();
        try {
            while (subjectMatcher.find()) {
                subjects.add(Subject.newBuilder()
                        .code(subjectMatcher.group("code"))
                        .name(subjectMatcher.group("name"))
                        .faculty(subjectMatcher.group("faculty")
                                .replace("\\\\", "\\")
                                .replace("\\\"", "\""))
                        .url(new URL(subjectMatcher.group("url")))
                        .build());
            }
            return new ArrayList<>(subjects);
        } catch (MalformedURLException e) {
            throw new UncheckedIOException(e);
        }
    }
}
