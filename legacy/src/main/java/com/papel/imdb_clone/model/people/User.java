package com.papel.imdb_clone.model.people;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.regex.Pattern;

/**
 * Represents a user of the application.
 * User implements Serializable interface to enable object serialization which means that the object can be converted into a byte stream to store it in a file or transmit it over a network.
 * This helps in saving the user object-instance to a file or transmitting it over a network or a database.
 */
public class User implements Serializable {

    //serial version uid to ensure compatibility across different versions for object serialization
    //serialization is the process of converting an object into a byte stream to store it in a file or transmit it over a network
    @Serial
    private static final long serialVersionUID = 1L;
    private static int nextId = 1;
    private int id;
    private String password; // This will store the hashed password
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private Instant lastActivity;

    /**
     * Email pattern to validate email addresses
     * The email pattern is:^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$
     * This pattern matches:
     * - One or more characters that are letters, digits, plus, underscore, dot, or hyphen
     * - An @ symbol
     * - One or more characters that are letters, digits, dot, or hyphen
     * - A dot
     * - Two or more letters
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private boolean active; // Whether the user is active
    private boolean locked; // Whether the user is locked
    private Instant createdAt; // When the user was created
    private Instant updatedAt; // When the user was last updated

    /**
     * Constructor for User
     * @param firstName The user's first name
     * @param lastName The user's last name
     * @param username The user's username
     * @param gender The user's gender
     * @param email The user's email address
     */
    public User(String firstName, String lastName, String username, char gender, String email) {
        this.id = nextId++;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        // Initialize email directly to avoid 'this' escape
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }
        this.email = email.toLowerCase();
    }

    public User() {
        this.id = nextId++;
        this.firstName = "";
        this.lastName = "";
        this.username = "";
        this.email = "";
        this.lastActivity = Instant.now();
        this.password = "";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    /**
     * Gets the timestamp of the user's last activity.
     *
     * @return The timestamp of the last activity, or null if never active
     */
    public Instant getLastActivity() {
        return lastActivity;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    //set password string with validation
    public void setPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    // Set email - must match the EMAIL_PATTERN
    public void setEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }
        this.email = email.toLowerCase();
    }


    @Override
    public String toString() {
        String base = "User{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' ;

        if (password != null) {
            base += ", password='" + password + '\'';
        }
        return base + '}';
    }

    //set join date for when the user joins the application. It loads the zone id of the system and converts it to an instant
    public void setJoinDate(LocalDate joinDate) {
        this.createdAt = joinDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    public boolean isAdmin() {
        return true;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;

    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}