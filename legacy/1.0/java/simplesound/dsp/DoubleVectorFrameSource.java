package simplesound.dsp;

import simplesound.pcm.PcmMonoInputStream;

import java.util.Iterator;

public class DoubleVectorFrameSource {

    private final PcmMonoInputStream pmis;
    private final int frameSize;
    private final int shiftAmount;
    private final boolean paddingApplied;

    private DoubleVectorFrameSource(PcmMonoInputStream pmis, int frameSize, int shiftAmount, boolean paddingApplied) {
        this.pmis = pmis;
        this.frameSize = frameSize;
        this.shiftAmount = shiftAmount;
        this.paddingApplied = paddingApplied;
    }

    public static DoubleVectorFrameSource fromSampleAmount(
            PcmMonoInputStream pmis, int frameSize, int shiftAmount) {
        return new DoubleVectorFrameSource(pmis, frameSize, shiftAmount, false);
    }

    public static DoubleVectorFrameSource fromSampleAmountWithPadding(
            PcmMonoInputStream pmis, int frameSize, int shiftAmount) {
        return new DoubleVectorFrameSource(pmis, frameSize, shiftAmount, true);
    }

    public static DoubleVectorFrameSource fromSizeInMiliseconds(
            PcmMonoInputStream pmis, double frameSizeInMilis, double shiftAmountInMilis) {
        return new DoubleVectorFrameSource(pmis,
                pmis.getFormat().sampleCountForMiliseconds(frameSizeInMilis),
                pmis.getFormat().sampleCountForMiliseconds(shiftAmountInMilis),
                false);
    }

    public static DoubleVectorFrameSource fromSizeInMilisecondsWithPadding(
            PcmMonoInputStream pmis, double frameSizeInMilis, double shiftAmountInMilis) {
        return new DoubleVectorFrameSource(pmis,
                pmis.getFormat().sampleCountForMiliseconds(frameSizeInMilis),
                pmis.getFormat().sampleCountForMiliseconds(shiftAmountInMilis),
                true);
    }

    public Iterable<DoubleVector> getIterableFrameReader() {
        return new Iterable<DoubleVector>() {
            public Iterator<DoubleVector> iterator() {
                return new NormalizedFrameIterator(pmis, frameSize, shiftAmount, paddingApplied);
            }
        };
    }

    public Iterator<DoubleVector> getNormalizedFrameIterator() {
        return new NormalizedFrameIterator(pmis, frameSize, shiftAmount, paddingApplied);
    }

    public PcmMonoInputStream getPmis() {
        return pmis;
    }

    public int getFrameSize() {
        return frameSize;
    }

    public int getShiftAmount() {
        return shiftAmount;
    }

    public boolean isPaddingApplied() {
        return paddingApplied;
    }
}
