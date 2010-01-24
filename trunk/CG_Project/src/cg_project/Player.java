/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cg_project;

/**
 *
 * @author christiandernehl
 */
public class Player {
    
    private int points = 0;
    
    
    
    public Player(){
        
    }
    
    
    void addPoints(int points){
        this.points += points;
    }

    int getPoints(){
        return points;
    }
}
