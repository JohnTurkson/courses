package com.johnturkson.courses;

import java.net.URL;
import java.util.Objects;
import java.util.regex.Matcher;

public class Course {
    private String subject;
    private String code;
    private String name;
    private String description;
    private int credits;
    private URL url;
    
    private Course(String subject, String code, String name, String description, int credits, URL url) {
        this.subject = subject;
        this.code = code;
        this.name = name;
        this.description = description;
        this.credits = credits;
        this.url = url;
    }
    
    public static Builder newBuilder() {
        return new Builder();
    }
    
    public String getSubject() {
        return subject;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getCredits() {
        return credits;
    }
    
    public URL getUrl() {
        return url;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Course course = (Course) o;
        return credits == course.credits &&
                Objects.equals(subject, course.subject) &&
                Objects.equals(code, course.code) &&
                Objects.equals(name, course.name) &&
                Objects.equals(description, course.description) &&
                Objects.equals(url, course.url);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(subject, code, name, description, credits, url);
    }
    
    @Override
    public String toString() {
        return subject + " " + code + ": " + name;
    }
    
    public String toJSON() {
        return "{\n" +
                "\t\"subject\": \"" + subject + "\",\n" +
                "\t\"code\": \"" + code + "\",\n" +
                "\t\"name\": \"" + name + "\",\n" +
                "\t\"description\": \"" + description
                .replace("\\", "\\\\")
                .replace("\"", "\\\"") + "\",\n" +
                "\t\"credits\": " + credits + ",\n" +
                "\t\"url\": \"" + url + "\"\n" +
                "}";
    }
    
    public String toCSV() {
        return "\"" + subject + "\"," +
                "\"" + code + "\"," +
                "\"" + name + "\"," +
                "\"" + description
                .replace("\\", "\\\\")
                .replace("\"", "\\\"") + "\"," +
                "\"" + credits + "\"," +
                "\"" + url + "\"";
    }
    
    public static class Builder {
        private String subject;
        private String code;
        private String name;
        private String description;
        private int credits;
        private URL url;
        
        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }
        
        public Builder code(String code) {
            this.code = code;
            return this;
        }
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder credits(int credits) {
            this.credits = credits;
            return this;
        }
        
        public Builder url(URL url) {
            this.url = url;
            return this;
        }
        
        public Course build() {
            return new Course(subject, code, name, description, credits, url);
        }
    }
}
