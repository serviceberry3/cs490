package weiner.noah.groceryguide;

import java.io.Serializable;

/**
 * Representation of the user's x, y position (not necessarily current).
 */
public class UserPosition implements Serializable {
    private final double xPos, yPos;

    private float accuracy;
    private String provider;

    public UserPosition(double x, double y) {
        this.xPos = x;
        this.yPos = y;
    }

    public UserPosition(String[] posStr) {
        this.xPos = Double.parseDouble(posStr[1]);
        this.yPos = Double.parseDouble(posStr[0]);
    }

    public double getYPos() {
        return yPos;
    }


    public double getXPos() {
        return xPos;
    }


    public float getAccuracy() {
        return accuracy;
    }


    public String getProvider() {
        return provider;
    }


    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }


    public void setProvider(String provider) {
        this.provider = provider;
    }
}
