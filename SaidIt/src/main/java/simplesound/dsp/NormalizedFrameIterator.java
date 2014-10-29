package simplesound.dsp;

import simplesound.dsp.DoubleVector;
import simplesound.pcm.PcmMonoInputStream;

import java.io.IOException;
import java.util.Iterator;

public class NormalizedFrameIterator implements Iterator<DoubleVector> {

    private final PcmMonoInputStream pmis;
    private final int frameSize;
    private final int shiftAmount;
    //TODO: not applied yet
    private final boolean applyPadding;

    public NormalizedFrameIterator(PcmMonoInputStream pmis, int frameSize, int shiftAmount, boolean applyPadding) {
        if (frameSize < 1)
            throw new IllegalArgumentException("Frame size must be larger than zero.");
        if (shiftAmount < 1)
            throw new IllegalArgumentException("Shift size must be larger than zero.");
        this.pmis = pmis;
        this.frameSize = frameSize;
        this.shiftAmount = shiftAmount;
        this.applyPadding = applyPadding;
    }

    public NormalizedFrameIterator(PcmMonoInputStream pmis, int frameSize, boolean applyPadding) {
        this(pmis, frameSize, frameSize, applyPadding);
    }

    public NormalizedFrameIterator(PcmMonoInputStream pmis, int frameSize) {
        this(pmis, frameSize, frameSize, false);
    }

    private DoubleVector currentFrame;
    private int frameCounter;

    public boolean hasNext() {
        double[] data;
        try {
            if (frameCounter == 0) {
                data = pmis.readSamplesNormalized(frameSize);
                if (data.length < frameSize)
                    return false;
                currentFrame = new DoubleVector(data);
            } else {
                data = pmis.readSamplesNormalized(shiftAmount);
                if (data.length < shiftAmount)
                    return false;
                double[] frameData = currentFrame.data.clone();
                System.arraycopy(data, 0, frameData, frameData.length - shiftAmount, shiftAmount);
                currentFrame = new DoubleVector(frameData);
            }
        } catch (IOException e) {
            return false;
        }
        frameCounter++;
        return true;
    }

    public DoubleVector next() {
        return currentFrame;
    }

    public void remove() {
        throw new UnsupportedOperationException("Remove is not supported.");
    }

    public int getFrameSize() {
        return frameSize;
    }

    public int getShiftAmount() {
        return shiftAmount;
    }
}
