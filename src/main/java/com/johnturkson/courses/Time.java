package com.johnturkson.courses;

import java.util.List;
import java.util.Objects;

public class Time {
    private List<String> days;
    private int startHour;
    private int startMinute;
    private int endHour;
    private int endMinute;
    
    private Time(List<String> days, int startHour, int startMinute, int endHour, int endMinute) {
        if (startHour < 0 || startHour > 23) {
            throw new IllegalArgumentException("Starting hour value must be between 0 and 24.");
        }
        
        if (endHour < 0 || endHour > 23) {
            throw new IllegalArgumentException("Ending hour value must be between 0 and 24.");
        }
        
        if (startMinute < 0 || startMinute > 59) {
            throw new IllegalArgumentException("Starting minute value must be between 0 and 60.");
        }
        
        if (endMinute < 0 || endMinute > 59) {
            throw new IllegalArgumentException("Ending minute value must be between 0 and 60.");
        }
        
        this.days = days;
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.endHour = endHour;
        this.endMinute = endMinute;
    }
    
    public static Builder newBuilder() {
        return new Builder();
    }
    
    public int getEndMinute() {
        return endMinute;
    }
    
    public List<String> getDays() {
        return days;
    }
    
    public int getStartHour() {
        return startHour;
    }
    
    public int getStartMinute() {
        return startMinute;
    }
    
    public int getEndHour() {
        return endHour;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Time time = (Time) o;
        return endMinute == time.endMinute &&
                startHour == time.startHour &&
                startMinute == time.startMinute &&
                endHour == time.endHour &&
                Objects.equals(days, time.days);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(endMinute, days, startHour, startMinute, endHour);
    }
    
    @Override
    public String toString() {
        String formatted = String.join(" ", days);
        formatted += " ";
        formatted += startHour == 0 ? 12 : startHour > 12 ? startHour - 12 : startHour;
        formatted += (startMinute < 10 ? ":" + "0" + startMinute : ":" + startMinute);
        formatted += startHour < 12 ? " AM" : " PM";
        formatted += " - ";
        formatted += endHour == 0 ? 12 : endHour > 12 ? endHour - 12 : endHour;
        formatted += (endMinute < 10 ? ":" + "0" + endMinute : ":" + endMinute);
        formatted += endHour < 12 ? " AM" : " PM";
        return formatted;
    }
    
    public static class Builder {
        private List<String> days;
        private int startHour;
        private int startMinute;
        private int endHour;
        private int endMinute;
        
        public Builder days(List<String> days) {
            this.days = days;
            return this;
        }
        
        public Builder startHour(int startHour) {
            this.startHour = startHour;
            return this;
        }
        
        public Builder startMinute(int startMinute) {
            this.startMinute = startMinute;
            return this;
        }
        
        public Builder endHour(int endHour) {
            this.endHour = endHour;
            return this;
        }
        
        public Builder endMinute(int endMinute) {
            this.endMinute = endMinute;
            return this;
        }
        
        public Time build() {
            return new Time(days, startHour, startMinute, endHour, endMinute);
        }
    }
}
