package Util;

import io.qameta.allure.Step;

import java.util.Random;

public class Tokens {
    static String correctChars = "1234567890ABCDEF";
    static String incorrectChars = "ZXQ";

    public static String generateToken(String chars) {
        int length = 32;
        StringBuilder stringBuilder = new StringBuilder(length);
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            stringBuilder.append(chars.charAt(index));
        }

        return stringBuilder.toString();
    }

    @Step("Создание случайного токена корректного формата")
    public static String generateCorrectToken() {
        return generateToken(correctChars);
    }

    @Step("Создание случайного токена некорректного формата")
    public static String generateIncorrectToken() {
        return generateToken(incorrectChars);
    }

    @Step("Создание случайного токена некорректной длины")
    public static String generateShortToken() {
        return generateToken(correctChars).substring(10);
    }
}
