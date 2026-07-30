import com.aura.container.ContainerIndex;

public class BinaryIndexFuzzer {
    public static void fuzzerTestOneInput(byte[] data) {
        try {
            ContainerIndex.parse(data);
        } catch (Exception e) {
        }
    }
}
