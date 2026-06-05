package Util;

import java.util.Random;

public class Tokens {
    public static String generateToken() {
        int length = 32;
        String chars = "1234567890ABCDEF";
        StringBuilder stringBuilder = new StringBuilder(length);
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            stringBuilder.append(chars.charAt(index));
        }

        return stringBuilder.toString();
    }


    public static String generateIncorrectToken() {
        return generateToken().substring(10);
    }
}
