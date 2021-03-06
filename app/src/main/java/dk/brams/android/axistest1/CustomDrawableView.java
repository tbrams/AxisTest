package dk.brams.android.axistest1;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Locale;

public class CustomDrawableView extends View {
    public static final String TAG = "TBR:CustomDrawableView";

    /**
     * First attempt to make a scalable axis display.  Will do proper setup later, but using
     * hardcoding at this time...
     *
     *
     * The settings below are the limits on the coordinate system and the step size on each axis.
     */

    private static final int MARGIN=10;
    private final Paint mAnnotationPaint;

    // Variables used for canvas dimensions
    private int mCanvasMinX;
    private int mCanvasMaxX;
    private int mCanvasMinY;
    private int mCanvasMaxY;

    // Variables used for axis values
    private float mXmin;
    private float mXmax;
    private float mYmin;
    private float mYmax;
    private float mDx;
    private float mDy;
    private float mDxm;
    private float mDym;

    // Variables used for transformation
    private float mAlphaX;
    private float mX0;
    private float mAlphaY;
    private float mY0;

    // various settings for graphics
    private float mLineWidth=10;
    private int   mLineColor=0xffee00ee;
    private int   mShapeColor=0xff74AC23;

    private int   mBarColor=0x77740074;
    private float mBarGapPercentage=10;

    private final Paint mAxisLinePaint;
    private float mFontSize;

    private ArrayList<Annotation> mMarkerList = new ArrayList();

    public CustomDrawableView(Context context) {
        super(context);


        // Initialize axis values
        axis_settings_2();

        // Initialize the graphics style for the axis and tick marks
        mAxisLinePaint = new Paint();
        mAxisLinePaint.setColor(0xff0000ee);
        mAxisLinePaint.setStrokeWidth(5);

        // Set the font size for the axis values
        Resources res = getResources();
        mFontSize = res.getDimension(R.dimen.axis_text_size);
        mAxisLinePaint.setTextSize(mFontSize);
//        mAxisLinePaint.setTextAlign(Paint.Align.CENTER);

        // Prepare paint for annotations
        mAnnotationPaint = new Paint();
        mAnnotationPaint.setColor(0xffee0000);

    }

    private void axis_settings_1() {
        setAxisValues(-50, 25, 25, -30, 50, 10);
    }

    private void axis_settings_2() {
        setAxisValues(-1.5f,1.5f, .5f, .1f, -2.0f, 2.0f, .5f, .1f);
    }

    /*
     * Handle initialization of scaling parameters here, when the layout size is known
     */
    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {
        super.onSizeChanged(width, height, oldw, oldh);

        mCanvasMinX = MARGIN;
        mCanvasMaxX = width-MARGIN-MARGIN;

        mCanvasMinY = height-MARGIN-MARGIN;
        mCanvasMaxY = MARGIN;

        // Define parameters for mapping real world values to canvas equivalents.
        mAlphaX = (mCanvasMaxX-mCanvasMinX)/(mXmax - mXmin);
        mAlphaY = (mCanvasMaxY-mCanvasMinY)/(mYmax - mYmin);


        mX0 = mCanvasMaxX-mAlphaX* mXmax;
        mY0 = mCanvasMaxY-mAlphaY* mYmax;

    }



    protected void onDraw(Canvas canvas) {
        drawAxisSystem(canvas);

        application_2(canvas);
    }

    private void application_1(Canvas canvas) {
        // draw an ellipse
        setShapeColor(0x55005555);
        drawEllipse(canvas, -25, -10, 500, 300);

        // Draw polyline
        Point[] myPoints = {
                new Point(-45, -15),
                new Point(-30, -20),
                new Point(10, -5),
                new Point(15, -15),
                new Point(20, -25)
        };
        drawPolyLine(canvas, myPoints, Color.RED);


        // Draw polygon
        Point[] myPolygon = {
                new Point(-45, 45),
                new Point(-20, 20),
                new Point(-5, 45)
        };
        drawPolygon(canvas, myPolygon, Color.GREEN);

        // Draw bar chart
        int[] values = {10, 20, 40};
        drawBarList(canvas, values);
    }


