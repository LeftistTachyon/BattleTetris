
import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;

public class CPUUseTest {
    public static void main(String[] args) {
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.
                getOperatingSystemMXBean();
        System.out.println(osBean.getSystemCpuLoad());
    }
}