package com.johnturkson.courses;

import java.util.Objects;

public class SeatInformation {
    private int totalSeatsRemaining;
    private int currentlyRegistered;
    private int generalSeatsRemaining;
    private int restrictedSeatsRemaining;
    private int totalSeats;
    
    private SeatInformation(int totalSeatsRemaining, int currentlyRegistered, int generalSeatsRemaining, int restrictedSeatsRemaining) {
        this.totalSeatsRemaining = totalSeatsRemaining;
        this.currentlyRegistered = currentlyRegistered;
        this.generalSeatsRemaining = generalSeatsRemaining;
        this.restrictedSeatsRemaining = restrictedSeatsRemaining;
        this.totalSeats = totalSeatsRemaining + currentlyRegistered;
    }
    
    public static Builder newBuilder() {
        return new Builder();
    }
    
    public int getTotalSeatsRemaining() {
        return totalSeatsRemaining;
    }
    
    public int getCurrentlyRegistered() {
        return currentlyRegistered;
    }
    
    public int getGeneralSeatsRemaining() {
        return generalSeatsRemaining;
    }
    
    public int getRestrictedSeatsRemaining() {
        return restrictedSeatsRemaining;
    }
    
    public int getTotalSeats() {
        return totalSeats;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SeatInformation that = (SeatInformation) o;
        return totalSeatsRemaining == that.totalSeatsRemaining &&
                currentlyRegistered == that.currentlyRegistered &&
                generalSeatsRemaining == that.generalSeatsRemaining &&
                restrictedSeatsRemaining == that.restrictedSeatsRemaining &&
                totalSeats == that.totalSeats;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(totalSeatsRemaining, currentlyRegistered, generalSeatsRemaining, restrictedSeatsRemaining, totalSeats);
    }
    
    @Override
    public String toString() {
        return "Total Seats Remaining: " + totalSeatsRemaining + " of " + totalSeats + "\n" +
                "Currently Registered: " + currentlyRegistered + " of " + totalSeats + "\n" +
                "General Seats Remaining: " + generalSeatsRemaining + " of " + totalSeats + "\n" +
                "Restricted Seats Remaining: " + restrictedSeatsRemaining + " of " + totalSeats;
    }
    
    public static class Builder {
        private int totalSeatsRemaining;
        private int currentlyRegistered;
        private int generalSeatsRemaining;
        private int restrictedSeatsRemaining;
        
        public Builder totalSeatsRemaining(int totalSeatsRemaining) {
            this.totalSeatsRemaining = totalSeatsRemaining;
            return this;
        }
        
        public Builder currentlyRegistered(int currentlyRegistered) {
            this.currentlyRegistered = currentlyRegistered;
            return this;
        }
        
        public Builder generalSeatsRemaining(int generalSeatsRemaining) {
            this.generalSeatsRemaining = generalSeatsRemaining;
            return this;
        }
        
        public Builder restrictedSeatsRemaining(int restrictedSeatsRemaining) {
            this.restrictedSeatsRemaining = restrictedSeatsRemaining;
            return this;
        }
        
        public SeatInformation build() {
            return new SeatInformation(totalSeatsRemaining, currentlyRegistered, generalSeatsRemaining, restrictedSeatsRemaining);
        }
    }
}
