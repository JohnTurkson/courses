package com.johnturkson.courses;

import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        List<Subject> subjects = new ArrayList<>();
        String response = HttpClient.newBuilder().build()
                .sendAsync(HttpRequest.newBuilder()
                                .uri(URI.create("https://courses.students.ubc.ca/cs/courseschedule?" +
                                        "pname=subjarea&" +
                                        "tname=subj-all-departments"))
                                .GET()
                                .build(),
                        HttpResponse.BodyHandlers.ofString())
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
        return subjects;
    }
}
