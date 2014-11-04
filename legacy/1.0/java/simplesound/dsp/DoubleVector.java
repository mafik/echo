package simplesound.dsp;

import java.util.Arrays;

/**
 * a vector containing a double numbers.
 */
public class DoubleVector {

    final double[] data;

    public DoubleVector(double[] data) {
        if (data == null)
            throw new IllegalArgumentException("Data cannot be null!");
        this.data = data;
    }

    public int size() {
        return data.length;
    }

    public double[] getData() {
        return data;
    }


    @Override
    public String toString() {
        return Arrays.toString(data);
    }

}
