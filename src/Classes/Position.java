package Classes;

import java.io.Serializable;

public class Position implements Serializable {
    int x;
    int y;

    public Position(){
        this.x = 0;
        this.y = 0;
    }

    public Position(int cX, int cY){
        this.x = cX;
        this.y = cY;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }
}
