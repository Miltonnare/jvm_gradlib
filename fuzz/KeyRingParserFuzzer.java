import com.aura.codec.encryption.AuraKeyRing;

public class KeyRingParserFuzzer {
    public static void fuzzerTestOneInput(byte[] data) {
        try {
            AuraKeyRing.parse(data);
        } catch (Exception e) {
        }
    }
}
