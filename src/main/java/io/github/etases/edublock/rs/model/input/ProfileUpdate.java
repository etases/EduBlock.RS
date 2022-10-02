package io.github.etases.edublock.rs.model.input;

import java.util.Date;

public record ProfileUpdate(String firstName, String lastName, boolean male, String avatar, Date birthDate, String address,
                            String phone, String email) {
    public boolean validate() {
        boolean isValidName = firstName != null && !firstName.isBlank() && lastName != null && !lastName.isBlank();
        boolean isValidDate = birthDate != null;
        boolean isValidPhone = phone != null && (phone.length() == 10 || phone.length() == 0);
        boolean isValidEmail = email != null && (email.contains("@") || email().length() == 0);

        return isValidName && isValidDate && isValidPhone && isValidEmail;
    }
}
