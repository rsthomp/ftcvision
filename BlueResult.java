package ftc.vision;

/**
 * Created by robotics on 1/5/2016.
 */
public class BlueResult {
    private int x;
    private int y;

    public BlueResult(int blobX, int blobY){
        x = blobX;
        y = blobY;
    }

    public int getX(){return x;}
    public int getY(){return y;}

    @Override
    public String toString(){
        return x + ", " + y;
    }

}
