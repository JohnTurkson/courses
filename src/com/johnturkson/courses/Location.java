package com.johnturkson.courses;

import java.util.Objects;

public class Location {
    private String name;
    private String code;
    private String room;
    
    private Location(String name, String code, String room) {
        this.name = name;
        this.code = code;
        this.room = room;
    }
    
    public static Location of(String name, String code, String room) {
        return new Location(name, code, room);
    }
    
    public static Builder newBuilder() {
        return new Builder();
    }
    
    public String getName() {
        return name;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getRoom() {
        return room;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Location location = (Location) o;
        return Objects.equals(name, location.name) &&
                Objects.equals(code, location.code) &&
                Objects.equals(room, location.room);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, code, room);
    }
    
    @Override
    public String toString() {
        return (code + " " + room).trim();
    }
    
    public static class Builder {
        private String name;
        private String code;
        private String room;
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder code(String code) {
            this.code = code;
            return this;
        }
        
        public Builder room(String room) {
            this.room = room;
            return this;
        }
        
        public Location build() {
            return new Location(name, code, room);
        }
    }
}