    private void application_2(Canvas canvas) {

        // Draw polygon
        PointF[] myPolygon = {
                new PointF(0.1f, 0.1f),
                new PointF(0.1f, 1.1f),
                new PointF(0.3f, 1.5f),
                new PointF(1.1f, 1.5f),
                new PointF(1.1f, 0.1f)
        };
        drawPolygon(canvas, myPolygon, Color.GREEN);

        // Annotation
        drawAnnotation(canvas, .8f, .95f, 10f);


        // Make a nice parametric plot of a Lissajous curve
        ArrayList myPointArr = new ArrayList();
        double dt=0.02;
        for (double t=0;t<=(2*Math.PI+dt);t+=dt){
            float x=(float)(Math.cos(3*t));
            float y=(float)(Math.sin(2*t));
            myPointArr.add(new PointF(x, y));
        }

        drawPolyLine(canvas, myPointArr, Color.RED);


    }

    private void drawAnnotation(Canvas canvas, float x, float y, float radius) {

        // Create an object to store away the annotation point
        Annotation annotation = new Annotation(
                new PointF(valueToCanvasX(x),valueToCanvasY(y)),
                convertDpToPixel(getContext(), radius)
        );
        mMarkerList.add(annotation);

        // Draw the annotation
        canvas.drawCircle(annotation.position.x, annotation.position.y, annotation.radius, mAnnotationPaint);
    }

    /**
     * Initialize the scale of the graph in both dimensions.
     *
     * @param xMin lowest x value on chart
     * @param xMax  highest x value on chart
     * @param dx    distance between x labels
     * @param yMin  lowest y value on chart
     * @param yMax  highest y value on chart
     * @param dy    distance between y labels
     */
    private void setAxisValues(float xMin, float xMax, float dx, float yMin, float yMax, float dy){
        mXmin = xMin;
        mXmax = xMax;
        mDx = dx;
        mYmin = yMin;
        mYmax = yMax;
        mDy = dy;

        mDxm=mDx;
        mDym=mDy;
    }

    /**
     * Initialize the scale of the graph in both dimensions with minor tickmarks
     *
     * @param xMin lowest x value on chart
     * @param xMax  highest x value on chart
     * @param dx    distance between x labels
     * @param dxm    distance between x tickmarks
     * @param yMin  lowest y value on chart
     * @param yMax  highest y value on chart
     * @param dy    distance between y labels
     * @param dym    distance between y tickmarks
     */
    private void setAxisValues(float xMin, float xMax, float dx, float dxm, float yMin, float yMax, float dy, float dym){
        mXmin = xMin;
        mXmax = xMax;
        mDx = dx;
        mDxm= dxm;
        mYmin = yMin;
        mYmax = yMax;
        mDy = dy;
        mDym=dym;
    }


    private void drawEllipse(Canvas canvas, int x0, int y0, int width, int height) {
        ShapeDrawable drawable = new ShapeDrawable(new OvalShape());
        drawable.getPaint().setColor(mShapeColor);
        drawable.setBounds(valueToCanvasX(x0), valueToCanvasY(y0),
                valueToCanvasX(x0) + width,
                valueToCanvasY(y0) + height);
        drawable.draw(canvas);
    }


    private void drawAbsPosRectangle(Canvas canvas, int x0, int y0, int width, int height) {
        ShapeDrawable bar = new ShapeDrawable(new RectShape());
        bar.getPaint().setColor(mBarColor);
        int x1 = valueToCanvasX(x0);
        int y1 = valueToCanvasY(y0);
        int x2 = (int)(valueToCanvasX(x0) + width*mAlphaX);
        int y2 = (int)(valueToCanvasY(y0) + height*mAlphaY);
        bar.setBounds( x1, (y1<y2?y1:y2) , x2, (y2>y1?y2:y1));
        bar.draw(canvas);
    }

    private void drawLineSegment(Canvas canvas, int x0, int y0, int x1, int y1) {
        Paint functionLinePaint = new Paint();
        functionLinePaint.setColor(mLineColor);
        functionLinePaint.setStrokeWidth(mLineWidth);
        canvas.drawLine(valueToCanvasX(x0), valueToCanvasY(y0), valueToCanvasX(x1), valueToCanvasY(y1), functionLinePaint);
    }


