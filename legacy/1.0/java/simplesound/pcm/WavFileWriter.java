package simplesound.pcm;

import android.util.Log;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

/**
 * Writes a wav file. Careful that it writes the total amount of the bytes information once the close method
 * is called. It has a counter in it to calculate the samle size.
 */
public class WavFileWriter implements Closeable {

    private final WavAudioFormat pcmAudioFormat;
    private final PcmMonoOutputStream pos;
    private int totalSampleBytesWritten = 0;
    private final File file;

    public WavFileWriter(WavAudioFormat wavAudioFormat, File file) throws IOException {
        if (wavAudioFormat.isBigEndian())
            throw new IllegalArgumentException("Wav file cannot contain bigEndian sample data.");
        if (wavAudioFormat.getSampleSizeInBits() > 8 && !wavAudioFormat.isSigned())
            throw new IllegalArgumentException("Wav file cannot contain unsigned data for this sampleSize:"
                    + wavAudioFormat.getSampleSizeInBits());
        this.pcmAudioFormat = wavAudioFormat;
        this.file = file;
        this.pos = new PcmMonoOutputStream(wavAudioFormat, file);
        pos.write(new RiffHeaderData(wavAudioFormat, 0).asByteArray());
    }

    public WavFileWriter write(byte[] bytes) throws IOException {
        checkLimit(totalSampleBytesWritten, bytes.length);
        pos.write(bytes);
        totalSampleBytesWritten += bytes.length;
        return this;
    }

    public WavFileWriter write(byte[] bytes, int offset, int count) throws IOException {
        checkLimit(totalSampleBytesWritten, count);
        pos.write(bytes, offset, count);
        totalSampleBytesWritten += count;
        return this;
    }

    private void checkLimit(int total, int toAdd) {
        final long result = total + toAdd;
        if (result >= Integer.MAX_VALUE) {
            throw new IllegalStateException("Size of bytes is too big:" + result);
        }
    }

    public WavFileWriter write(int[] samples) throws IOException {
        final int bytePerSample = pcmAudioFormat.getBytePerSample();
        checkLimit(totalSampleBytesWritten, samples.length * bytePerSample);
        pos.write(samples);
        totalSampleBytesWritten += samples.length * bytePerSample;
        return this;
    }

    public WavFileWriter write(short[] samples) throws IOException {
        checkLimit(totalSampleBytesWritten, samples.length * 2);
        pos.write(samples);
        totalSampleBytesWritten += samples.length * 2;
        return this;
    }

    WavFileWriter writeNormalized(double[] samples) throws IOException {
        return this;
    }

    public void close() throws IOException {
        pos.close();
        PcmAudioHelper.modifyRiffSizeData(file, totalSampleBytesWritten);
    }

    public PcmAudioFormat getWavFormat() {
        return pcmAudioFormat;
    }


    public int getTotalSampleBytesWritten() {
        return totalSampleBytesWritten;
    }
}
