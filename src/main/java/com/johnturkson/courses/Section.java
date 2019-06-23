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
    private String status;
    private SeatInformation seats;
    private URL url;
    
    private Section(String subject, String course, String code, String activity, int term, Map<Time, Location> schedule, String instructor, String status, SeatInformation seats, URL url) {
        this.subject = subject;
        this.course = course;
        this.code = code;
        this.activity = activity;
        this.term = term;
        this.schedule = schedule;
        this.instructor = instructor;
        this.status = status;
        this.seats = seats;
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
    
    public String getStatus() {
        return status;
    }
    
    public SeatInformation getSeats() {
        return seats;
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
                Objects.equals(url, section.url);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(subject, course, code, activity, term, url);
    }
    
    @Override
    public String toString() {
        return (status.equals("") ? status : "[" + status + "] ") +
                subject + " " + course + " " + code + " (" +
                schedule.keySet().stream()
                        .map(k -> k.toString() + " [" + schedule.get(k) + "]")
                        .collect(Collectors.joining(", ")) + ")" + " - " +
                instructor + ", " + seats.getTotalSeatsRemaining() + " of " + seats.getTotalSeats() + " seats remaining";
    }
    
    // TODO add seats to json and csv import/export
    public String toJSON() {
        return "{\n" +
                "\t\"subject\": \"" + subject + "\",\n" +
                "\t\"course\": \"" + course + "\",\n" +
                "\t\"code\": \"" + code + "\",\n" +
                "\t\"activity\": \"" + activity + "\",\n" +
                "\t\"term\": " + term + ",\n" +
                "\t\"schedule\": " + schedule.keySet()
                .stream()
                .map(k -> "\t\t{\n" +
                        "\t\t\t\"days\": \"" + String.join(" ", k.getDays()) + "\",\n" +
                        "\t\t\t\"startHour\": " + k.getStartHour() + ",\n" +
                        "\t\t\t\"startMinute\": " + k.getStartMinute() + ",\n" +
                        "\t\t\t\"endHour\": " + k.getEndHour() + ",\n" +
                        "\t\t\t\"endMinute\": " + k.getEndMinute() + ",\n" +
                        "\t\t\t\"name\": \"" + schedule.get(k).getName() + "\",\n" +
                        "\t\t\t\"code\": \"" + schedule.get(k).getCode() + "\",\n" +
                        "\t\t\t\"room\": \"" + schedule.get(k).getRoom() + "\"\n" +
                        "\t\t}")
                .collect(Collectors.joining(",\n", "[\n", "\n\t]")) + ",\n" +
                "\t\"instructor\": \"" + instructor + "\",\n" +
                "\t\"seats\": {\n" +
                "\t\t\"totalSeatsRemaining\": " + seats.getTotalSeatsRemaining() + ",\n" +
                "\t\t\"currentlyRegistered\": " + seats.getCurrentlyRegistered() + ",\n" +
                "\t\t\"generalSeatsRemaining\": " + seats.getTotalSeatsRemaining() + ",\n" +
                "\t\t\"restrictedSeatsRemaining\": " + seats.getTotalSeatsRemaining() + "\n" +
                "\t}\n" +
                "\t\"url\": \"" + url + "\"\n" +
                "}";
    }
    
    public String toCSV() {
        return "\"" + subject + "\"," +
                "\"" + course + "\"," +
                "\"" + code + "\"," +
                "\"" + activity + "\"," +
                "\"" + term + "\"," +
                "\"" + schedule.keySet().stream()
                .map(k -> String.join(" ", k.getDays()) + "," +
                        k.getStartHour() + "," +
                        k.getStartMinute() + "," +
                        k.getEndHour() + "," +
                        k.getEndMinute() + "," +
                        schedule.get(k).getName() + "," +
                        schedule.get(k).getCode() + "," +
                        schedule.get(k).getRoom())
                .collect(Collectors.joining(",")) + "\"," +
                "\"" + instructor + "\"," +
                "\"" + seats.getTotalSeatsRemaining() + " " +
                seats.getCurrentlyRegistered() + " " +
                seats.getGeneralSeatsRemaining() + " " +
                seats.getRestrictedSeatsRemaining() + "\"" +
                "\"" + url + "\"";
    }
    
    public static class Builder {
        private String subject;
        private String course;
        private String code;
        private String activity;
        private int term;
        private Map<Time, Location> schedule;
        private String instructor;
        private String status;
        private SeatInformation seats;
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
        
        public Builder status(String status) {
            this.status = status;
            return this;
        }
        
        public Builder seats(SeatInformation seats) {
            this.seats = seats;
            return this;
        }
        
        public Builder url(URL url) {
            this.url = url;
            return this;
        }
        
        public Section build() {
            return new Section(subject, course, code, activity, term, schedule, instructor, status, seats, url);
        }
    }
}
