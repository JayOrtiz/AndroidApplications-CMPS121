package com.example.will.thedrain;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

/**
 * Created by will on 5/28/15.
 */
public class Screen extends View {

    private static final String LOG_TAG = "MainActivity";

    int screenWidth = getWidth();
    int screenHeight = getHeight();

    //bounds of the rectangle holding the drain
    float drainX1;
    float drainX2;
    float drainY1;
    float drainY2;
    float drainWidth;
    float drainHeight;

    //bounds of the hole
    float drainHoleX;
    float drainHoleY;
    float drainRad = 75;

    //variables for extra walls, first/secondWallStart refer to the y positions of each wall
    //wallWidth is an X value, later set to 1/2 the screen size
    float firstWallStart;
    float firstWallEnd;
    float secondWallStart;
    float secondWallEnd;
    float wallWidth;

    Tomato tomato;

    float reboundVal = 1;

    private Runnable animator = new Runnable() {
        @Override
        public void run() {
            physics();

            invalidate();
            postDelayed(this, 20);
        }
    };

    private void physics(){

        tomato.addVelX(MainActivity.accelX/2);
        tomato.addVelY(MainActivity.accelY/2);

        tomato.setX(tomato.getxPos() + tomato.getxVel());
        tomato.setY(tomato.getyPos() + tomato.getyVel());

        //Log.i(LOG_TAG, "velX = " + tomato.getxVel() + " velY = " + tomato.getyVel());
        //Log.i(LOG_TAG, "X = " + tomato.getxPos() + " velY = " + tomato.getyPos());

        //checks if the ball is within outer walls
        checkOuterBounds();

        //check if ball hits bounds of first inner wall
        topWallBounce();

        //check if ball hits bounds of second inner wall
        bottomWallBounce();

        //check if the tomato is in the drain hole with the following formula
        //(x - center_x)^2 + (y - center_y)^2 < radius^2
        double checkX = Math.pow(tomato.getxPos() - drainHoleX, 2) ;
        double checkY = Math.pow(tomato.getyPos() - drainHoleY, 2) ;
        //if the tomato is in the drain then move it back to the top
        if(checkX + checkY < Math.pow(drainRad,2)){
            tomato.changeCoords(250, 250);
            tomato.setVelX(0);
            tomato.setVelY(0);
        }


    }

    public void topWallBounce(){
        //firstWallStart is the y position of the top wall

        //check if the tomato is within the width of the wall
        if(tomato.getxPos() >= 0 && tomato.getxPos() <= wallWidth){
            //does a few checks to make sure that they are within bounds ehen tomato hit from top
            if(tomato.getyPos()+tomato.getRadius() > firstWallStart &&
                    tomato.getyPos()-tomato.getRadius() < firstWallStart && tomato.getyVel() > 0){
                //reverse the direction of the tomato when hitting the wall
                tomato.setVelY(tomato.getyVel()* -reboundVal);
                tomato.setY(firstWallStart - tomato.getRadius()-3);
            }
            //similar check for the bottom wall
            if(tomato.getyPos()-tomato.getRadius() < firstWallStart &&
                    tomato.getyPos()+tomato.getRadius() > firstWallStart && tomato.getyVel() < 0){
                tomato.setVelY(tomato.getyVel()* -reboundVal);
                tomato.setY(firstWallStart + tomato.getRadius()+3);
            }

        }
    }

    //similar to topWallBounce but for the bottom wall
    public void bottomWallBounce(){
        if(tomato.getxPos() >= wallWidth && tomato.getxPos() <= wallWidth*2){
            if(tomato.getyPos()+tomato.getRadius() > secondWallStart &&
                    tomato.getyPos()-tomato.getRadius() < secondWallStart && tomato.getyVel() > 0){
                tomato.setVelY(tomato.getyVel()* -reboundVal);
                tomato.setY(secondWallStart - tomato.getRadius()-3);
            }
            if(tomato.getyPos()-tomato.getRadius() < secondWallStart &&
                    tomato.getyPos()+tomato.getRadius() > secondWallStart && tomato.getyVel() < 0){
                tomato.setVelY(tomato.getyVel()* -reboundVal);
                tomato.setY(secondWallStart + tomato.getRadius()+3);
            }
        }
    }

