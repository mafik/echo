package simplesound.pcm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class MonoWavFileReader {

    private final File file;
    private final RiffHeaderData riffHeaderData;

    public MonoWavFileReader(String fileName) throws IOException {
        this(new File(fileName));
    }

    public MonoWavFileReader(File file) throws IOException {
        this.file = file;
        riffHeaderData = new RiffHeaderData(file);
        if (riffHeaderData.getFormat().getChannels() != 1)
            throw new IllegalArgumentException("Wav file is not Mono.");
    }

    public PcmMonoInputStream getNewStream() throws IOException {
        PcmMonoInputStream asis = new PcmMonoInputStream(
                riffHeaderData.getFormat(),
                new FileInputStream(file));
        long amount = asis.skip(RiffHeaderData.PCM_RIFF_HEADER_SIZE);
        if (amount < RiffHeaderData.PCM_RIFF_HEADER_SIZE)
            throw new IllegalArgumentException("cannot skip necessary amount of bytes from underlying stream.");
        return asis;
    }

    private void validateFrameBoundaries(int frameStart, int frameEnd) {
        if (frameStart < 0)
            throw new IllegalArgumentException("Start Frame cannot be negative:" + frameStart);
        if (frameEnd < frameStart)
            throw new IllegalArgumentException("Start Frame cannot be after end frame. Start:"
                    + frameStart + ", end:" + frameEnd);
        if (frameEnd > riffHeaderData.getSampleCount())
            throw new IllegalArgumentException("Frame count out of bounds. Max sample count:"
                    + riffHeaderData.getSampleCount() + " but frame is:" + frameEnd);
    }

    public int[] getAllSamples() throws IOException {
        PcmMonoInputStream stream = getNewStream();
        try {
            return stream.readAll();
        } finally {
            stream.close();
        }
    }

    public int[] getSamplesAsInts(int frameStart, int frameEnd) throws IOException {
        validateFrameBoundaries(frameStart, frameEnd);
        PcmMonoInputStream stream = getNewStream();
        try {
            stream.skipSamples(frameStart);
            return stream.readSamplesAsIntArray(frameEnd - frameStart);
        } finally {
            stream.close();
        }
    }


    public PcmAudioFormat getFormat() {
        return riffHeaderData.getFormat();
    }

    public int getSampleCount() {
        return riffHeaderData.getSampleCount();
    }

    public File getFile() {
        return file;
    }
}
