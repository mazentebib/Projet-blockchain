package com.isd.blockchain;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Arrays;

public class Test {

	public static void main(String[] args) {
        String a = "foo";
        String b = "bar";

        // Concatenate the two strings
        String concat = b+a;

        // Convert the concatenated string to a char array and sort it
        char[] sortedChars = concat.toCharArray();
        Arrays.sort(sortedChars);

        // Convert the sorted char array back to a string
        String sortedString = new String(sortedChars);

        // Calculate the hash of the sorted string
        String result = hash(sortedString);

        System.out.println(result );
        
        Instant instant = Instant.now();
        long timestamp = instant.getEpochSecond();
        System.out.println("Timestamp actuel : " + timestamp);
    }

	public static String hash(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
