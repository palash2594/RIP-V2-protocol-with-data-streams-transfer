import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * This class manages all the output streams.
 */

public class ManageOutputStreams {

    public String getFileType(byte[] data) {
        String fileName = new String(Arrays.copyOfRange(data, 11, data.length));
        return fileName.trim();
    }

    public byte[] filterData(byte[] data) {

        int offset = (data[12] & 0xFF);
        for (int i = 1; i < 3; i++) {
            offset = (offset << 8) | (data[i]) & 0xFF;
        }

        return Arrays.copyOfRange(data, 14, offset);
    }

    public boolean checkIfLastPacket(byte[] data) {

        return false;
    }

    public void outputDataFile() throws IOException {

        Map<String, BufferedOutputStream> outputStreams = DataStore.getOutputStreams();
        Map<String, List<byte []>> outputData = DataStore.getOutputData();

        while (true) {

            for (Map.Entry<String, List<byte[]>> entry : outputData.entrySet()) {
                String source = entry.getKey();
                List<byte []> packets = entry.getValue();

                if (!outputStreams.containsKey(source)) {

                    String fileName = getFileType(packets.get(0));
                    FileOutputStream fout = new FileOutputStream(source + "_" + fileName);
                    BufferedOutputStream bout = new BufferedOutputStream(fout);
                    outputStreams.put(source, bout);

                    // removed the first packet.
                    packets.remove(0);
                }

                for (byte[] currentPacket : packets) {
                    // writing on file.
                    byte[] filteredCurrentPacket = filterData(currentPacket);
                    DataStore.getOutputStreams().get(source).write(filteredCurrentPacket);
                }

            }
        }
    }
}
