package com.johnturkson.courses;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class InformationRetriever {
    private String endpoint;
    private int year;
    private Session session;
    
    private InformationRetriever(String endpoint, int year, Session session) {
        this.endpoint = endpoint;
        this.year = year;
        this.session = session;
    }
    
    public static Builder newBuilder() {
        return new Builder();
    }
    
    public Optional<Subject> getSubjectByCode(String code) {
        return getSubjects().stream()
                .filter(s -> s.getCode().trim().equals(code.trim()))
                .findFirst();
    }
    
    public Optional<Subject> getSubjectByName(String name) {
        return getSubjects().stream()
                .filter(s -> s.getName().trim().equals(name.trim()))
                .findFirst();
    }
    
    public List<Subject> getSubjects() {
        List<Subject> subjects = new ArrayList<>();
        String response = HttpClient.newBuilder().build()
                .sendAsync(HttpRequest.newBuilder()
                        .uri(URI.create(endpoint + "/cs/courseschedule?" +
                                "sessyr=" + year + "&" +
                                "sesscd=" + session.abbreviated() + "&" +
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
            subjects.add(Subject.newBuilder()
                    .code(subjectMatcher.group("code").trim())
                    .name(subjectMatcher.group("name").trim())
                    .faculty(subjectMatcher.group("faculty").trim())
                    .url("https://courses.students.ubc.ca" +
                            subjectMatcher.group("url")
                                    .replace("&amp;", "&"))
                    .build());
        }
        
        return subjects.stream().distinct().collect(Collectors.toList());
    }
    
    public int getYear() {
        return year;
    }
    
    public Session getSession() {
        return session;
    }
    
    public static class Builder {
        private String endpoint;
        private int year;
        private Session session;
        
        public Builder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }
        
        public Builder year(int year) {
            this.year = year;
            return this;
        }
        
        public Builder session(Session session) {
            this.session = session;
            return this;
        }
        
        public InformationRetriever build() {
            return new InformationRetriever(endpoint, year, session);
        }
    }
}
