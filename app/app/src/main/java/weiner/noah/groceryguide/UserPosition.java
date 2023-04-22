package weiner.noah.groceryguide;

import java.io.Serializable;

/**
 * Representation of the user's lat, lon position.
 */
public class UserPosition implements Serializable {
    private final double lon, lat;

    private int floor;

    private float accuracy;
    private String provider;

    public UserPosition(double latitude, double longitude) {
        this.lon = longitude;
        this.lat = latitude;
    }

    public UserPosition(double latitude, double longitude, int floor) {
        this.lon = longitude;
        this.lat = latitude;
        this.floor = floor;
    }

    public UserPosition(String[] posStr) {
        this.lon = Double.parseDouble(posStr[1]);
        this.lat = Double.parseDouble(posStr[0]);
    }

    public int getFloor() {
        return floor;
    }


    public double getLat() {
        return lat;
    }


    public double getLon() {
        return lon;
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

    public void setFloor(int floor) {
        this.floor = floor;
    }
}
