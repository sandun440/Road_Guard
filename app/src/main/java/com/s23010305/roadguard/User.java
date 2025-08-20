package com.s23010305.roadguard;

public class User {
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String password;
    private String phone;


    public User(String firstName, String lastName, String email,
                String username, String password, String phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.password = password;
        this.phone = phone;
    }

    // --- Getters ---
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getPhone() { return phone; }

    // --- Setters ---
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setEmail(String email) { this.email = email; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setPhone(String phone) { this.phone = phone; }

    // --- Helpers ---
    public String getFullName() {
        return (firstName != null ? firstName : "") +
                (lastName != null && !lastName.isEmpty() ? " " + lastName : "");
    }

    public void setFullName(String fullName) {
        if (fullName != null && !fullName.trim().isEmpty()) {
            String[] parts = fullName.trim().split("\\s+", 2);
            this.firstName = parts[0];
            this.lastName = (parts.length > 1) ? parts[1] : "";
        }
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", fullName='" + getFullName() + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}
