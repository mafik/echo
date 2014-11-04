package eu.mrogalski.saidit;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

public class RingView extends View {

    static final String TAG = RingView.class.getSimpleName();

    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    public float ringWidth;
    private float[] intervals = new float[2];

    public static class Ring {
        public int ticks;
        public float max;
        public float value;
    }

    public ArrayList<Ring> rings = new ArrayList<Ring>();

    public RingView(Context context) {
        super(context);
        init();
    }

    public RingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        paint.setColor(0xff402400);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.BUTT);
        final Resources resources = getResources();
        if(resources != null) {
            final DisplayMetrics displayMetrics = resources.getDisplayMetrics();
            ringWidth = displayMetrics.density * 10;
            Log.d(TAG, "density: " + displayMetrics.density);
            Log.d(TAG, "densityDpi: " + displayMetrics.densityDpi);
            Log.d(TAG, "scaledDensity: " + displayMetrics.scaledDensity);
        } else {
            ringWidth = 10;
        }
        paint.setStrokeWidth(ringWidth);
        Ring ring;
        ring = new Ring();
        ring.ticks = 1;
        ring.max = 100;
        ring.value = 100;
        rings.add(ring);
        ring = new Ring();
        ring.ticks = 60;
        ring.max = 10;
        ring.value = 8;
        rings.add(ring);
        ring = new Ring();
        ring.ticks = 60;
        ring.max = 100;
        ring.value = 10;
        rings.add(ring);
    }

    private final RectF oval = new RectF();

    @Override
    protected void onDraw(Canvas canvas) {
        final int h = getMeasuredHeight();
        final int w = getMeasuredWidth();
        int lower = Math.min(w, h);
        float r = lower / 2f - ringWidth / 2;
        for(Ring ring : rings) {
            oval.set(w/2f - r, h/2f - r, w/2f + r, h/2f + r);
            if(ring.ticks > 1) {
                float circumference = (float) (Math.PI * 2 * r);
                float both = circumference / ring.ticks;
                intervals[0] = both * 3 / 4;
                intervals[1] = both * 1 / 4;
                final DashPathEffect pathEffect = new DashPathEffect(intervals, both - intervals[1]/2);
                paint.setPathEffect(pathEffect);
            } else {
                paint.setPathEffect(null);
            }
            canvas.drawArc(oval, 270, ring.value / ring.max * 360, false, paint);
            r -= ringWidth * 2;
        }
    }
}
