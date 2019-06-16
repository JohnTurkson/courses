package com.johnturkson.courses;

import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;

public class Main {
    public static void main(String... args) {
        Instant start = Instant.now();
        List<Subject> subjects = Subjects.importFromJSON(Paths.get("./src/main/resources/subjects.json"));
        for (Subject s : subjects) {
            List<Course> courses = Courses.importFromJSON(Paths.get("./src/main/resources/" + s.getCode() + ".json"));
            for (Course c : courses) {
                List<Section> sections = Sections.importFromJSON(Paths.get("./src/main/resources/" + c.getSubject() + c.getCode() + ".json"));
                
                sections.stream()
                        .filter(e -> e.getSubject().equals("CPSC"))
                        .filter(e -> e.getActivity().equals("Lecture"))
                        .filter(e -> e.getTerm() == 1)
                        .forEach(System.out::println);
            }
        }
        Instant end = Instant.now();
        System.out.println("Elapsed: ~" + (end.toEpochMilli() - start.toEpochMilli()) + "ms");
    }
}
