package weiner.noah.groceryguide;

/**
 * Represents the CURRENT user position (that's why it's static)
 */
public class CurrentUserPosition {
    private static double currXPos = 0;
    private static double currYPos = 0;

    public static double getCurrYPos() {
        return currYPos;
    }

    public static double getCurrXPos() {
        return currXPos;
    }

    public static void setCurrXPos(double currXPos) {
        CurrentUserPosition.currXPos = currXPos;
    }

    public static void setCurrYPos(double currYPos) {
        CurrentUserPosition.currYPos = currYPos;
    }

    public static void setPos(UserPosition pos) {
        CurrentUserPosition.currXPos = pos.getXPos();
        CurrentUserPosition.currYPos = pos.getYPos();
    }
}
