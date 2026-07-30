import com.aura.stream.BitInputStream;
import com.aura.stream.BoundedInputStream;
import com.aura.stream.ChecksummingInputStream;
import java.io.ByteArrayInputStream;

public class StreamReaderFuzzer {
    public static void fuzzerTestOneInput(byte[] data) {
        if (data.length < 2) {
            return;
        }

        int maxLimit = data[0] & 0xFF;
        byte[] payload = java.util.Arrays.copyOfRange(data, 1, data.length);

        try (var bis = new ByteArrayInputStream(payload);
             var bounded = new BoundedInputStream(bis, maxLimit);
             var checksumming = new ChecksummingInputStream(bounded);
             var bitStream = new BitInputStream(checksumming)) {

            while (true) {
                int bitsCount = bitStream.readBits(3); // Read 3 bits at a time
                if (bitsCount == -1) {
                    break;
                }
            }
            checksumming.getChecksum();
        } catch (Exception e) {
            // Ignore expected stream boundaries exceptions
        }
    }
}
