public class PrintStackTraceTest {
    public static void main(String[] args) {
        System.out.println("A1");
        try {
            int i = 1 / 0;
        } catch (Exception e) {
            e.printStackTrace(System.out);
        } finally {
            System.out.println("A2");
        }
    }
}