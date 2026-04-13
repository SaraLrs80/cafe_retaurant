package test;

import com.cafe.caferestaurant.utils.PasswordUtil;

public class TestPassword {

    public static void main(String[] args) {

        String raw = "Admin123@";

        String hash = PasswordUtil.hash(raw);

        System.out.println("Mot de passe clair : " + raw);
        System.out.println("Hash BCrypt : " + hash);

    }
}