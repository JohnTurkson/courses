package com.johnturkson.courses;

import java.util.*;
import java.util.stream.Collectors;

public class Timetable {
    public static final int TIMETABLE_START_TIME = 8;
    public static final int TIMETABLE_END_TIME = 20;
    public static final int ROW_HEADER_WIDTH = 8;
    public static final int COLUMN_WIDTH = 18;
    private List<Section> sections;
    
    private Timetable(List<Section> sections) {
        this.sections = sections;
    }
    
    public static Builder newBuilder() {
        return new Builder();
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
    
    public List<Section> removeConflictingSections(List<Section> sections) {
        List<Section> nonConflictingSections = new ArrayList<>();
        for (Section s1 : sections) {
            boolean hasConflict = false;
            for (Section s2 : nonConflictingSections) {
                if (hasScheduleConflict(s1, s2)) {
                    hasConflict = true;
                }
            }
            if (!hasConflict) {
                nonConflictingSections.add(s1);
            }
        }
        return nonConflictingSections;
    }
    
    public boolean hasScheduleConflict(Section s1, Section s2) {
        for (Time t1 : s1.getSchedule().keySet()) {
            for (Time t2 : s2.getSchedule().keySet()) {
                if (t1.getDays().stream().noneMatch(d -> t2.getDays().contains(d))) {
                    continue;
                }
                if (isOverlapping(t1, t2)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public boolean isOverlapping(Time t1, Time t2) {
        int t1StartTime = t1.getStartHour() * 60 + t1.getStartMinute();
        int t1EndTime = t1.getEndHour() * 60 + t1.getEndMinute();
        int t2StartTime = t2.getStartHour() * 60 + t2.getStartMinute();
        int t2EndTime = t2.getEndHour() * 60 + t2.getEndMinute();
        
        return (t1StartTime >= t2StartTime && t1StartTime < t2EndTime) ||
                (t1EndTime <= t2EndTime && t1EndTime > t2StartTime);
    }
    
    public String generateColumns(List<Section> sections) {
        sections = removeConflictingSections(sections);
        
        String columns = padRight(generateRowHeaders(), ROW_HEADER_WIDTH);
        
        for (String day : List.of("Mon", "Tue", "Wed", "Thu", "Fri")) {
            String column = generateBlankColumn();
            for (Section section : sections) {
                for (Time time : section.getSchedule().keySet()) {
                    if (time.getDays().contains(day)) {
                        column = drawBox(section, time, column);
                    }
                }
            }
            columns = combineColumns(columns, column);
            
        }
        return columns;
    }
    
    public String drawBox(Section section, Time time, String column) {
        int startRow = (time.getStartHour() - TIMETABLE_START_TIME) * 4 + time.getStartMinute() / 15;
        int endRow = (time.getEndHour() - TIMETABLE_START_TIME) * 4 + time.getEndMinute() / 15;
        
        List<String> lines = Arrays.stream(column.split("\n")).collect(Collectors.toList());
        
        // Merge boxes
        if (lines.get(startRow).equals("+" + "-".repeat(COLUMN_WIDTH - 2) + "+")) {
            lines.set(startRow, "|" + "-".repeat(COLUMN_WIDTH - 2) + "|");
        } else {
            lines.set(startRow, "+" + "-".repeat(COLUMN_WIDTH - 2) + "+");
        }
        
        if (lines.get(endRow).equals("+" + "-".repeat(COLUMN_WIDTH - 2) + "+")) {
            lines.set(endRow, "|" + "-".repeat(COLUMN_WIDTH - 2) + "|");
        } else {
            lines.set(endRow, "+" + "-".repeat(COLUMN_WIDTH - 2) + "+");
        }
        
        // Draw left and right outline
        for (int i = startRow + 1; i <= endRow - 1; i++) {
            lines.set(i, "|" + " ".repeat(COLUMN_WIDTH - 2) + "|");
        }
        
        lines.set(startRow + (endRow - startRow) / 2, "|" +
                center(section.getSubject() + " " + section.getCourse() + " " +
                        section.getCode(), COLUMN_WIDTH - 2) + "|");
        lines.set(startRow + (endRow - startRow) / 2 + 1, "|" +
                center(section.getInstructor(), COLUMN_WIDTH - 2) + "|");
        
        return String.join("\n", lines);
    }
    
    public String combineColumns(String c1, String c2) {
        String[] c1Lines = c1.split("\n");
        String[] c2Lines = c2.split("\n");
        String combined = "";
        for (int i = 0; i < c1Lines.length; i++) {
            combined += c1Lines[i] + c2Lines[i] + "\n";
        }
        return combined;
    }
    
    public String generateBlankColumn() {
        return (" ".repeat(COLUMN_WIDTH) + "\n").repeat((TIMETABLE_END_TIME - TIMETABLE_START_TIME + 1) * 4);
    }
    
    public String generateRowHeaders() {
        String rowHeaders = "";
        for (int i = TIMETABLE_START_TIME; i <= TIMETABLE_END_TIME; i++) {
            rowHeaders += i > 12 ? (i - 12) + "00" + "\n\n" + (i - 12) + "30" + "\n\n" :
                    i + "00" + "\n\n" + i + "30" + "\n\n";
        }
        return rowHeaders;
    }
    
    private String trimToWidth(String column, int desiredWidth) {
        String trimmed = "";
        for (String line : column.lines().collect(Collectors.toList())) {
            if (line.length() > desiredWidth) {
                trimmed += line;
            } else {
                trimmed += line.substring(0, desiredWidth) + "\n";
            }
        }
        return trimmed;
    }
    
    public String center(String text, int desiredWidth) {
        if (text.length() > desiredWidth) {
            return text;
        } else {
            return " ".repeat((desiredWidth - text.length()) / 2) + text +
                    " ".repeat(desiredWidth - text.length() - ((desiredWidth - text.length()) / 2));
        }
    }
    
    public String padRight(String column, int desiredWidth) {
        String padded = "";
        for (String line : column.lines().collect(Collectors.toList())) {
            padded += line;
            if (line.length() < desiredWidth) {
                padded += " ".repeat(desiredWidth - line.length());
            }
            padded += "\n";
        }
        return padded;
    }
    
    public String padLeft(String column, int desiredWidth) {
        String padded = "";
        for (String line : column.split("\n")) {
            if (line.length() < desiredWidth) {
                padded += " ".repeat(desiredWidth - line.length());
            }
            padded += line;
            padded += "\n";
        }
        return padded;
    }
    
    @Override
    public String toString() {
        return null;
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
