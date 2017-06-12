package dk.brams.android.axistest1;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.view.View;

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

    // Variables used for canvas dimensions
    private int mCanvasMinX;
    private int mCanvasMaxX;
    private int mCanvasMinY;
    private int mCanvasMaxY;

    // Variables used for axis values
    private int mXmin;
    private int mXmax;
    private int mYmin;
    private int mYmax;
    private int mDx;
    private int mDy;

    // Variables used for transformation
    private double mAlphaX;
    private double mX0;
    private double mAlphaY;
    private double mY0;

    // various settings for graphics
    private float mLineWidth=10;
    private int   mLineColor=0xffee00ee;
    private int   mShapeColor=0xff74AC23;

    private int   mBarColor=0x77740074;
    private float mBarGapPercentage=10;

    private final Paint mAxisLinePaint;
    private float mFontSize;

    public CustomDrawableView(Context context) {
        super(context);


        // Initialize axis values
        setAxisValues(-50, 25, 25, -30, 50, 10);

        // Initialize the graphics style for the axis and tick marks
        mAxisLinePaint = new Paint();
        mAxisLinePaint.setColor(0xff0000ee);
        mAxisLinePaint.setStrokeWidth(5);

        // Set the font size for the axis values
        Resources res = getResources();
        mFontSize = res.getDimension(R.dimen.axis_text_size);
        mAxisLinePaint.setTextSize(mFontSize);
//        mAxisLinePaint.setTextAlign(Paint.Align.CENTER);

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
        mX0 = mCanvasMaxX-mAlphaX* mXmax;

        mAlphaY = (double)(mCanvasMaxY-mCanvasMinY)/(mYmax - mYmin);
        mY0 = mCanvasMaxY-mAlphaY* mYmax;

    }



    protected void onDraw(Canvas canvas) {

        // draw an ellipse
        setShapeColor(0x55005555);
        drawEllipse(canvas, -25, -10, 500, 300);

        // Draw axis system
        drawAxisSystem(canvas);

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



    /**
     * Initialize the scale of the graph in both dimensions.
     *
     * @param xMin lowest x value on chart
     * @param xMax  highest x value on chart
     * @param dx    distance between x tickmarks
     * @param yMin  lowest y value on chart
     * @param yMax  highest y value on chart
     * @param dy    distance between y tickmarks
     */
    private void setAxisValues(int xMin, int xMax, int dx, int yMin, int yMax, int dy){
        mXmin = xMin;
        mXmax = xMax;
        mDx = dx;
        mYmin = yMin;
        mYmax = yMax;
        mDy = dy;
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
        for (int x = mXmin; x<= mXmax; x+= mDx) {
            int xC= valueToCanvasX(x);
            canvas.drawLine(xC, (int)(mY0+20), xC, (int)mY0 , mAxisLinePaint);
            if (x!=0)
               drawTextCentered(String.format("%d",x),xC, (int) (mY0+20+mFontSize*.667), mAxisLinePaint, canvas);
        }

        // Y-Axis
        canvas.drawLine((int)mX0, mCanvasMinY, (int)mX0, mCanvasMaxY, mAxisLinePaint);
        for (int y = mYmin; y<= mYmax; y+= mDy) {
            int yC= valueToCanvasY(y);
            canvas.drawLine((int)(mX0-20), yC, (int)mX0, yC , mAxisLinePaint);
            if (y!=0)
                drawTextRightJustified(String.format("%d",y), (int)(mX0-20-mFontSize/2), yC, mAxisLinePaint, canvas);
        }
    }


    /**
     * Map real Y value to canvas value.
     *
     * @param y value
     * @return equivalent canvas y value
     */
    private int valueToCanvasY(int y) {
        return (int)(mAlphaY*y+mY0);
    }



    /**
     * Map real X value to canvas value.
     *
     * @param x value
     * @return equivalent canvas x value
     */
    private int valueToCanvasX(int x) { return (int)(mAlphaX*x+mX0); }


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

}
