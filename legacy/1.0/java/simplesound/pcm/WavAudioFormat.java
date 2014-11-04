package simplesound.pcm;

public class WavAudioFormat extends PcmAudioFormat {

    /**
     * if data is represented as big endian or little endian.
     */
    protected final boolean bigEndian = false;

    private WavAudioFormat(int sampleRate, int sampleSizeInBits, int channels, boolean signed) {
        super(sampleRate, sampleSizeInBits, channels, false, signed);
    }

    /**
     * a builder class for generating PCM Audio format for wav files.
     */
    public static class Builder {
        private int _sampleRate;
        private int _sampleSizeInBits = 16;
        private int _channels = 1;

        public Builder sampleRate(int sampleRate) {
            this._sampleRate = sampleRate;
            return this;
        }

        public Builder channels(int channels) {
            this._channels = channels;
            return this;
        }

        public Builder sampleSizeInBits(int sampleSizeInBits) {
            this._sampleSizeInBits = sampleSizeInBits;
            return this;
        }

        public WavAudioFormat build() {
            if (_sampleSizeInBits == 8)
                return new WavAudioFormat(_sampleRate, _sampleSizeInBits, _channels, false);
            else
                return new WavAudioFormat(_sampleRate, _sampleSizeInBits, _channels, true);
        }
    }

    /**
     * generates a PcmAudioFormat for wav files for 16 bits signed mono data.
     *
     * @param sampleRate sampling rate.
     * @return new PcmAudioFormat object for given wav header values. .
     */
    public static WavAudioFormat mono16Bit(int sampleRate) {
        return new WavAudioFormat(sampleRate, 16, 1, true);
    }

    /**
     * Generates audio format data for Wav audio format. returning PCM format is little endian.
     *
     * @param sampleRate       sample rate
     * @param sampleSizeInBits bit amount per sample
     * @param channels         channel count. can be 1 or 2
     * @return a RawAudioFormat suitable for wav format.
     */
    public static WavAudioFormat wavFormat(int sampleRate, int sampleSizeInBits, int channels) {
        if (sampleSizeInBits == 8)
            return new WavAudioFormat(sampleRate, sampleSizeInBits, channels, false);
        else
            return new WavAudioFormat(sampleRate, sampleSizeInBits, channels, true);
    }
}
