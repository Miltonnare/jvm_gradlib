import com.aura.codec.compression.Lz77Compression;
import com.aura.codec.compression.RleCompression;
import java.util.Arrays;

public class ArchiveDecoderFuzzer {
    public static void fuzzerTestOneInput(byte[] data) {
        if (data.length < 2) {
            return;
        }

        byte type = data[0];
        byte[] payload = Arrays.copyOfRange(data, 1, data.length);

        try {
            if ((type & 1) == 0) {
                // Fuzz Rle decompression
                RleCompression.decompress(payload, 1024 * 1024);
            } else {
                // Fuzz Lz77 decompression
                Lz77Compression.decompress(payload, 1024 * 1024);
            }
        } catch (Exception e) {
            // Ignore expected decompressor formatting issues
        }
    }
}
