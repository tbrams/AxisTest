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

    private int xMin=-50;
    private int xMax=25;
    private int yMin=-30;
    private int yMax=5;
    private int dX=25;
    private int dY=5;
    private Paint mLinePaint;
    private float mFontSize;


    public CustomDrawableView(Context context) {
        super(context);

        /**
         * This is just to have something on the canvas - here is an ellipse...
         */

        int x = 10;
        int y = 10;
        int width = 300;
        int height = 500;

        mDrawable = new ShapeDrawable(new OvalShape());
        mDrawable.getPaint().setColor(0xff74AC23);
        mDrawable.setBounds(x, y, x + width, y + height);

        /**
         * This is the graphics style for the axis and tickmarks
         */
        mLinePaint = new Paint();
        mLinePaint.setColor(0xff0000ee);
        mLinePaint.setStrokeWidth(5);

        Resources res = getResources();
        mFontSize = res.getDimension(R.dimen.axis_text_size);
        mLinePaint.setTextSize(mFontSize);
//        mLinePaint.setTextAlign(Paint.Align.CENTER);


    }


    protected void onDraw(Canvas canvas) {

        /**
         * Attempt to scale the coordinate system to aling with the available canvas size
         */

        int height= canvas.getHeight();
        int width = canvas.getWidth();

        mDrawable.draw(canvas);


       // Draw axis lines without scale on canvas - 10 is the margin we want to use here
        // so the canvas coordinate limits are as follows:
        int xCanvasMin=10;
        int xCanvasMax=width-10-10;

        int yCanvasMin = height-10-10;
        int yCanvasMax = 10;

        // X-Axis
        double xAlpha = (xCanvasMax-xCanvasMin)/(xMax-xMin);
        double x0 = xCanvasMax-xAlpha*xMax;

        double yAlpha = (double)(yCanvasMax-yCanvasMin)/(yMax-yMin);
        double y0 = yCanvasMax-yAlpha*yMax;

        canvas.drawLine(xCanvasMin, (int)y0, xCanvasMax, (int)y0 ,mLinePaint);
        for (int x=xMin; x<=xMax; x+=dX) {
            int xC=(int)(xAlpha*x+x0);
            canvas.drawLine(xC, (int)(y0+20), xC, (int)y0 ,mLinePaint);
            if (x!=0)
               drawTextCentered(String.format("%d",x),xC, (int) (y0+20+mFontSize*.667), mLinePaint, canvas);
        }

        // Y-Axis
        canvas.drawLine((int)x0, yCanvasMin, (int)x0, yCanvasMax,mLinePaint);
        for (int y=yMin; y<=yMax; y+=dY) {
            int yC=(int)(yAlpha*y+y0);
            canvas.drawLine((int)(x0-20), yC, (int)x0, yC ,mLinePaint);
            if (y!=0)
                drawTextRightJustified(String.format("%d",y), (int)(x0-20-mFontSize/2), yC, mLinePaint, canvas);
        }

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