    private void drawPolyLine(Canvas canvas, Point[] points, int color) {
        Paint polyLinePaint = new Paint();
        polyLinePaint.setColor(color);
        polyLinePaint.setStrokeWidth(8);
        polyLinePaint.setStyle(Paint.Style.STROKE);

        Path polyLinePath = new Path();
        polyLinePath.moveTo(valueToCanvasX(points[0].x), valueToCanvasY(points[0].y));
        for (int i = 1; i < points.length; i++) {
            polyLinePath.lineTo(valueToCanvasX(points[i].x), valueToCanvasY(points[i].y));
        }
        canvas.drawPath(polyLinePath, polyLinePaint);

    }

    private void drawPolyLine(Canvas canvas, ArrayList<PointF> points, int color) {
        Paint polyLinePaint = new Paint();
        polyLinePaint.setColor(color);
        polyLinePaint.setStrokeWidth(8);
        polyLinePaint.setStyle(Paint.Style.STROKE);

        Path polyLinePath = new Path();
        polyLinePath.moveTo(valueToCanvasX(points.get(0).x), valueToCanvasY(points.get(0).y));
        for (int i = 1; i < points.size(); i++) {
            polyLinePath.lineTo(valueToCanvasX(points.get(i).x), valueToCanvasY(points.get(i).y));
        }
        canvas.drawPath(polyLinePath, polyLinePaint);
    }


        private void drawPolygon(Canvas canvas, Point[] points, int color) {
        Paint polyPaint = new Paint();
        polyPaint.setColor(color);
        polyPaint.setStyle(Paint.Style.FILL);

        Path polygonPath = new Path();
        polygonPath.moveTo(valueToCanvasX(points[0].x), valueToCanvasY(points[0].y));
        for (int i = 1; i < points.length; i++) {
            polygonPath.lineTo(valueToCanvasX(points[i].x), valueToCanvasY(points[i].y));
        }
        polygonPath.close();

        canvas.drawPath(polygonPath, polyPaint);
    }

    private void drawPolygon(Canvas canvas, PointF[] points, int color) {
        Paint polyPaint = new Paint();
        polyPaint.setColor(color);
        polyPaint.setAlpha(50);
        polyPaint.setStyle(Paint.Style.FILL);

        Path polygonPath = new Path();
        polygonPath.moveTo(valueToCanvasX(points[0].x), valueToCanvasY(points[0].y));
        for (int i = 1; i < points.length; i++) {
            polygonPath.lineTo(valueToCanvasX(points[i].x), valueToCanvasY(points[i].y));
        }
        polygonPath.close();

        canvas.drawPath(polygonPath, polyPaint);
    }




    private void drawBarList(Canvas canvas, int[] sizes) {
        int x=MARGIN;
        int y = valueToCanvasY(10);
        int canvas_width = (mCanvasMaxX-mCanvasMinX);
        int n= sizes.length;
        int width = canvas_width/n;
        int bar_width = (int) (width*(100-mBarGapPercentage)/100);
        x = x+(int)(width*mBarGapPercentage/100/2);
        for (int i=0;i<n;i++) {
            drawCanvasRectangle(canvas, x+(i*width), y, bar_width, (int)(sizes[i]*mAlphaY));
        }
    }

    private void drawCanvasRectangle(Canvas canvas, int x0, int y0, int width, int height) {
        ShapeDrawable bar = new ShapeDrawable(new RectShape());
        bar.getPaint().setColor(mBarColor);
        int x1 = x0;
        int y1 = y0;
        int x2 = (int)(x0 + width);
        int y2 = (int)(y0 + height);
        bar.setBounds( x1, (y1<y2?y1:y2) , x2, (y2>y1?y2:y1));
        bar.draw(canvas);
    }

    public float getLineWidth() { return mLineWidth; }

    public void setLineWidth(float lineWidth) {
        mLineWidth = lineWidth;
    }

    public int getLineColor() { return mLineColor; }

    public void setLineColor(int lineColor) {
        mLineColor = lineColor;
    }

    public int getShapeColor() { return mShapeColor; }

    public void setShapeColor(int shapeColor) {
        mShapeColor = shapeColor;
    }



