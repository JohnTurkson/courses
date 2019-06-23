package com.johnturkson.courses;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Timetable {
    List<Section> sections;
    
    private Timetable(List<Section> sections) {
        this.sections = sections;
    }
    
    private Map<Integer, List<Section>> getCoursesMappedByTerm(List<Section> sections) {
        Map<Integer, List<Section>> mappedCourses = new TreeMap<>();
        
        for (Section s : sections) {
            if (mappedCourses.get(s.getTerm()) != null) {
                mappedCourses.get(s.getTerm()).add(s);
            } else {
                mappedCourses.put(s.getTerm(), List.of(s));
            }
        }
        
        return mappedCourses;
    }
    
    public static class Builder {
        private List<Section> sections;
        
        public Builder sections(List<Section> sections) {
            this.sections = sections;
            return this;
        }
        
        public Timetable build() {
            return new Timetable(sections);
        }
    }
}
