package io.github.etases.edublock.rs.model.input;

public record AccountLogin(String username, String password) {
    public boolean validate() {
        return username != null && !username.isBlank()
                && password != null && !password.isBlank();
    }
}
