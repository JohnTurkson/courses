package com.johnturkson.courses;

import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.TimerTask;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main {
    public static void main(String... args) {
        // InformationRetriever retriever = InformationRetriever.newBuilder()
        //         .endpoint("https://courses.students.ubc.ca")
        //         .year(2019)
        //         .session(Session.WINTER)
        //         .build();
    
        Timetable t = Timetable.newBuilder().build();
        List<Section> cpsc210Labs = Sections.getSections("CPSC", "213").stream()
                .filter(s -> s.getActivity().matches("Lecture"))
                .collect(Collectors.toList());
        
        System.out.println(t.generateColumns(cpsc210Labs));
    
        // Instant start = Instant.now();
        // List<Section> cpsc313 = Sections.getSections("MATH", "221").stream()
        //         .filter(s -> s.getActivity().matches("Lecture"))
        //         .collect(Collectors.toList());
        // cpsc313.forEach(s -> System.out.println(s + "\n" + s.getSeats() + "\n"));
        // Instant end = Instant.now();
        // System.out.println(end.toEpochMilli() - start.toEpochMilli());
        
        // List<Subject> subjects = Subjects.importFromJSON(Paths.get("./src/main/resources/subjects.json"));
        // List<Course> courses = Courses.importFromJSON(Paths.get("./src/main/resources/CPSC.json"));
        // courses = courses.stream().filter(c -> c.getCode().matches("\\d+")).collect(Collectors.toList());
        // List<CompletableFuture<HttpResponse<String>>> futures = Collections.synchronizedList(new ArrayList<>());
        // List<String> urls = Collections.synchronizedList(new ArrayList<>());
        //
        // futures = courses.stream().map(c -> {
        //     System.out.println(c);
        //    
        //     String query = "https://courses.students.ubc.ca/cs/courseschedule?" +
        //             "pname=subjarea&" +
        //             "tname=subj-course&" +
        //             "dept=" + c.getSubject().trim() + "&" +
        //             "course=" + c.getCode().trim();
        //         return HttpClient.newHttpClient()
        //                 .sendAsync(HttpRequest.newBuilder()
        //                         .uri(URI.create(query))
        //                         .GET()
        //                         .build(), HttpResponse.BodyHandlers.ofString());
        //                 // .thenAccept(f -> urls.add(f.uri().toString()));
        //    
        // }).collect(Collectors.toList());
        //
        // futures.forEach(r -> r.thenAccept(f -> urls.add(f.uri().toString())).join());
        //
        // urls.forEach(System.out::println);
        //
        
        
        // List<Subject> subjects = Subjects.importFromJSON(Paths.get("./src/main/resources/subjects.json"));
        // for (Subject s : subjects) {
        //     List<Course> courses = Courses.importFromJSON(Paths.get("./src/main/resources/" + s.getCode() + ".json"));
        //     for (Course c : courses) {
        //         List<Section> sections = Sections.importFromJSON(Paths.get("./src/main/resources/" + c.getSubject() + c.getCode() + ".json"));
        //
        //         sections.stream()
        //                 .filter(e -> e.getSubject().equals("BIOL"))
        //                 .filter(e -> e.getCourse().matches("111"))
        //                 .filter(e -> e.getActivity().matches("Lecture|Laboratory|Tutorial"))
        //                 .filter(e -> e.getTerm() == 2)
        //                 .forEach(System.out::println);
        //        
        //     }
        // }
    
    }
}
