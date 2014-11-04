package simplesound.pcm;

import org.jcaki.Bytes;
import org.jcaki.IOs;

import java.io.*;

public class PcmMonoOutputStream extends OutputStream implements Closeable {

    final PcmAudioFormat format;
    final DataOutputStream dos;

    public PcmMonoOutputStream(PcmAudioFormat format, DataOutputStream dos) {
        this.format = format;
        this.dos = dos;
    }

    public PcmMonoOutputStream(PcmAudioFormat format, File file) throws IOException {
        this.format = format;
        this.dos = new DataOutputStream(new FileOutputStream(file));
    }

    public void write(int b) throws IOException {
        dos.write(b);
    }

    @Override
    public void write(byte[] buffer, int offset, int count) throws IOException {
        dos.write(buffer, offset, count);
    }

    public void write(short[] shorts) throws IOException {
        dos.write(Bytes.toByteArray(shorts, shorts.length, format.isBigEndian()));
    }

    public void write(int[] ints) throws IOException {
        dos.write(Bytes.toByteArray(ints, ints.length, format.getBytePerSample(), format.isBigEndian()));
    }

    public void close() {
        IOs.closeSilently(dos);
    }
}
