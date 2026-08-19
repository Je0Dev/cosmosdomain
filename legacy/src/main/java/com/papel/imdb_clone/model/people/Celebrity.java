package com.papel.imdb_clone.model.people;

import com.papel.imdb_clone.enums.Ethnicity;
import java.time.LocalDate;
import java.util.Objects;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Celebrity class is an abstract class that represents a celebrity.
 * Implements proper equality checks and unique ID generation.
 */
public abstract class Celebrity {

    private static int nextId = 1; //unique id for each celebrity
    protected int id; //unique id for each celebrity
    protected String firstName; //first name of the celebrity
    protected String lastName; //last name of the celebrity
    protected LocalDate birthDate; //birth date of the celebrity
    protected char gender; //gender of the celebrity
    private Ethnicity ethnicity; //ethnicity of the celebrity
    protected List<String> notableWorks = new ArrayList<>(); //notable works of the celebrity

    /**
     * Celebrity constructor with required fields.
     * @param firstName First name of the celebrity
     * @param lastName Last name of the celebrity
     * @param birthDate Birth date (can be null)
     * @param gender Gender (M/F/other)
     */
    protected Celebrity(String firstName, String lastName, LocalDate birthDate, char gender) {
        this.id = nextId++;
        this.firstName = firstName != null ? firstName.trim() : "";
        this.lastName = lastName != null ? lastName.trim() : "";
        this.birthDate = birthDate;
        this.gender = gender;
    }


    public Celebrity(String actorName) {
        this(actorName, "", null, 'U');
        this.id = nextId++;
        this.firstName = actorName;
    }

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public char getGender() {
        return gender;
    }

    public String getFullName() {
        return String.format("%s %s", firstName, lastName).trim();
    }

    public Ethnicity getEthnicity() {
        return ethnicity;
    }

    public void setEthnicity(Ethnicity ethnicity) {
        this.ethnicity = ethnicity;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName != null ? firstName.trim() : "";
    }

    public void setLastName(String lastName) {
        this.lastName = lastName != null ? lastName.trim() : "";
    }

    public void setGender(char gender) {
        this.gender = gender;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Celebrity celebrity = (Celebrity) o;
        return gender == celebrity.gender &&
               Objects.equals(firstName.toLowerCase(), celebrity.firstName.toLowerCase()) &&
               Objects.equals(lastName.toLowerCase(), celebrity.lastName.toLowerCase()) &&
               Objects.equals(birthDate, celebrity.birthDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName.toLowerCase(), lastName.toLowerCase(), birthDate, gender);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", birthDate=" + birthDate +
                ", gender=" + gender +
                ", ethnicity=" + ethnicity +
                '}';
    }

    public Ethnicity getNationality() {
        return ethnicity;
    }
    
    /**
     * Gets the list of notable works for this celebrity.
     * @return List of notable works
     */
    public List<String> getNotableWorks() {
        return new ArrayList<>(notableWorks);
    }
    
    /**
     * Sets the notable works for this celebrity.
     * @param works List of notable works
     */
    public void setNotableWorks(List<String> works) {
        if (works != null) {
            this.notableWorks = new ArrayList<>(works);
        } else {
            this.notableWorks = new ArrayList<>();
        }
    }

    /**
     * Sets the ID for this celebrity.
     * @param andIncrement The ID to set
     */
    public void setId(int andIncrement) {
        this.id = andIncrement;
    }
    
    /**
     * Sets the notable works from a comma-separated string.
     * @param worksString Comma-separated string of notable works
     */
    public void setNotableWorks(String worksString) {
        if (worksString != null && !worksString.trim().isEmpty()) {
            // First, clean up the string by removing any surrounding quotes and extra spaces
            String cleaned = worksString.trim()
                .replaceAll("^[\"']|[\"']$", "")  // Remove surrounding quotes
                .replace("\"", "")  // Remove any remaining quotes
                .replace("  ", " ")   // Replace double spaces with single space
                .trim();
            
            // Handle different delimiters (comma, semicolon, or newline)
            this.notableWorks = Arrays.stream(cleaned.split("[,;\n]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
            
            // If still no works found, try splitting by spaces as a last resort
            if (this.notableWorks.isEmpty() && cleaned.contains(" ")) {
                this.notableWorks = Arrays.stream(cleaned.split("\\s+"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            }
            
            // Log the parsed works for debugging
            if (!this.notableWorks.isEmpty()) {
                System.out.println("Parsed notable works for " + getFullName() + ": " + String.join(", ", this.notableWorks));
            }
        } else {
            this.notableWorks = new ArrayList<>();
        }
    }
    
    /**
     * Adds a notable work to the list if it doesn't already exist.
     * @param work The notable work to add
     */
    public void addNotableWork(String work) {
        if (work != null && !work.trim().isEmpty() && !notableWorks.contains(work.trim())) {
            notableWorks.add(work.trim());
        }
    }
}