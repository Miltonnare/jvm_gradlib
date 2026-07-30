import com.aura.container.AuraContainerReader;
import com.aura.codec.encryption.XorEncryptionProvider;

public class ContainerParserFuzzer {
    public static void fuzzerTestOneInput(byte[] data) {
        try {
            AuraContainerReader reader = new AuraContainerReader(data);
            reader.setEncryptionProvider(new XorEncryptionProvider(new byte[]{0x55, (byte) 0xAA}));
            if (reader.getIndex() != null) {
                for (var entry : reader.getIndex().getEntries()) {
                    try {
                        reader.readResource(entry.getName());
                    } catch (Exception e) {
                        // Ignore resource-level exceptions to continue fuzzing other entries
                    }
                }
            }
        } catch (Exception e) {
            // Ignore expected container formatting exceptions
        }
    }
}
