package com.johnturkson.courses;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SectionTracker {
    private List<Section> trackedSections;
    private Map<LocalDateTime, Map<Section, List<Change>>> pastChanges;
    private ScheduledExecutorService updater;
    private boolean trackingPaused;
    private boolean printOnUpdate;
    private boolean exportOnUpdate;
    private Path exportPath;
    
    private SectionTracker(List<Section> trackedSections) {
        this.trackedSections = trackedSections.stream().distinct().collect(Collectors.toList());
        this.pastChanges = new LinkedHashMap<>();
        this.updater = Executors.newScheduledThreadPool(10);
        this.trackingPaused = false;
        this.printOnUpdate = false;
    }
    
    public static Builder newBuilder() {
        return new Builder();
    }
    
    public List<Section> getTrackedSections() {
        return trackedSections;
    }
    
    public Map<LocalDateTime, Map<Section, List<Change>>> getPastChanges() {
        return pastChanges;
    }
    
    public void clearPastChanges() {
        pastChanges.clear();
    }
    
    public void printUpdates(boolean isEnabled) {
        printOnUpdate = isEnabled;
    }
    
    public void exportUpdates(boolean isEnabled) {
        exportOnUpdate = isEnabled;
    }
    
    public void exportTo(Path exportPath) {
        this.exportPath = exportPath;
    }
    
    public void startTracking(Duration interval) {
        startTracking(interval, Duration.ZERO);
    }
    
    public void startTracking(Duration interval, Duration initialDelay) {
        if (interval.toSeconds() == 0) {
            throw new IllegalArgumentException("Interval duration must be at least one second.");
        }
        
        trackingPaused = false;
        updater.scheduleWithFixedDelay(this::update,
                initialDelay.toSeconds(),
                interval.toSeconds(),
                TimeUnit.SECONDS);
    }
    
    public void stopTracking() {
        trackingPaused = true;
    }
    
    public void update() {
        if (!trackingPaused) {
            Map<Section, List<Change>> updates = checkForUpdates();
            if (printOnUpdate) {
                if (!updates.values().isEmpty()) {
                    updates.values().forEach(c -> c.forEach(System.out::println));
                } else {
                    System.out.println("[" + LocalDateTime.now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)) + "]" + " No changes.");
                    try {
                        Files.writeString(exportPath, "[" + LocalDateTime.now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)) + "]" + " No changes.\n", StandardOpenOption.APPEND);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (exportOnUpdate) {
                if (exportPath != null) {
                    for (List<Change> changes : updates.values()) {
                        for (Change c : changes) {
                            try {
                                Files.writeString(exportPath,
                                        c.toString() + "\n",
                                        StandardOpenOption.APPEND);
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        }
                    }
                } else {
                    throw new IllegalStateException("Export path is not set.");
                }
            }
        }
    }
    
    public Map<Section, List<Change>> checkForUpdates() {
        LocalDateTime time = LocalDateTime.now();
        Map<Section, List<Change>> updates = new LinkedHashMap<>();
        Map<Section, CompletableFuture<Optional<Section>>> futures = new LinkedHashMap<>();
        
        for (Section section : trackedSections) {
            futures.put(section, CompletableFuture.supplyAsync(() -> Sections.getSection(
                    section.getSubject(),
                    section.getCourse(),
                    section.getCode())));
        }
        
        for (Section section : futures.keySet()) {
            Optional<Section> updated = futures.get(section)
                    .exceptionally(e -> Optional.empty())
                    .join();
            if (updated.isPresent()) {
                List<Change> changes = getChangesBetweenSections(section, updated.get());
                if (!changes.isEmpty()) {
                    updates.put(updated.get(), changes);
                }
            }
        }
        
        if (!updates.keySet().isEmpty()) {
            pastChanges.put(time, updates);
        }
        
        // Update tracked sections
        trackedSections = trackedSections.stream()
                .filter(s -> !updates.keySet().contains(s))
                .collect(Collectors.toList());
        trackedSections.addAll(updates.keySet());
        
        return updates;
    }
    
    public List<Change> getChangesBetweenSections(Section oldSection, Section newSection) {
        List<Change> changes = new ArrayList<>();
        if (!oldSection.getInstructor().equals(newSection.getInstructor())) {
            changes.add(Change.newBuilder()
                    .time(LocalDateTime.now())
                    .section(oldSection)
                    .field("Instructor")
                    .oldValue(oldSection.getInstructor())
                    .newValue(newSection.getInstructor())
                    .build());
        }
        
        if (oldSection.getSeats().getTotalSeatsRemaining() != newSection.getSeats().getTotalSeatsRemaining()) {
            changes.add(Change.newBuilder()
                    .time(LocalDateTime.now())
                    .section(oldSection)
                    .field("Total Seats Remaining")
                    .oldValue(Integer.toString(oldSection.getSeats().getTotalSeatsRemaining()))
                    .newValue(Integer.toString(newSection.getSeats().getTotalSeatsRemaining()))
                    .build());
        }
        
        if (oldSection.getSeats().getCurrentlyRegistered() != newSection.getSeats().getCurrentlyRegistered()) {
            changes.add(Change.newBuilder()
                    .time(LocalDateTime.now())
                    .section(oldSection)
                    .field("Currently Registered")
                    .oldValue(Integer.toString(oldSection.getSeats().getCurrentlyRegistered()))
                    .newValue(Integer.toString(newSection.getSeats().getCurrentlyRegistered()))
                    .build());
        }
        
        if (oldSection.getSeats().getGeneralSeatsRemaining() != newSection.getSeats().getGeneralSeatsRemaining()) {
            changes.add(Change.newBuilder()
                    .time(LocalDateTime.now())
                    .section(oldSection)
                    .field("General Seats Remaining")
                    .oldValue(Integer.toString(oldSection.getSeats().getGeneralSeatsRemaining()))
                    .newValue(Integer.toString(newSection.getSeats().getGeneralSeatsRemaining()))
                    .build());
        }
        
        if (oldSection.getSeats().getRestrictedSeatsRemaining() != newSection.getSeats().getRestrictedSeatsRemaining()) {
            changes.add(Change.newBuilder()
                    .time(LocalDateTime.now())
                    .section(oldSection)
                    .field("Restricted Seats Remaining")
                    .oldValue(Integer.toString(oldSection.getSeats().getRestrictedSeatsRemaining()))
                    .newValue(Integer.toString(newSection.getSeats().getRestrictedSeatsRemaining()))
                    .build());
        }
        return changes;
    }
    
    public static class Builder {
        private List<Section> trackedSections;
        
        public Builder trackedSections(List<Section> trackedSections) {
            this.trackedSections = trackedSections;
            return this;
        }
        
        public SectionTracker build() {
            return new SectionTracker(trackedSections);
        }
    }
    
    public static class Change {
        private LocalDateTime time;
        private Section section;
        private String field;
        private String oldValue;
        private String newValue;
        
        private Change(LocalDateTime time, Section section, String field, String oldValue, String newValue) {
            this.time = time;
            this.section = section;
            this.field = field;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }
        
        public static Builder newBuilder() {
            return new Builder();
        }
        
        public LocalDateTime getTime() {
            return time;
        }
        
        public Section getSection() {
            return section;
        }
        
        public String getField() {
            return field;
        }
        
        public String getOldValue() {
            return oldValue;
        }
        
        public String getNewValue() {
            return newValue;
        }
        
        @Override
        public String toString() {
            return "[" + time.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)) + "] " +
                    section.getSubject() + " " + section.getCourse() + " " + section.getCode() + " - " +
                    field + ": " + oldValue + " -> " + newValue;
        }
        
        public static class Builder {
            private LocalDateTime time;
            private Section section;
            private String field;
            private String oldValue;
            private String newValue;
            
            public Builder time(LocalDateTime time) {
                this.time = time;
                return this;
            }
            
            public Builder section(Section section) {
                this.section = section;
                return this;
            }
            
            public Builder field(String field) {
                this.field = field;
                return this;
            }
            
            public Builder oldValue(String oldValue) {
                this.oldValue = oldValue;
                return this;
            }
            
            public Builder newValue(String newValue) {
                this.newValue = newValue;
                return this;
            }
            
            public Change build() {
                return new Change(time, section, field, oldValue, newValue);
            }
        }
    }
}
