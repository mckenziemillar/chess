package model;

import java.util.Objects;

public record UserData(String username, String password, String email) {
    public UserData {
        // You can add validation logic here if needed
        if (username == null || password == null || email == null) {
            throw new IllegalArgumentException("Fields cannot be null");
        }
    }

    @Override
    public String username() {
        return username;
    }
    @Override
    public String password() {
        return password;
    }
    @Override
    public String email() {
        return email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserData userData = (UserData) o;
        return Objects.equals(username, userData.username) && Objects.equals(password, userData.password) && Objects.equals(email, userData.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, password, email);
    }

    @Override
    public String toString() {
        return "UserData{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