    private void drawAxisSystem(Canvas canvas) {

        // X-Axis
        canvas.drawLine(mCanvasMinX, (int)mY0, mCanvasMaxX, (int)mY0 , mAxisLinePaint);
        for (float x = mXmin; x<= mXmax; x+= mDxm) {
            int xC= valueToCanvasX(x);
            canvas.drawLine((int)xC, (int)(mY0+7), xC, (int)mY0 , mAxisLinePaint);
        }
        for (float x = mXmin; x<= mXmax; x+= mDx) {
            int xC= valueToCanvasX(x);
            canvas.drawLine((int)xC, (int)(mY0+20), xC, (int)mY0 , mAxisLinePaint);
            if (x!=0)
               drawTextCentered(String.format(Locale.ENGLISH, "%.1f",x),xC, (int) (mY0+20+mFontSize*.667), mAxisLinePaint, canvas);
        }

        // Y-Axis
        canvas.drawLine((int)mX0, mCanvasMinY, (int)mX0, mCanvasMaxY, mAxisLinePaint);
        for (float y = mYmin; y<= mYmax; y+= mDym) {
            int yC= valueToCanvasY(y);
            canvas.drawLine((int)(mX0-7), yC, (int)mX0, yC , mAxisLinePaint);
        }
        for (float y = mYmin; y<= mYmax; y+= mDy) {
            int yC= valueToCanvasY(y);
            canvas.drawLine((int)(mX0-20), yC, (int)mX0, yC , mAxisLinePaint);
            if (y!=0)
                drawTextRightJustified(String.format(Locale.ENGLISH, "%.1f",y), (int)(mX0-20-mFontSize/2), yC, mAxisLinePaint, canvas);
        }
    }


    /**
     * Map real Y value to canvas value.
     *
     * @param y value
     * @return equivalent canvas y value
     */
    private int valueToCanvasY(float y) {
        return (int)(mAlphaY*y+mY0);
    }



    /**
     * Map real X value to canvas value.
     *
     * @param x value
     * @return equivalent canvas x value
     */
    private int valueToCanvasX(float x) {
        return (int)(mAlphaX*x+mX0);
    }


    private void drawTextCentered(String text, int x, int y, Paint paint, Canvas canvas) {
        int xPos = x - (int)(paint.measureText(text)/2);
        int yPos = (int) (y - ((paint.descent() + paint.ascent()) / 2)) ;

        canvas.drawText(text, xPos, yPos, paint);
    }


    private void drawTextRightJustified(String text, int x, int y, Paint paint, Canvas canvas) {
        int xPos = x - (int)(paint.measureText(text));
        int yPos = (int) (y - ((paint.descent() + paint.ascent()) / 2)) ;

        canvas.drawText(text, xPos, yPos, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float pointX = event.getX();
        float pointY = event.getY();
        // Checks for the event that occurs
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                // Test if the click is on an annotation, and if there are anything to show at this point
                Log.d(TAG, String.format(Locale.ENGLISH, "onTouchEvent: on (%f, %f)", pointX, pointY));
                for (Annotation a : mMarkerList) {
                    if (Math.hypot(pointX-a.position.x, pointY-a.position.y)<=a.radius){
                        Log.d(TAG, "onTouchEvent: We got a hit on the annotation");
                    }
                };
                return true;
            case MotionEvent.ACTION_MOVE:
//                path.lineTo(pointX, pointY);
                break;
            default:
                return false;
        }
        // Force a view to draw again
        postInvalidate();
        return true;
    }

    /**
     * Convert dp to pixels.
     *
     * @param context [Context]
     * @param dp [float] Size in dp
     * @return   [float] equivalent size in pixels on screen
     */
    public static float convertDpToPixel(Context context, float dp){
        Resources r = context.getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

    /**
     * Convert pixels to dp.
     *
     * @param context [Context]
     * @param px [float] Size in pixels
     * @return   [float] equivalent size in device independent pixels
     */
    public static float convertPixelsToDp(Context context, float px){
        Resources r = context.getResources();
        float dp = px / (r.getDisplayMetrics().densityDpi / 160f);
        return dp;
    }

    public class Annotation {
        private float radius;
        private PointF position;

        public Annotation(PointF pos, float rad) {
            radius=rad;
            position=pos;
        }
    }
}
