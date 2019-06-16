package com.johnturkson.courses;

import java.net.URL;
import java.util.Objects;

public class Subject {
    private String code;
    private String name;
    private String faculty;
    private URL url;
    
    private Subject(String code, String name, String faculty, URL url) {
        this.code = code;
        this.name = name;
        this.faculty = faculty;
        this.url = url;
    }
    
    public static Builder newBuilder() {
        return new Builder();
    }
    
    public String getCode() {
        return code;
    }
    
    public String getName() {
        return name;
    }
    
    public String getFaculty() {
        return faculty;
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
        Subject subject = (Subject) o;
        return Objects.equals(code, subject.code) &&
                Objects.equals(name, subject.name) &&
                Objects.equals(faculty, subject.faculty) &&
                Objects.equals(url, subject.url);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(code, name, faculty, url);
    }
    
    @Override
    public String toString() {
        return code;
    }
    
    public String toJSON() {
        return "{\n" +
                "\t\"code\": \"" + code + "\",\n" +
                "\t\"name\": \"" + name + "\",\n" +
                "\t\"faculty\": \"" + faculty
                .replace("\\", "\\\\")
                .replace("\"", "\\\"") + "\",\n" +
                "\t\"url\": \"" + url + "\"\n" +
                "}";
    }
    
    public String toCSV() {
        return "\"" + code + "\"" + "," +
                "\"" + name + "\"" + "," +
                "\"" + faculty
                .replace("\\", "\\\\")
                .replace("\"", "\\\"") + "\"" + "," +
                "\"" + url + "\"";
    }
    
    public static class Builder {
        private String code;
        private String name;
        private String faculty;
        private URL url;
        
        public Builder code(String code) {
            this.code = code;
            return this;
        }
        
        public Builder name(String code) {
            this.name = code;
            return this;
        }
        
        public Builder faculty(String faculty) {
            this.faculty = faculty;
            return this;
        }
        
        public Builder url(URL url) {
            this.url = url;
            return this;
        }
        
        public Subject build() {
            return new Subject(code, name, faculty, url);
        }
    }
}
