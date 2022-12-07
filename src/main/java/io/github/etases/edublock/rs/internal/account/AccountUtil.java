package io.github.etases.edublock.rs.internal.account;

import io.github.etases.edublock.rs.entity.Account;
import lombok.experimental.UtilityClass;
import org.hibernate.Session;
import org.tinylog.Logger;

import java.sql.Date;
import java.text.Normalizer;
import java.util.regex.Pattern;

@UtilityClass
public final class AccountUtil {
    private static final Pattern normalizePattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

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
        String username = sb.toString();
        username = unaccent(username);
        return username;
    }

    private static String unaccent(String str) {
        str = str.replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a");
        str = str.replaceAll("[èéẹẻẽêềếệểễ]", "e");
        str = str.replaceAll("[ìíịỉĩ]", "i");
        str = str.replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o");
        str = str.replaceAll("[ùúụủũưừứựửữ]", "u");
        str = str.replaceAll("[ỳýỵỷỹ]", "y");
        str = str.replace("đ", "d");

        str = str.replaceAll("[ÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴ]", "A");
        str = str.replaceAll("[ÈÉẸẺẼÊỀẾỆỂỄ]", "E");
        str = str.replaceAll("[ÌÍỊỈĨ]", "I");
        str = str.replaceAll("[ÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠ]", "O");
        str = str.replaceAll("[ÙÚỤỦŨƯỪỨỰỬỮ]", "U");
        str = str.replaceAll("[ỲÝỴỶỸ]", "Y");
        str = str.replace("Đ", "D");

        try {
            String temp = Normalizer.normalize(str, Normalizer.Form.NFD);
            return normalizePattern.matcher(temp).replaceAll("").replace(" ", "-");
        } catch (Exception ex) {
            Logger.error(ex, "Error when unaccenting string");
            return str;
        }
    }

    public static boolean containsUnaccent(String text, String string) {
        return unaccent(text).toLowerCase().contains(unaccent(string).toLowerCase());
    }
}
