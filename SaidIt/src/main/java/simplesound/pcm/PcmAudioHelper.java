package simplesound.pcm;

import static org.jcaki.Bytes.toByteArray;
import org.jcaki.IOs;

import java.io.*;

public class PcmAudioHelper {

    /**
     * Converts a pcm encoded raw audio stream to a wav file.
     *
     * @param af format
     * @param rawSource raw source file
     * @param wavTarget raw file target
     * @throws IOException thrown if an error occurs during file operations.
     */
    public static void convertRawToWav(WavAudioFormat af, File rawSource, File wavTarget) throws IOException {
        DataOutputStream dos = new DataOutputStream(new FileOutputStream(wavTarget));
        dos.write(new RiffHeaderData(af, 0).asByteArray());
        DataInputStream dis = new DataInputStream(new FileInputStream(rawSource));
        byte[] buffer = new byte[4096];
        int i;
        int total = 0;
        while ((i = dis.read(buffer)) != -1) {
            total += i;
            dos.write(buffer, 0, i);
        }
        dos.close();
        modifyRiffSizeData(wavTarget, total);
    }

    public static void convertWavToRaw(File wavSource, File rawTarget) throws IOException {
        IOs.copy(new MonoWavFileReader(wavSource).getNewStream(), new FileOutputStream(rawTarget));
    }

    public static double[] readAllFromWavNormalized(String fileName) throws IOException {
        return new MonoWavFileReader(new File(fileName)).getNewStream().readSamplesNormalized();
    }

    /**
     * Modifies the size information in a wav file header.
     *
     * @param wavFile a wav file
     * @param size    size to replace the header.
     * @throws IOException if an error occurs whule accesing the data.
     */
    static void modifyRiffSizeData(File wavFile, int size) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(wavFile, "rw");
        raf.seek(RiffHeaderData.RIFF_CHUNK_SIZE_INDEX);
        raf.write(toByteArray(size + 36, false));
        raf.seek(RiffHeaderData.RIFF_SUBCHUNK2_SIZE_INDEX);
        raf.write(toByteArray(size, false));
        raf.close();
    }

    public static void generateSilenceWavFile(WavAudioFormat wavAudioFormat, File file, double sec) throws IOException {
        WavFileWriter wfr = new WavFileWriter(wavAudioFormat, file);
        int[] empty = new int[(int) (sec * wavAudioFormat.getSampleRate())];
        try {
            wfr.write(empty);
        } finally {
            wfr.close();
        }
    }

}
