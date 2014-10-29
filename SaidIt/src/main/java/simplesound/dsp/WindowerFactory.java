package simplesound.dsp;

import org.jcaki.Doubles;

import static java.lang.Math.PI;
import static java.lang.Math.cos;

public class WindowerFactory {

    private static class RaisedCosineWindower implements DoubleVectorProcessor {
        double alpha;
        double cosineWindow[];

        RaisedCosineWindower(double alpha, int length) {
            if (length <= 0)
                throw new IllegalArgumentException("Window length cannot be smaller than 1");
            this.alpha = alpha;
            cosineWindow = new double[length];
            for (int i = 0; i < length; i++) {
                cosineWindow[i] = (1 - alpha) - alpha * cos(2 * PI * i / ((double) length - 1.0));
            }
        }

        public DoubleVector process(DoubleVector input) {
            return new DoubleVector(Doubles.multiply(input.data, cosineWindow));
        }

        public void processInPlace(DoubleVector input) {
            Doubles.multiplyInPlace(input.data, cosineWindow);
        }
    }

    public static DoubleVectorProcessor newHammingWindower(int length) {
        return new RaisedCosineWindower(0.46d, length);
    }

    public static DoubleVectorProcessor newHanningWindower(int length) {
        return new RaisedCosineWindower(0.5d, length);
    }

    public static DoubleVectorProcessor newTriangularWindower(int length) {
        return new RaisedCosineWindower(0.0d, length);
    }

}
