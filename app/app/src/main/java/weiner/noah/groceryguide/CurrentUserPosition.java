package weiner.noah.groceryguide;


/**
 * Represents the CURRENT user position (that's why it's static)
 */
public class CurrentUserPosition {
    private static double currLon = 0;
    private static double currLat = 0;

    public static double getCurrLat() {
        return currLat;
    }

    public static double getCurrLon() {
        return currLon;
    }

    public static void setCurrLon(double currLon) {
        CurrentUserPosition.currLon = currLon;
    }

    public static void setCurrLat(double currLat) {
        CurrentUserPosition.currLat = currLat;
    }

    public static void setPos(UserPosition ClientPos) {
        CurrentUserPosition.currLon = ClientPos.getLon();
        CurrentUserPosition.currLat = ClientPos.getLat();
    }
}
