package simplesound.dsp;

import java.util.Iterator;
import java.util.List;

public class DoubleVectorProcessingPipeline  {

    List<DoubleVectorProcessor> processors;
    Iterator<DoubleVector> vectorSource;

    public DoubleVectorProcessingPipeline(Iterator<DoubleVector> vectorSource,
                                          List<DoubleVectorProcessor> processors) {
        this.vectorSource = vectorSource;
        this.processors = processors;
    }
}
