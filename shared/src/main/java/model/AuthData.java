package model;

import java.util.Objects;

public record AuthData(String authToken, String username) {
    public AuthData{
        if(authToken == null || username == null){
            throw new IllegalArgumentException("Fields cannot be null");
        }
    }

    @Override
    public String toString() {
        return "AuthData{" +
                "authToken='" + authToken + '\'' +
                ", username='" + username + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AuthData authData = (AuthData) o;
        return Objects.equals(authToken, authData.authToken) && Objects.equals(username, authData.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(authToken, username);
    }

    @Override
    public String authToken() {
        return authToken;
    }

    @Override
    public String username() {
        return username;
    }
}
