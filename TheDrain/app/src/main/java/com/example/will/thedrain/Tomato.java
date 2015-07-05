package com.example.will.thedrain;

/**
 * Created by will on 5/28/15.
 */

//code for the tomato class
//basically a bunch of variables with respective getters and setters
public class Tomato {
    private float x;
    private float y;
    private float velX;
    private float velY;
    private float radius;

    public Tomato(){
        x = 0;
        y = 0;
        velX = 0;
        velY = 0;
        radius = 3;
    }

    public Tomato(float initX, float initY, float rad){
        x = initX;
        y = initY;
        velX = 0;
        velY = 0;
        radius = rad;
    }

    public void changeCoords(float newX, float newY){
        x = newX;
        y = newY;
    }

    public void setY(float newY){
        y = newY;
    }

    public void setX(float newX){
        x = newX;
    }

    public float getxPos(){
        return x;
    }

    public float getyPos(){
        return y;
    }

    public float getxVel(){
        return velX;
    }

    public float getyVel(){
        return velY;
    }

    public float getRadius(){
        return radius;
    }

    public void setVelX(float newVelX){ velX = newVelX; }

    public void setVelY(float newVelY){ velY = newVelY; }

    public void addVelX(float newVelX){ velX += newVelX; }

    public void addVelY(float newVelY){ velY += newVelY; }
}
