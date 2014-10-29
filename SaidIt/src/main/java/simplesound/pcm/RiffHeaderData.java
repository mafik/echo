package simplesound.pcm;

import static org.jcaki.Bytes.toByteArray;
import static org.jcaki.Bytes.toInt;
import org.jcaki.IOs;

import java.io.*;

class RiffHeaderData {

    public static final int PCM_RIFF_HEADER_SIZE = 44;
    public static final int RIFF_CHUNK_SIZE_INDEX = 4;
    public static final int RIFF_SUBCHUNK2_SIZE_INDEX = 40;

    private final PcmAudioFormat format;
    private final int totalSamplesInByte;

    public RiffHeaderData(PcmAudioFormat format, int totalSamplesInByte) {
        this.format = format;
        this.totalSamplesInByte = totalSamplesInByte;
    }

    public double timeSeconds() {
        return (double) totalSamplesInByte / format.getBytePerSample() / format.getSampleRate();
    }

    public RiffHeaderData(DataInputStream dis) throws IOException {

        try {
            byte[] buf4 = new byte[4];
            byte[] buf2 = new byte[2];

            dis.skipBytes(4 + 4 + 4 + 4 + 4 + 2);

            dis.readFully(buf2);
            final int channels = toInt(buf2, false);

            dis.readFully(buf4);
            final int sampleRate = toInt(buf4, false);

            dis.skipBytes(4 + 2);

            dis.readFully(buf2);
            final int sampleSizeInBits = toInt(buf2, false);

            dis.skipBytes(4);

            dis.readFully(buf4);
            totalSamplesInByte = toInt(buf4, false);

            format = new WavAudioFormat.Builder().
                    channels(channels).
                    sampleRate(sampleRate).
                    sampleSizeInBits(sampleSizeInBits).
                    build();
        } finally {
            IOs.closeSilently(dis);
        }
    }

    public RiffHeaderData(File file) throws IOException {
        this(new DataInputStream(new FileInputStream(file)));
    }

    public byte[] asByteArray() {
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            // ChunkID (the String "RIFF") 4 Bytes
            baos.write(toByteArray(0x52494646, true));
            // ChunkSize (Whole file size in byte minus 8 bytes ) , or (4 + (8 + SubChunk1Size) + (8 + SubChunk2Size))
            // little endian 4 Bytes.
            baos.write(toByteArray(36 + totalSamplesInByte, false));
            // Format (the String "WAVE") 4 Bytes big endian
            baos.write(toByteArray(0x57415645, true));

            // Subchunk1
            // Subchunk1ID (the String "fmt ") 4 bytes big endian.
            baos.write(toByteArray(0x666d7420, true));
            // Subchunk1Size. 16 for the PCM. little endian 4 bytes.
            baos.write(toByteArray(16, false));
            // AudioFormat , for PCM = 1, Little endian 2 Bytes.
            baos.write(toByteArray((short) 1, false));
            // Number of channels Mono = 1, Stereo = 2  Little Endian , 2 bytes.
            int channels = format.getChannels();
            baos.write(toByteArray((short) channels, false));
            // SampleRate (8000, 44100 etc.) little endian, 4 bytes
            int sampleRate = format.getSampleRate();
            baos.write(toByteArray(sampleRate, false));
            // byte rate (SampleRate * NumChannels * BitsPerSample/8) little endian, 4 bytes.
            baos.write(toByteArray(channels * sampleRate * format.getBytePerSample(), false));
            // Block Allign == NumChannels * BitsPerSample/8  The number of bytes for one sample including all channels. LE, 2 bytes
            baos.write(toByteArray((short) (channels * format.getBytePerSample()), false));
            // BitsPerSample (8, 16 etc.) LE, 2 bytes
            baos.write(toByteArray((short) format.getSampleSizeInBits(), false));

            // Subchunk2
            // SubChunk2ID (String "data") 4 bytes.
            baos.write(toByteArray(0x64617461, true));
            // Subchunk2Size    == NumSamples * NumChannels * BitsPerSample/8. This is the number of bytes in the data.
            // You can also think of this as the size of the read of the subchunk following this number. LE, 4 bytes.
            baos.write(toByteArray(totalSamplesInByte, false));

            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        } finally {
            IOs.closeSilently(baos);
        }
    }

    public PcmAudioFormat getFormat() {
        return format;
    }

    public int getTotalSamplesInByte() {
        return totalSamplesInByte;
    }

    public int getSampleCount() {
        return totalSamplesInByte / format.getBytePerSample();
    }

    public String toString() {
        return "[ Format: " + format.toString() + " , totalSamplesInByte:" + totalSamplesInByte + "]";
    }
}
