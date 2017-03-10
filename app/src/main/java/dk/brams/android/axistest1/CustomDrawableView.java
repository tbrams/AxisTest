package dk.brams.android.axistest1;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.view.View;

public class CustomDrawableView extends View {
    public static final String TAG = "TBR:CustomDrawableView";
    private ShapeDrawable mDrawable;

    /**
     * First attempt to make a scalable axis display.  Will do proper setup later, but using
     * hardcoding at this time...
     *
     *
     * The settings below are the limits on the coordinate system and the step size on each axis.
     */

    private int mCanvasMinX;
    private int mCanvasMaxX;
    private int mCanvasMinY;
    private int mCanvasMaxY;

    private int mXmin;
    private int mXmax;
    private int mYmin;
    private int mYmax;
    private int mDx;
    private int mDy;

    private double mAlphaX;
    private double mX0;
    private double mAlphaY;
    private double mY0;



    private Paint mLinePaint;
    private float mFontSize;


    public CustomDrawableView(Context context) {
        super(context);


        /**
         * This is a drawable with an ellipse...
         * Just to have something to plot besides axis system.
         *
         * Notice scale is not related to axis values here
         */

        int x = 10;
        int y = 10;
        int width = 300;
        int height = 500;

        mDrawable = new ShapeDrawable(new OvalShape());
        mDrawable.getPaint().setColor(0xff74AC23);
        mDrawable.setBounds(x, y, x + width, y + height);


        // Initialize axis values
        setAxisValues(-50, 25, 25, -30, 5, 5);

        // Initialize the graphics style for the axis and tick marks
        mLinePaint = new Paint();
        mLinePaint.setColor(0xff0000ee);
        mLinePaint.setStrokeWidth(5);

        Resources res = getResources();
        mFontSize = res.getDimension(R.dimen.axis_text_size);
        mLinePaint.setTextSize(mFontSize);
//        mLinePaint.setTextAlign(Paint.Align.CENTER);


    }


    protected void onDraw(Canvas canvas) {

        // Plot whatever is defined  above in the drawable as well
        mDrawable.draw(canvas);

        // The overlay the axis system
        initializePlotArea(canvas);
        drawAxisSystem(canvas);
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


    /**
     * Will initialize the canvas parameters based on canvas size.
     *
     * @param canvas
     */
    private void initializePlotArea(Canvas canvas) {

        int height= canvas.getHeight();
        int width = canvas.getWidth();

        // Define the canvas coordinate limits so that we leave a margin around the graph
        int margin = 10;

        mCanvasMinX = margin;
        mCanvasMaxX = width-margin-margin;

        mCanvasMinY = height-margin-margin;
        mCanvasMaxY = margin;


        // Define parameters for converting values to canvas equivalents
        mAlphaX = (mCanvasMaxX-mCanvasMinX)/(mXmax - mXmin);
        mX0 = mCanvasMaxX-mAlphaX* mXmax;

        mAlphaY = (double)(mCanvasMaxY-mCanvasMinY)/(mYmax - mYmin);
        mY0 = mCanvasMaxY-mAlphaY* mYmax;


    }


    private void drawAxisSystem(Canvas canvas) {

        // X-Axis
        canvas.drawLine(mCanvasMinX, (int)mY0, mCanvasMaxX, (int)mY0 ,mLinePaint);
        for (int x = mXmin; x<= mXmax; x+= mDx) {
            int xC= valueToCanvasX(x);
            canvas.drawLine(xC, (int)(mY0+20), xC, (int)mY0 ,mLinePaint);
            if (x!=0)
               drawTextCentered(String.format("%d",x),xC, (int) (mY0+20+mFontSize*.667), mLinePaint, canvas);
        }

        // Y-Axis
        canvas.drawLine((int)mX0, mCanvasMinY, (int)mX0, mCanvasMaxY,mLinePaint);
        for (int y = mYmin; y<= mYmax; y+= mDy) {
            int yC= valueToCanvasY(y);
            canvas.drawLine((int)(mX0-20), yC, (int)mX0, yC ,mLinePaint);
            if (y!=0)
                drawTextRightJustified(String.format("%d",y), (int)(mX0-20-mFontSize/2), yC, mLinePaint, canvas);
        }
    }


    /**
     * Map real Y value to canvas value.
     *
     * @param y value from setup
     * @return equivalent canvas y value
     */
    private int valueToCanvasY(int y) {
        return (int)(mAlphaY*y+mY0);
    }



    /**
     * Map real X value to canvas value.
     *
     * @param x value from setup
     * @return equivalent canvas x value
     */
    private int valueToCanvasX(int x) {

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

}
