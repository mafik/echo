package simplesound.pcm;

import org.jcaki.Bytes;
import org.jcaki.IOs;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class PcmMonoInputStream extends InputStream implements Closeable {

    private final PcmAudioFormat format;
    private final DataInputStream dis;
    /**
     * this is used for normalization.
     */
    private final int maxPositiveIntegerForSampleSize;


    public PcmMonoInputStream(PcmAudioFormat format, InputStream is) {
        if (format.getChannels() != 1)
            throw new IllegalArgumentException("Only mono streams are supported.");
        this.format = format;
        this.dis = new DataInputStream(is);
        this.maxPositiveIntegerForSampleSize = 0x7fffffff >>> (32 - format.getSampleSizeInBits());
    }

    public int read() throws IOException {
        return dis.read();
    }

    public int[] readSamplesAsIntArray(int amount) throws IOException {
        byte[] bytez = new byte[amount * format.getBytePerSample()];
        int readAmount = dis.read(bytez);
        if (readAmount == -1)
            return new int[0];
        return Bytes.toReducedBitIntArray(
                bytez,
                readAmount,
                format.getBytePerSample(),
                format.getSampleSizeInBits(),
                format.isBigEndian());
    }

    public int[] readAll() throws IOException {
        byte[] all = IOs.readAsByteArray(dis);
        return Bytes.toReducedBitIntArray(
                all,
                all.length,
                format.getBytePerSample(),
                format.getSampleSizeInBits(),
                format.isBigEndian());
    }

    private static final int BYTE_BUFFER_SIZE = 4096;

    /**
     * reads samples as byte array. if there is not enough data for the amount of samples, remaining data is returned
     * anyway. if the byte amount is not an order of bytes required for sample (such as 51 bytes left but 16 bit samples)
     * an IllegalStateException is thrown.
     *
     * @param amount amount of samples to read.
     * @return byte array.
     * @throws IOException           if there is an IO error.
     * @throws IllegalStateException if the amount of bytes read is not an order of correct.
     */
    public byte[] readSamplesAsByteArray(int amount) throws IOException {

        byte[] bytez = new byte[amount * format.getBytePerSample()];
        int readCount = dis.read(bytez);
        if (readCount != bytez.length) {
            validateReadCount(readCount);
            byte[] result = new byte[readCount];
            System.arraycopy(bytez, 0, result, 0, readCount);
            return result;
        } else
            return bytez;
    }

    private void validateReadCount(int readCount) {
        if (readCount % format.getBytePerSample() != 0)
            throw new IllegalStateException("unexpected amounts of bytes read from the input stream. " +
                    "Byte count must be an order of:" + format.getBytePerSample());
    }

    public int[] readSamplesAsIntArray(int frameStart, int frameEnd) throws IOException {
        skipSamples(frameStart * format.getBytePerSample());
        return readSamplesAsIntArray(frameEnd - frameStart);
    }

    /**
     * skips samples from the stream. if end of file is reached, it returns the amount that is actually skipped.
     *
     * @param skipAmount amount of samples to skip
     * @return actual skipped sample count.
     * @throws IOException if there is a problem while skipping.
     */
    public int skipSamples(int skipAmount) throws IOException {
        long actualSkipped = dis.skip(skipAmount * format.getBytePerSample());
        return (int) actualSkipped / format.getBytePerSample();
    }

    public double[] readSamplesNormalized(int amount) throws IOException {
        return normalize(readSamplesAsIntArray(amount));
    }

    public double[] readSamplesNormalized() throws IOException {
        return normalize(readAll());
    }

    private double[] normalize(int[] original) {
        if (original.length == 0)
            return new double[0];
        double[] normalized = new double[original.length];
        for (int i = 0; i < normalized.length; i++) {
            normalized[i] = (double) original[i] / maxPositiveIntegerForSampleSize;
        }
        return normalized;
    }

    public void close() throws IOException {
        dis.close();
    }

    /**
     * finds the byte location of a given time. if time is negative, exception is thrown.
     *
     * @param second second information
     * @return the byte location in the samples.
     */
    public int calculateSampleByteIndex(double second) {

        if (second < 0)
            throw new IllegalArgumentException("Time information cannot be negative.");

        int loc = (int) (second * format.getSampleRate() * format.getBytePerSample());

        //byte alignment. 
        if (loc % format.getBytePerSample() != 0) {
            loc += (format.getBytePerSample() - loc % format.getBytePerSample());
        }
        return loc;
    }

    /**
     * calcualates the time informationn for a given sample.
     *
     * @param sampleIndex sample index.
     * @return approximate seconds information for the given sample.
     */
    public double calculateSampleTime(int sampleIndex) {
        if (sampleIndex < 0)
            throw new IllegalArgumentException("sampleIndex information cannot be negative:" + sampleIndex);

        return (double) sampleIndex / format.getSampleRate();
    }

    public PcmAudioFormat getFormat() {
        return format;
    }
}
