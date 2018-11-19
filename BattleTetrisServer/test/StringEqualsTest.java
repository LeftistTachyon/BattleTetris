
import java.util.Random;

public class StringEqualsTest {
    public static void main(String[] args) {
        final String chars = "QWERTYUIOPASDFGHKLZXCVBNM qwertyuiopasdfghklzxcvbnm1234567890~!@#$%^&*()+`-={}|[]\\:\";\'<>?,./", 
                toCompare = ".";
        final int totalChars = chars.length(), tests = 10000, strLength = 250;
                
        String[] strings = new String[tests];
        Random r = new Random();
        for (int i = 0; i < tests; i++) {
            String temp = "";
            for (int j = 0; j < strLength; j++) {
                temp += chars.charAt(r.nextInt(totalChars));
            }
            strings[i] = temp;
        }
        
        double start, total;
        
        start = System.nanoTime();
        for (String string : strings) {
            string.equals(".");
        }
        total = System.nanoTime() - start;
        System.out.printf("big to small: %.2fms%n", total/1000000);
        
        start = System.nanoTime();
        for (String string : strings) {
            ".".equals(string);
        }
        total = System.nanoTime() - start;
        System.out.printf("small to big: %.2fms%n", total/1000000);
        
        // Result: 
        // small to big is usually faster, especially if strLength is small
        // however, big to small is sometimes faster if strLength is big
    }
}