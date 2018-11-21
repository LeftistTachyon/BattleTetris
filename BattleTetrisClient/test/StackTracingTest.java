
import java.util.Arrays;

public class StackTracingTest {
    public static void main(String[] args) {
        Exception e = new Exception();
        e.printStackTrace(System.out);
        System.out.println(Arrays.toString(e.getStackTrace()));
    }
}