    public void checkOuterBounds(){
        //makes the ball rebound when hitting the top edge
        if(tomato.getyPos() - tomato.getRadius() < drainY1){
            tomato.setY(drainY1 + tomato.getRadius());
            tomato.setVelY(tomato.getyVel()* -reboundVal);
        }

        //makes ball rebound when hitting bottom edge
        if(tomato.getyPos() + tomato.getRadius() > drainY2){
            tomato.setY(drainY2 - tomato.getRadius());
            tomato.setVelY(tomato.getyVel()* -reboundVal);
        }

        //makes ball rebound when hitting left edge
        if(tomato.getxPos() - tomato.getRadius() < drainX1){
            tomato.setX(drainX1 + tomato.getRadius());
            tomato.setVelX(tomato.getxVel()* -reboundVal);
        }

        //makes ball rebound when hitting right edge
        if(tomato.getxPos() + tomato.getRadius() > drainX2){
            tomato.setX(drainX2 - tomato.getRadius());
            tomato.setVelX(tomato.getxVel()* -reboundVal);
        }
    }

    public void animateScreen(){
        post(animator);
    }

    private Paint paint= new Paint();

    public Screen(Context context){
        super(context);
        init();
    }

    private void init(){

        //Sets paint to for the borders
        setBorderPaint();

        tomato = new Tomato(250, 250, 30);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        //a bunch of stuff from the slides
        int desiredWidth = 100;
        int desiredHeight = 100;
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int width;
        int height;
        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(desiredWidth, widthSize);
        } else {
            width = desiredWidth;
        }
        //Measure Height…
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(desiredHeight, heightSize);
        } else {
            height = desiredHeight;
        }
        //MUST CALL THIS
        setMeasuredDimension(width, height);
        //Log.i(LOG_TAG, "Width (onMeasure) = " + width + " Height (onMeasure) = " + height);
        screenHeight = height;
        screenWidth = width;

    }

    private void setBorderPaint(){
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(160);
        paint.setStrokeWidth(5);
    }

    private void setTomatoPaint(){
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.RED);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(160);
        paint.setStrokeWidth(5);
    }

    protected void onDraw(Canvas canvas) {
        //Log.i(LOG_TAG, "Width = " + screenWidth + " Height = " + screenHeight);

        setBorderPaint();
        drawBounds(canvas);

        setBorderPaint();
        drawDrain(canvas);

        setTomatoPaint();
        drawTomato(canvas);
    }

    private void drawTomato(Canvas canvas){
        canvas.drawCircle(tomato.getxPos(), tomato.getyPos(), tomato.getRadius(), paint);
    }

    private void drawDrain(Canvas canvas){
        drainHoleX = screenWidth/2;
        drainHoleY = screenHeight/5*4;

        canvas.drawCircle(drainHoleX, drainHoleY, drainRad, paint);
    }

    private void drawBounds(Canvas canvas){
        setDrainBounds();

        //Log.i(LOG_TAG, "X1 = " + drainX1 + " X2 = " + drainX2 + " Y1 = " + drainY1 + " Y2 = " + drainY2);

        //draw outer walls
        canvas.drawLine(drainX1, drainY1, drainX2, drainY1, paint);
        canvas.drawLine(drainX2, drainY1, drainX2, drainY2, paint);
        canvas.drawLine(drainX2, drainY2, drainX1, drainY2, paint);
        canvas.drawLine(drainX1, drainY2, drainX1, drainY1, paint);

        //draw inner walls
        canvas.drawLine(drainX1, firstWallStart, wallWidth, firstWallStart, paint);
        canvas.drawLine(drainX2, secondWallStart, wallWidth, secondWallStart, paint);
    }

    //will set the position of the rectangle,drain, and inner walls
    private void setDrainBounds(){
        drainX1 = screenWidth/8;
        drainX2 = screenWidth/8*7;
        drainY1 = screenHeight/10;
        drainY2 = screenHeight/10*9;
        drainHeight = drainY2 - drainY1;
        drainWidth = drainX2 -drainX1;

        //set location for inner walls
        wallWidth = screenWidth/2;
        firstWallStart = drainY1 + (drainHeight/3);
        secondWallStart = drainY2 - (drainHeight/3);
    }

}
