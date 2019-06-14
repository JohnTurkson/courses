package com.johnturkson.courses;

public class Main {
    public static void main(String... args) {
        Subjects.getSubjects().forEach(s -> System.out.println(s.getCode() + ": " + s.getName() + " - " + s.getFaculty()));
        Courses.getCourses("CPSC").forEach(System.out::println);
        Sections.getSections("CPSC", "213").forEach(System.out::println);
    }
}
