package simplesound.dsp;

public class MutableComplex {
    public double real;
    public double imaginary;

    public MutableComplex(double real, double imaginary) {
        this.real = real;
        this.imaginary = imaginary;
    }

    public MutableComplex(Complex complex) {
        this.real = complex.real;
        this.imaginary = complex.imaginary;
    }

    public Complex getImmutableComplex() {
        return new Complex(real, imaginary);
    }
}
