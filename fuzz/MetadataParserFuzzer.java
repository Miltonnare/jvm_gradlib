import com.aura.metadata.BinaryMetadataReader;

public class MetadataParserFuzzer {
    public static void fuzzerTestOneInput(byte[] data) {
        try {
            BinaryMetadataReader.read(data);
        } catch (Exception e) {
            // Ignore expected BSM formatting exceptions
        }
    }
}
