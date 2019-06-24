package com.johnturkson.courses;

public enum Session {
    WINTER("W", "Winter"),
    SUMMER("S", "Summer");
    
    private String abbreviation;
    private String name;
    
    Session(String abbreviation, String name) {
        this.abbreviation = abbreviation;
        this.name = name;
    }
    
    public String abbreviated() {
        return abbreviation;
    }
    
    public String getName() {
        return name;
    }
}
