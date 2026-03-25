package com.cafe.caferestaurant.utils;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utilitaire de hachage BCrypt pour les mots de passe.
 *
 * Dépendance Maven :
 *   <dependency>
 *     <groupId>org.mindrot</groupId>
 *     <artifactId>jbcrypt</artifactId>
 *     <version>0.4</version>
 *   </dependency>
 */
public class PasswordUtil {

    private static final int WORK_FACTOR = 12;

    /** Hacher un mot de passe en clair. */
    public static String hash(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(WORK_FACTOR));
    }

    /** Vérifier un mot de passe contre son hash. */
    public static boolean verify(String rawPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(rawPassword, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }

    private PasswordUtil() {}
}