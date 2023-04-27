package weiner.noah.groceryguide;


import android.graphics.RectF;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.CompletionStage;

public class SSWhalley {
    private final ArrayList<StoreElement> lineList = new ArrayList<>();
    private final ArrayList<StoreElement> rectList = new ArrayList<>();

    private final float aisleFrozen16Left = 16f * Constants.cellWidth;
    private final float produceIslandsLeft = 920f;

    public SSWhalley() {
        //the outer box for the store
        rectList.add(new StoreElement(new RectF(0f, 0f, Constants.mapFrameRectWidth, Constants.mapFrameRectHeight), 0.0f, "frame", "h"));

        //entrance, exit
        rectList.add(new StoreElement(new RectF(800f, 860f, 800f+90f, 900f), 0.00f, "entrance", "h"));
        //random things directly to left when walk into store
        rectList.add(new StoreElement(new RectF(760f, 840f, 760f+60f, 840f+20f), -90f, "entrance_island", "v"));
        rectList.add(new StoreElement(new RectF(250f, 855f, 250+90f, 900f), 0.00f, "exit", "h"));

        //checkout area
        rectList.add(new StoreElement(new RectF(110f, 760f, 640f, 830f), 0.00f, "checkout", "h"));

        //old produce island
        rectList.add(new StoreElement(new RectF(700f, 800f, 700f+60f, 800f+20f), -45f, "old_produce_island", "h"));
        rectList.add(new StoreElement(new RectF(720.08f, 683.49f, 819.20f, 711.69f), 0.00f, "chips_wall_of_value", "h"));

        //all of the store elements inside

        //produce islands directly in front of entrance
        rectList.add(new StoreElement(new RectF(820f, 800f, 820f+60, 800f+15f), 30f, "produce_island_front_west", "h"));
        rectList.add(new StoreElement(new RectF(820f, 800f-50f, 820f+60, 800f+15f-50f), 30f, "produce_island_front_east", "h"));

        //floral island
        rectList.add(new StoreElement(new RectF(produceIslandsLeft, 800f, 900f+100f, 800f+25f), 0.00f, "floral_island", "h"));

        //main produce islands
        rectList.add(new StoreElement(new RectF(produceIslandsLeft, 750f, 900f+100f, 750f+25f), 0.00f, "produce_island_1", "h"));
        rectList.add(new StoreElement(new RectF(produceIslandsLeft, 680f, 900f+100f, 680+25f), 0.00f, "produce_island_2", "h"));
        rectList.add(new StoreElement(new RectF(produceIslandsLeft, 610f, 900f+100f, 610+25f), 0.00f, "produce_island_3", "h"));
        rectList.add(new StoreElement(new RectF(produceIslandsLeft, 540f, 900f+100f, 540+25f), 0.00f, "produce_island_4", "h"));
        rectList.add(new StoreElement(new RectF(produceIslandsLeft, 470f, 900f+100f, 470+25f), 0.00f, "produce_island_5", "h"));
        rectList.add(new StoreElement(new RectF(produceIslandsLeft, 400f, 900f+100f, 400+25f), 0.00f, "produce_island_6", "h"));

        //first produce wall
        rectList.add(new StoreElement(new RectF(1053.13f, 360f, 1079.24f, 798.84f), 0.00f, "produce_wall_west", "v"));
        rectList.add(new StoreElement(new RectF(1053.13f, 200f, 1079.24f, 350f), 0.00f, "produce_wall_east", "v"));

        //meat islands
        rectList.add(new StoreElement(new RectF(300f, 80f, 300f+100f, 80f+30f), 0.00f, "meat_island_north", "h"));
        rectList.add(new StoreElement(new RectF(460f, 80f, 460f+100f, 80f+30f), 0.00f, "meat_island_south", "h"));



        rectList.add(new StoreElement(new RectF(800f, 360f, 800f+30f, 607.44f), 0.00f, "deli", "v"));

        rectList.add(new StoreElement(new RectF(990f, 110f, 990f+100f, 110f+30f), 65f, "dips_wall", "h"));

        //stuff on back wall
        rectList.add(new StoreElement(new RectF(930f, 0f, 930f+100f, 30f), 0f, "cakes_fridge_wall", "h"));
        rectList.add(new StoreElement(new RectF(760f, 0f, 760f+160f, 30f), 0f, "bread_wall", "h"));


        rectList.add(new StoreElement(new RectF(700f, 0f, 700f+50f, 30f), 0f, "fish_wall", "h"));
        rectList.add(new StoreElement(new RectF(630f, 0f, 630f+70f, 30f), 0f, "fish_fridge_wall", "h"));



        rectList.add(new StoreElement(new RectF(360f, 0.38f, 600f, 30f), 0.00f, "wall_meat_packaged", "h"));
        rectList.add(new StoreElement(new RectF(27.81f, 2.27f, 350f, 30f), 0.00f, "wall_dairy_yog_milk", "h"));



        //begin the "aisles"
        rectList.add(new StoreElement(new RectF(0, 41.94f, Constants.aisleWidth, 697.81f), 0.00f, "wall_dairy_egg_cheese", "v"));
        rectList.add(new StoreElement(new RectF(Constants.aisleSpan, 133.59f, Constants.aisleSpan + Constants.aisleWidth, 695.54f), 0.00f, "aisle_ice_cream_frozen", "v"));
        rectList.add(new StoreElement(new RectF(aisleFrozen16Left, 137.93f, Constants.aisleWidth + aisleFrozen16Left, 696.99f), 0.00f, "aisle_frozen_16", "v"));
        rectList.add(new StoreElement(new RectF(aisleFrozen16Left + Constants.aisleSpan, 134.00f, aisleFrozen16Left + Constants.aisleSpan + Constants.aisleWidth, 700.27f), 0.00f, "aisle_16_15", "v"));
        rectList.add(new StoreElement(new RectF(aisleFrozen16Left + Constants.aisleSpan * 2, 130.72f, aisleFrozen16Left + Constants.aisleSpan * 2 + Constants.aisleWidth, 698.44f), 0.00f, "aisle_15_14", "v"));
        rectList.add(new StoreElement(new RectF(aisleFrozen16Left + Constants.aisleSpan * 3, 132.18f, aisleFrozen16Left + Constants.aisleSpan * 3 + Constants.aisleWidth, 702.79f), 0.00f, "aisle_14_13", "v"));
        rectList.add(new StoreElement(new RectF(aisleFrozen16Left + Constants.aisleSpan * 4, 130.72f, aisleFrozen16Left + Constants.aisleSpan * 4 + Constants.aisleWidth, 698.44f), 0.00f, "aisle_13_12", "v"));
        rectList.add(new StoreElement(new RectF(aisleFrozen16Left + Constants.aisleSpan * 5, 197.16f, aisleFrozen16Left + Constants.aisleSpan * 5 + Constants.aisleWidth, 701.34f), 0.00f, "aisle_12_11", "v"));
        rectList.add(new StoreElement(new RectF(aisleFrozen16Left + Constants.aisleSpan * 6, 194.27f, aisleFrozen16Left + Constants.aisleSpan * 6 + Constants.aisleWidth, 701.34f), 0.00f, "aisle_11_10", "v"));
        rectList.add(new StoreElement(new RectF(aisleFrozen16Left + Constants.aisleSpan * 7, 241.92f, aisleFrozen16Left + Constants.aisleSpan * 7 + Constants.aisleWidth, 696.99f), 0.00f, "aisle_10_9", "v"));
        rectList.add(new StoreElement(new RectF(aisleFrozen16Left + Constants.aisleSpan * 8, 189.93f, aisleFrozen16Left + Constants.aisleSpan * 8 + Constants.aisleWidth, 698.44f), 0.00f, "aisle_9_8", "v"));
        rectList.add(new StoreElement(new RectF(aisleFrozen16Left + Constants.aisleSpan * 9, 191.37f, aisleFrozen16Left + Constants.aisleSpan * 9 + Constants.aisleWidth, 698.44f), 0.00f, "aisle_8_7", "v"));
        rectList.add(new StoreElement(new RectF(aisleFrozen16Left + Constants.aisleSpan * 10, 166.28f, aisleFrozen16Left + Constants.aisleSpan * 10 + Constants.aisleWidth, 699.34f), 0.00f, "aisle_7_6", "v"));
        rectList.add(new StoreElement(new RectF(aisleFrozen16Left + Constants.aisleSpan * 11, 165.32f, aisleFrozen16Left + Constants.aisleSpan * 11 + Constants.aisleWidth, 681.98f), 0.00f, "aisle_6_5", "v"));
        rectList.add(new StoreElement(new RectF(aisleFrozen16Left + Constants.aisleSpan * 12, 191.37f, aisleFrozen16Left + Constants.aisleSpan * 12 + Constants.aisleWidth, 696.99f), 0.00f, "aisle_5_4", "v"));
        rectList.add(new StoreElement(new RectF(aisleFrozen16Left + Constants.aisleSpan * 13, 162.89f, aisleFrozen16Left + Constants.aisleSpan * 13 + Constants.aisleWidth, 681.05f), 0.00f, "aisle_4_3", "v"));
        rectList.add(new StoreElement(new RectF(aisleFrozen16Left + Constants.aisleSpan * 14, 162.75f, aisleFrozen16Left + Constants.aisleSpan * 14 + Constants.aisleWidth, 669.30f), 0.00f, "aisle_3_deli", "v"));



        rectList.add(new StoreElement(new RectF(880f, 110f, 880+80f, 110f+30f), 50.75f, "bread_island", "h"));

        rectList.add(new StoreElement(new RectF(700f, 160f, 700f+40f, 160f+30f), 0.00f, "grabngo", "h"));

        //breakfast (bacon etc)
        rectList.add(new StoreElement(new RectF(700f, 250f, 700f+150f, 250f+30f), 70f, "breakfast", "h"));

        //cheese island
        rectList.add(new StoreElement(new RectF(820f, 140f, 820f+80f, 140+30f), 50.75f, "cheese_island", "h"));

        //grab and go deli island
        rectList.add(new StoreElement(new RectF(850f, 270f, 850f+60f, 270+30f), 0.00f, "grabngo_deli_island", "h"));

        //pastries/bakery islands
        rectList.add(new StoreElement(new RectF(980f, 290f, 980+40f, 290+30f), 0.00f, "pastries_south", "h"));
        rectList.add(new StoreElement(new RectF(940f, 240f, 940f+40f, 240+30f), 0.00f, "pastries_north", "h"));

        rectList.add(new StoreElement(new RectF(0, 750f, Constants.aisleWidth, 850f), 0.00f, "alcohol", "v"));

        //stuff on front wall
        rectList.add(new StoreElement(new RectF(0f, 870f, 0+90f, 900f), 0.00f, "alcohol_wall_front", "h"));
        rectList.add(new StoreElement(new RectF(100f, 870f, 100+60f, 900f), 0.00f, "donuts_wall_front", "h"));
    }

    public ArrayList<StoreElement> getLineList() {
        return lineList;
    }

    public ArrayList<StoreElement> getRectList() {
        return rectList;
    }

    public StoreElement getElementByName(String name) {
        for (StoreElement e : rectList) {
            if (Objects.equals(e.getId(), name)) {
                return e;
            }
        }

        //element with this name was not found
        return null;
    }

    public static class StoreElement {
        private final RectF rect;
        private final Float rot;

        private final String id;

        //orientation pre-rotation (is it a horizontal or vertical rect)?
        private final String orientation;

        private StoreElement(RectF rect, Float rot, String id, String orientation) {
            this.rect = rect;
            this.rot = rot;
            this.id = id;
            this.orientation = orientation;
        }

        public Float getRot() {
            return rot;
        }

        public RectF getRect() {
            return rect;
        }

        public String getId() {
            return id;
        }

        public String getOrientation() {
            return orientation;
        }
    }
}
