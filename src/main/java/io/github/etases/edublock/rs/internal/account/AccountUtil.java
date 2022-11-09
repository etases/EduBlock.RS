package io.github.etases.edublock.rs.internal.account;

import io.github.etases.edublock.rs.entity.Account;
import lombok.experimental.UtilityClass;
import org.hibernate.Session;

import java.sql.Date;

@UtilityClass
public final class AccountUtil {
    public static Account createAccount(Session session, String initialUsername, String password) {
        long count = session.createNamedQuery("Account.countByUsernameRegex", Long.class)
                .setParameter("username", initialUsername + "%")
                .uniqueResult();
        String salt = PasswordUtil.generateSalt();
        String hashedPassword = PasswordUtil.hashPassword(password, salt);
        var account = new Account();
        account.setUsername(initialUsername + (count == 0 ? "" : count));
        account.setSalt(salt);
        account.setHashedPassword(hashedPassword);
        account.setCreatedAt(new Date(System.currentTimeMillis()));
        return account;
    }

    public static String generateUsername(String firstName, String lastName) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < firstName.length(); i++) {
            char c = firstName.charAt(i);
            if (Character.isLetter(c)) {
                if (i == 0) {
                    sb.append(Character.toUpperCase(c));
                } else {
                    sb.append(Character.toLowerCase(c));
                }
            }
        }
        for (char c : lastName.toCharArray()) {
            if (Character.isLetter(c) && Character.isUpperCase(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
