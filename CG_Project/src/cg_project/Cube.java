/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cg_project;

import org.lwjgl.util.vector.Vector3f;

/**
 *
 * @author christiandernehl
 */
public class Cube {
    private Vector3f center;
    private Vector3f initalPosition;
    private float size = 1;
    private float[] color;
    
    
    public Cube(){
        center = new Vector3f(0,0,0);
        this.initalPosition = new Vector3f(center);
    }
    
    public Cube(Vector3f center){
        this.center = center;
        this.initalPosition = new Vector3f(center);
    }
    
    public Cube(Vector3f center, float size){
        this.center = center;
        this.initalPosition = new Vector3f(center);
        this.size = size;
    }

    public Cube(Vector3f center, float size, float[] color){
        this.center = center;
        this.initalPosition = new Vector3f(center);
        this.size = size;
        this.color = color.clone();
    }    
    
    public Vector3f getCenter(){
        return center;
    }
    
    public float getSize(){
        return size;
    }
    
    public float[] getColor(){
        return color;
    }
    
    public void setToInitialPosition(){
        this.center = new Vector3f(initalPosition);
    }
    
    public void setCenter(Vector3f center){
        this.center = center;
    }
    
    public void setSize(float size){
        this.size = size;
    }
    
    public void setColor(float[] color){
        this.color = color.clone();
    }
    
    
}
