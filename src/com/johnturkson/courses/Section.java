package com.johnturkson.courses;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class Section {
    private String subject;
    private String course;
    private String code;
    private String activity;
    private int term;
    private Map<Time, Location> schedule;
    private String instructor;
    private URL url;
    
    private Section(String subject, String course, String code, String activity, int term, Map<Time, Location> schedule, String instructor, URL url) {
        this.subject = subject;
        this.course = course;
        this.code = code;
        this.activity = activity;
        this.term = term;
        this.schedule = schedule;
        this.instructor = instructor;
        this.url = url;
    }
    
    public static Builder newBuilder() {
        return new Builder();
    }
    
    public String getSubject() {
        return subject;
    }
    
    public String getCourse() {
        return course;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getActivity() {
        return activity;
    }
    
    public int getTerm() {
        return term;
    }
    
    public Map<Time, Location> getSchedule() {
        return schedule;
    }
    
    public String getInstructor() {
        return instructor;
    }
    
    public URL getUrl() {
        return url;
    }
    
    public List<Time> getTimes() {
        return new ArrayList<>(schedule.keySet());
    }
    
    public List<Location> getLocations() {
        return new ArrayList<>(schedule.values());
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Section section = (Section) o;
        return term == section.term &&
                Objects.equals(subject, section.subject) &&
                Objects.equals(course, section.course) &&
                Objects.equals(code, section.code) &&
                Objects.equals(activity, section.activity) &&
                Objects.equals(schedule, section.schedule) &&
                Objects.equals(instructor, section.instructor) &&
                Objects.equals(url, section.url);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(subject, course, code, activity, term, schedule, instructor, url);
    }
    
    @Override
    public String toString() {
        return subject + " " + course + " " + code + " (" +
                schedule.keySet().stream()
                        .map(k -> k.toString() + " [" + schedule.get(k) + "]")
                        .collect(Collectors.joining(", ")) + ")" + " - " + instructor;
    }
    
    public static class Builder {
        private String subject;
        private String course;
        private String code;
        private String activity;
        private int term;
        private Map<Time, Location> schedule;
        private String instructor;
        private URL url;
        
        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }
        
        public Builder course(String course) {
            this.course = course;
            return this;
        }
        
        public Builder code(String code) {
            this.code = code;
            return this;
        }
        
        public Builder activity(String activity) {
            this.activity = activity;
            return this;
        }
        
        public Builder term(int term) {
            this.term = term;
            return this;
        }
        
        public Builder schedule(Map<Time, Location> schedule) {
            this.schedule = schedule;
            return this;
        }
        
        public Builder instructor(String instructor) {
            this.instructor = instructor;
            return this;
        }
        
        public Builder url(URL url) {
            this.url = url;
            return this;
        }
        
        public Section build() {
            return new Section(subject, course, code, activity, term, schedule, instructor, url);
        }
    }
}
