package weiner.noah.groceryguide;


import android.graphics.RectF;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.concurrent.CompletionStage;

public class SSWhalley {
    private final ArrayList<StoreElement> lineList = new ArrayList<>();
    private final ArrayList<StoreElement> rectList = new ArrayList<>();

    private final float aisleFrozen16Left = 16f * Constants.cellWidth;

    public SSWhalley() {
        //the outer box for the store
        rectList.add(new StoreElement(new RectF(0f, 0f, Constants.mapFrameRectWidth, Constants.mapFrameRectHeight), 0.0f, "frame"));

        //all of the store elements inside
        rectList.add(new StoreElement(new RectF(939.14f, 775.22f, 1017.92f, 799.01f), -29.26f, "producebay_1"));
        rectList.add(new StoreElement(new RectF(300.60f, 80.12f, 478.10f, 135.12f), 0.00f, "backbay_1"));
        rectList.add(new StoreElement(new RectF(89.50f, 742.98f, 660.07f, 829.88f), 0.00f, "checkout"));
        rectList.add(new StoreElement(new RectF(913.31f, 862.19f, 1020.45f, 898.38f), 0.00f, "entrance"));
        rectList.add(new StoreElement(new RectF(900.50f, 677.66f, 1006.59f, 707.16f), 0.00f, "producebay_2"));
        rectList.add(new StoreElement(new RectF(784.52f, 297.71f, 819.37f, 607.44f), 0.00f, "deli"));
        rectList.add(new StoreElement(new RectF(489.77f, 80.12f, 669.77f, 134.29f), 0.00f, "backbay_2"));
        rectList.add(new StoreElement(new RectF(958.48f, 128.49f, 1106.81f, 166.16f), 65.58f, "wall_dips"));
        rectList.add(new StoreElement(new RectF(157.45f, 855.34f, 264.60f, 900.12f), 0.00f, "exit"));
        rectList.add(new StoreElement(new RectF(712.83f, -1.99f, 878.40f, 38.15f), 0.45f, "wall_fish"));
        rectList.add(new StoreElement(new RectF(720.08f, 683.49f, 819.20f, 711.69f), 0.00f, "chips"));


        rectList.add(new StoreElement(new RectF(397.11f, 0.38f, 685.86f, 37.71f), 0.00f, "wall_meatpackaged"));
        rectList.add(new StoreElement(new RectF(27.81f, 2.27f, 386.41f, 36.44f), 0.00f, "wall_dairyyogmilk"));
        rectList.add(new StoreElement(new RectF(1053.13f, 230.92f, 1079.24f, 798.84f), 0.00f, "wall_produce"));


        //begin the "aisles"
        rectList.add(new StoreElement(new RectF(0, 41.94f, Constants.aisleWidth, 697.81f), 0.00f, "wall_dairyeggcheese"));
        rectList.add(new StoreElement(new RectF(Constants.aisleSpan, 133.59f, Constants.aisleSpan + Constants.aisleWidth, 695.54f), 0.00f, "aisle_dairyeggcheese_frozen"));
        rectList.add(new StoreElement(new RectF(aisleFrozen16Left, 137.93f, Constants.aisleWidth + aisleFrozen16Left, 696.99f), 0.00f, "aisle_frozen_16"));
        rectList.add(new StoreElement(new RectF(aisleFrozen16Left + Constants.aisleSpan, 134.00f, aisleFrozen16Left + Constants.aisleSpan + Constants.aisleWidth, 700.27f), 0.00f, "aisle_16_15"));
        rectList.add(new StoreElement(new RectF(aisleFrozen16Left + Constants.aisleSpan * 2, 130.72f, aisleFrozen16Left + Constants.aisleSpan * 2 + Constants.aisleWidth, 698.44f), 0.00f, "aisle_15_14"));
        rectList.add(new StoreElement(new RectF(aisleFrozen16Left + Constants.aisleSpan * 3, 132.18f, aisleFrozen16Left + Constants.aisleSpan * 3 + Constants.aisleWidth, 702.79f), 0.00f, "aisle_14_13"));
        rectList.add(new StoreElement(new RectF(aisleFrozen16Left + Constants.aisleSpan * 4, 130.72f, aisleFrozen16Left + Constants.aisleSpan * 4 + Constants.aisleWidth, 698.44f), 0.00f, "aisle_13_12"));
        rectList.add(new StoreElement(new RectF(aisleFrozen16Left + Constants.aisleSpan * 5, 197.16f, aisleFrozen16Left + Constants.aisleSpan * 5 + Constants.aisleWidth, 701.34f), 0.00f, "aisle_12_11"));
        rectList.add(new StoreElement(new RectF(aisleFrozen16Left + Constants.aisleSpan * 6, 194.27f, aisleFrozen16Left + Constants.aisleSpan * 6 + Constants.aisleWidth, 701.34f), 0.00f, "aisle_11_10"));
        rectList.add(new StoreElement(new RectF(aisleFrozen16Left + Constants.aisleSpan * 7, 241.92f, aisleFrozen16Left + Constants.aisleSpan * 7 + Constants.aisleWidth, 696.99f), 0.00f, "aisle_10_9"));
        rectList.add(new StoreElement(new RectF(aisleFrozen16Left + Constants.aisleSpan * 8, 189.93f, aisleFrozen16Left + Constants.aisleSpan * 8 + Constants.aisleWidth, 698.44f), 0.00f, "aisle_9_8"));
        rectList.add(new StoreElement(new RectF(aisleFrozen16Left + Constants.aisleSpan * 9, 191.37f, aisleFrozen16Left + Constants.aisleSpan * 9 + Constants.aisleWidth, 698.44f), 0.00f, "aisle_8_7"));
        rectList.add(new StoreElement(new RectF(aisleFrozen16Left + Constants.aisleSpan * 10, 166.28f, aisleFrozen16Left + Constants.aisleSpan * 10 + Constants.aisleWidth, 699.34f), 0.00f, "aisle_7_6"));
        rectList.add(new StoreElement(new RectF(aisleFrozen16Left + Constants.aisleSpan * 11, 165.32f, aisleFrozen16Left + Constants.aisleSpan * 11 + Constants.aisleWidth, 681.98f), 0.00f, "aisle_6_5"));
        rectList.add(new StoreElement(new RectF(aisleFrozen16Left + Constants.aisleSpan * 12, 191.37f, aisleFrozen16Left + Constants.aisleSpan * 12 + Constants.aisleWidth, 696.99f), 0.00f, "aisle_5_4"));
        rectList.add(new StoreElement(new RectF(aisleFrozen16Left + Constants.aisleSpan * 13, 162.89f, aisleFrozen16Left + Constants.aisleSpan * 13 + Constants.aisleWidth, 681.05f), 0.00f, "aisle_4_3"));
        rectList.add(new StoreElement(new RectF(aisleFrozen16Left + Constants.aisleSpan * 14, 162.75f, aisleFrozen16Left + Constants.aisleSpan * 14 + Constants.aisleWidth, 669.30f), 0.00f, "aisle_3_"));

        rectList.add(new StoreElement(new RectF(904.85f, 618.24f, 1002.24f, 649.19f), 0.00f, "producebay_3"));
        rectList.add(new StoreElement(new RectF(903.40f, 545.78f, 997.89f, 578.17f), 0.00f, "producebay_4"));
        rectList.add(new StoreElement(new RectF(900.50f, 484.91f, 1006.59f, 514.40f), 0.00f, "producebay_5"));
        rectList.add(new StoreElement(new RectF(897.60f, 422.59f, 1003.69f, 452.09f), 0.00f, "producebay_6"));
        rectList.add(new StoreElement(new RectF(897.60f, 355.92f, 1003.69f, 385.42f), 0.00f, "producebay_7"));
        rectList.add(new StoreElement(new RectF(882.40f, -0.72f, 1001.59f, 39.78f), 0.45f, "bread"));
        rectList.add(new StoreElement(new RectF(742.04f, 162.43f, 840.93f, 189.03f), 0.00f, "grabngo"));

        rectList.add(new StoreElement(new RectF(834.23f, 830.46f, 941.47f, 862.42f), -89.64f, "snackbay"));
        rectList.add(new StoreElement(new RectF(858.12f, 193.42f, 940.58f, 222.00f), 50.75f, "cheese"));
        rectList.add(new StoreElement(new RectF(942.04f, 240.40f, 1022.29f, 268.69f), 0.00f, "pastries"));

        rectList.add(new StoreElement(new RectF(0, 750f, Constants.aisleWidth, 850f), 0.00f, "alcohol"));
    }

    public ArrayList<StoreElement> getLineList() {
        return lineList;
    }

    public ArrayList<StoreElement> getRectList() {
        return rectList;
    }

    public static class StoreElement {
        private final RectF rect;
        private final Float rot;

        private final String id;

        private StoreElement(RectF rect, Float rot, String id) {
            this.rect = rect;
            this.rot = rot;
            this.id = id;
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
    }
}
