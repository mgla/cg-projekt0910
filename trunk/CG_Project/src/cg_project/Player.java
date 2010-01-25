package cg_project;

/**
 * @author Maik Glatki, Christian Dernehl, Dominic Gatzen
 */
public class Player {
    
    private int points = 0;
    
    public Player(){
        
    }
    
    public int getPoints()
    {
    	return this.points;
    }
    
    void addPoints(int points){
        this.points += points;
    }
}
