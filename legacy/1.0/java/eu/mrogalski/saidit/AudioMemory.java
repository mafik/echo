package eu.mrogalski.saidit;

import android.os.SystemClock;

import java.util.concurrent.LinkedBlockingDeque;

public class AudioMemory {

    final int FILL_RATE = SaidItService.SAMPLE_RATE;
    final int CHUNK_SIZE = FILL_RATE * 2 * 10;

    private final LinkedBlockingDeque<byte[]> filled = new LinkedBlockingDeque<byte[]>();
    private final LinkedBlockingDeque<byte[]> free = new LinkedBlockingDeque<byte[]>();

    private long fillingStartUptimeMillis;
    private boolean filling = false;
    private boolean currentWasFilled = false;
    private byte[] current = null;
    private int offset = 0;

    public AudioMemory() {
        final long maxMemory = Runtime.getRuntime().maxMemory();
        final long useMemory = maxMemory / 8 * 2; // use 1/4 of maxMemory

        for(long i = 0; i < useMemory; i += CHUNK_SIZE) {
            free.addLast(new byte[CHUNK_SIZE]);
        }
    }

    public interface Consumer {
        public int consume(byte[] array, int offset, int count);
    }

    public void read(Consumer reader) {
        synchronized (this) {
            if(!filling && current != null && currentWasFilled) {
                reader.consume(current, offset, current.length - offset);
            }
            for(byte[] arr : filled) {
                reader.consume(arr, 0, arr.length);
            }
            if(current != null && offset > 0) {
                reader.consume(current, 0, offset);
            }
        }
    }

    public void fill(Consumer filler) {
        synchronized (this) {
            if(current == null) {
                if(free.isEmpty()) {
                    currentWasFilled = true;
                    current = filled.removeFirst();
                } else {
                    currentWasFilled = false;
                    current = free.removeFirst();
                }
                offset = 0;
            }
            filling = true;
            fillingStartUptimeMillis = SystemClock.uptimeMillis();
        }

        int read = filler.consume(current, offset, current.length - offset);

        synchronized (this) {
            if(offset + read >= current.length) {
                filled.addLast(current);
                current = null;
                offset = 0;
            } else {
                offset += read;
            }
            filling = false;
        }
    }

    public interface Observer {
        public void observe(int probablyTaken, int reallyTaken, int total);
    }

    public void observe(Observer observer) {
        final int probablyTaken;
        final int reallyTaken;
        final int total;
        synchronized (this) {
            reallyTaken = filled.size() * CHUNK_SIZE + (current == null ? 0 : currentWasFilled ? CHUNK_SIZE : offset);
            total = (filled.size() + free.size() + (current == null ? 0 : 1)) * CHUNK_SIZE;
            probablyTaken = (int) (filling ? (SystemClock.uptimeMillis() - fillingStartUptimeMillis) * FILL_RATE / 1000 : 0);
        }
        observer.observe(probablyTaken, reallyTaken, total);
    }

}
