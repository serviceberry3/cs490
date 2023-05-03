package weiner.noah.groceryguide;


import android.graphics.RectF;

import java.util.ArrayList;
import java.util.Objects;

public class SSWhalley {
    private final ArrayList<StoreElement> lineList = new ArrayList<>();
    private final ArrayList<StoreElement> elementList = new ArrayList<>();

    private final float aisleFrozen16Left = 16f * Constants.cellWidth;
    private final float produceIslandsLeft = 920f;

    public SSWhalley() {
        //the outer box for the store
        elementList.add(new StoreElement(new RectF(0f, 0f, Constants.mapFrameRectWidth, Constants.mapFrameRectHeight), 0.0f, "frame", "Frame", "h",
                new String[]{}));

        //entrance, exit
        elementList.add(new StoreElement(new RectF(800f, 860f, 800f+90f, 900f), 0.00f, "entrance", "Entrance","h",
                new String[]{"Enter the store here"}));
        //random things directly to left when walk into store
        elementList.add(new StoreElement(new RectF(760f, 840f, 760f+60f, 840f+20f), -90f, "entrance_island", "Entrance island", "v",
                new String[]{"Seasonal goodies"}));
        elementList.add(new StoreElement(new RectF(250f, 855f, 250+90f, 900f), 0.00f, "exit", "Exit","h",
                new String[]{"Exit the store here after checking out"}));

        //checkout area
        elementList.add(new StoreElement(new RectF(110f, 760f, 640f, 830f), 0.00f, "checkout", "Checkout","h",
                new String[]{"Check out here"}));

        //old produce island
        elementList.add(new StoreElement(new RectF(700f, 800f, 700f+60f, 800f+20f), -45f, "old_produce_island", "Healthy snacks and damaged produce", "h",
                new String[]{"Damaged produce", "Fruit-based snacks"}));
        elementList.add(new StoreElement(new RectF(720.08f, 683.49f, 819.20f, 711.69f), 0.00f, "chips_wall_of_value", "Deals and chips", "h",
                new String[]{"Chips", "Value deals"}));

        //all of the store elements inside

        //floral island
        elementList.add(new StoreElement(new RectF(produceIslandsLeft, 800f, 900f+100f, 800f+25f), 0.00f, "floral_island", "Floral","h",
                new String[]{"Floral", "Plants and flowers"}));

        //produce islands directly in front of entrance
        elementList.add(new StoreElement(new RectF(820f, 800f, 820f+60, 800f+15f), 30f, "produce_island_front_west", "Produce 1", "h",
                new String[]{"Produce", "Berries"}));
        elementList.add(new StoreElement(new RectF(820f, 800f-50f, 820f+60, 800f+15f-50f), 30f, "produce_island_front_east", "Produce 2","h",
                new String[]{"Produce", "Dried fruits"}));

        //main produce islands
        elementList.add(new StoreElement(new RectF(produceIslandsLeft, 750f, 900f+100f, 750f+25f), 0.00f, "produce_island_1", "Produce 3","h",
                new String[]{"Produce", "Fruit"}));
        elementList.add(new StoreElement(new RectF(produceIslandsLeft, 680f, 900f+100f, 680+25f), 0.00f, "produce_island_2", "Produce 4", "h",
                new String[]{"Produce", "Fruit", "Melons"}));
        elementList.add(new StoreElement(new RectF(produceIslandsLeft, 610f, 900f+100f, 610+25f), 0.00f, "produce_island_3", "Produce 5", "h",
                new String[]{"Produce", "Avocados"}));
        elementList.add(new StoreElement(new RectF(produceIslandsLeft, 540f, 900f+100f, 540+25f), 0.00f, "produce_island_4", "Produce 6", "h",
                new String[]{"Produce", "Onions", "Potatoes"}));
        elementList.add(new StoreElement(new RectF(produceIslandsLeft, 470f, 900f+100f, 470+25f), 0.00f, "produce_island_5", "Produce 7", "h",
                new String[]{"Produce", "Vegetables"}));
        elementList.add(new StoreElement(new RectF(produceIslandsLeft, 400f, 900f+100f, 400+25f), 0.00f, "produce_island_6", "Produce 8", "h",
                new String[]{"Produce"}));

        //first produce wall
        elementList.add(new StoreElement(new RectF(1053.13f, 360f, 1079.24f, 798.84f), 0.00f, "produce_wall_west", "Bagged produce", "v",
                new String[]{"Produce", "Tofu", "Kombucha", "Fresh health foods"}));
        elementList.add(new StoreElement(new RectF(1053.13f, 200f, 1079.24f, 350f), 0.00f, "produce_wall_east", "Fresh vegetables", "v",
                new String[]{"Produce", "Vegetables", "Greens"}));

        //meat islands
        elementList.add(new StoreElement(new RectF(300f, 80f, 300f+100f, 80f+30f), 0.00f, "meat_island_north", "Meat island 1" , "h",
                new String[]{"Meat products", "Refrigerated boxed meat snacks"}));
        elementList.add(new StoreElement(new RectF(460f, 80f, 460f+100f, 80f+30f), 0.00f, "meat_island_south", "Meat island 2", "h",
                new String[]{"Meat products", "Refrigerated boxed meat snacks"}));



        elementList.add(new StoreElement(new RectF(800f, 360f, 800f+30f, 607.44f), 0.00f, "deli", "Deli","v",
                new String[]{"Deli", "Fresh meat", "Butcher"}));

        elementList.add(new StoreElement(new RectF(990f, 110f, 990f+100f, 110f+30f), 65f, "dips_wall", "Dips","h"
        , new String[]{"Dips", "Hummus", "Spreads"}));

        //stuff on back wall
        elementList.add(new StoreElement(new RectF(930f, 0f, 930f+100f, 30f), 0f, "cakes_fridge_wall", "Refrigerated cakes", "h"
                , new String[]{"Fresh cakes", "Perishable pastries", "Frozen desserts"}));
        elementList.add(new StoreElement(new RectF(760f, 0f, 760f+160f, 30f), 0f, "bread_wall", "Fresh bread 1","h"
        , new String[]{"Bagels", "Artisan baked breads"}));

        elementList.add(new StoreElement(new RectF(880f, 110f, 880+80f, 110f+30f), 50f, "bread_island", "Fresh bread 2", "h"
                , new String[]{"Bagels", "Fresh bread products", "Artisan bread", "Pastries"}));


        elementList.add(new StoreElement(new RectF(700f, 0f, 700f+50f, 30f), 0f, "fish_wall", "Fresh fish","h"
        , new String[]{"Fish counter", "Fresh seafood"}));
        elementList.add(new StoreElement(new RectF(630f, 0f, 630f+65f, 30f), 0f, "fish_fridge_wall", "Refrigerated fish","h"
        , new String[]{"Frozen fish products", "Refrigerated fish products"}));



        elementList.add(new StoreElement(new RectF(360f, 0.38f, 600f, 30f), 0.00f, "wall_meat_packaged", "Packaged meat","h"
                , new String[]{"Chicken breast", "Steak cuts"}));
        elementList.add(new StoreElement(new RectF(27.81f, 2.27f, 350f, 30f), 0.00f, "wall_dairy_yog_milk", "Dairy: yogurt and milk", "h"
                , new String[]{"Yogurt", "Milk", "Orange juice"}));



        //begin the "aisles"
        elementList.add(new StoreElement(new RectF(0, 41.94f, Constants.aisleWidth, 697.81f), 0.00f, "wall_dairy_egg_cheese", "Dairy: eggs and cheese", "v"
                , new String[]{"Cottage cheese", "Eggs", "Heavy cream", "Sliced cheese"}));
        elementList.add(new StoreElement(new RectF(Constants.aisleSpan, 133.59f, Constants.aisleSpan + Constants.aisleWidth, 695.54f), 0.00f, "aisle_ice_cream_frozen", "Ice cream/frozen","v"
                , new String[]{"Ice cream", "Frozen dinners", "Frozen pizza", "Frozen treats"}));
        elementList.add(new StoreElement(new RectF(aisleFrozen16Left, 137.93f, Constants.aisleWidth + aisleFrozen16Left, 696.99f), 0.00f, "aisle_frozen_16", "Frozen/aisle 16","v"
                , new String[]{"Frozen dinners", "Paper towels", "Cups/plates", "Foils/wraps", "Trash bags", "Bath tissue"}));
        elementList.add(new StoreElement(new RectF(aisleFrozen16Left + Constants.aisleSpan, 134.00f, aisleFrozen16Left + Constants.aisleSpan + Constants.aisleWidth, 700.27f), 0.00f, "aisle_16_15", "Aisle 16/15","v"
                , new String[]{"Paper towels", "Cups/plates", "Foils/wraps", "Trash bags", "Bath tissue", "Candles", "Mops/brooms", "Hardware", "Detergents/cleaners", "Automotive"}));
        elementList.add(new StoreElement(new RectF(aisleFrozen16Left + Constants.aisleSpan * 2, 130.72f, aisleFrozen16Left + Constants.aisleSpan * 2 + Constants.aisleWidth, 698.44f), 0.00f, "aisle_15_14", "Aisle 15/14", "v"
                , new String[]{"Candles", "Mops/brooms", "Hardware", "Detergents/cleaners", "Automotive", "Pet supplies", "Pet treats", "Office supplies", "DVDs/video", "Books/magazines"}));
        elementList.add(new StoreElement(new RectF(aisleFrozen16Left + Constants.aisleSpan * 3, 132.18f, aisleFrozen16Left + Constants.aisleSpan * 3 + Constants.aisleWidth, 702.79f), 0.00f, "aisle_14_13", "Aisle 14/13", "v"
                , new String[]{"Pet supplies", "Pet treats", "Office supplies", "DVDs/video", "Books/magazines", "Cough/cold", "Pain relief", "First aid", "Bath/skin care", "Toothpaste"}));
        elementList.add(new StoreElement(new RectF(aisleFrozen16Left + Constants.aisleSpan * 4, 130.72f, aisleFrozen16Left + Constants.aisleSpan * 4 + Constants.aisleWidth, 698.44f), 0.00f, "aisle_13_12", "Aisle 13/12", "v"
                , new String[]{"Cough/cold", "Pain relief", "First aid", "Bath/skin care", "Toothpaste", "Wipes/diapers", "Baby food/formula", "Cosmetics", "Sanitary needs"}));
        elementList.add(new StoreElement(new RectF(aisleFrozen16Left + Constants.aisleSpan * 5, 197.16f, aisleFrozen16Left + Constants.aisleSpan * 5 + Constants.aisleWidth, 701.34f), 0.00f, "aisle_12_11", "Aisle 12/11", "v"
                , new String[]{"Wipes/diapers", "Baby food/formula", "Cosmetics", "Sanitary needs", "Seasonal items", "Greeting cards", "Toys/games", "Candy/gum"}));
        elementList.add(new StoreElement(new RectF(aisleFrozen16Left + Constants.aisleSpan * 6, 194.27f, aisleFrozen16Left + Constants.aisleSpan * 6 + Constants.aisleWidth, 701.34f), 0.00f, "aisle_11_10", "Aisle 11/10", "v"
                , new String[]{"Seasonal items", "Greeting cards", "Toys/games", "Candy/gum", "Pasta/sauce", "Canned tomatoes", "Bread crumbs", "Rice/beans"}));
        elementList.add(new StoreElement(new RectF(aisleFrozen16Left + Constants.aisleSpan * 7, 241.92f, aisleFrozen16Left + Constants.aisleSpan * 7 + Constants.aisleWidth, 696.99f), 0.00f, "aisle_10_9", "Aisle 10/9", "v"
                , new String[]{"Pasta/sauce", "Canned tomatoes", "Bread crumbs", "Rice/beans", "Mexican food", "Chinese food", "Canned soup", "Canned vegetables"}));
        elementList.add(new StoreElement(new RectF(aisleFrozen16Left + Constants.aisleSpan * 8, 189.93f, aisleFrozen16Left + Constants.aisleSpan * 8 + Constants.aisleWidth, 698.44f), 0.00f, "aisle_9_8", "Aisle 9/8", "v"
                , new String[]{"Mexican food", "Chinese food", "Canned soup", "Canned vegetables", "Kitchen gadgets", "Canned fruit", "Baking needs", "Syrup/honey", "Pancake mix"}));
        elementList.add(new StoreElement(new RectF(aisleFrozen16Left + Constants.aisleSpan * 9, 191.37f, aisleFrozen16Left + Constants.aisleSpan * 9 + Constants.aisleWidth, 698.44f), 0.00f, "aisle_8_7", "Aisle 8/7", "v"
                , new String[]{"Kitchen gadgets", "Canned fruit", "Baking needs", "Syrup/honey", "Pancake mix", "Salad dressing", "Cooking oil", "Canned meats/fish", "Pickles/olives", "Peanut butter/jelly", "Bread"}));
        elementList.add(new StoreElement(new RectF(aisleFrozen16Left + Constants.aisleSpan * 10, 166.28f, aisleFrozen16Left + Constants.aisleSpan * 10 + Constants.aisleWidth, 699.34f), 0.00f, "aisle_7_6", "Aisle 7/6", "v"
                , new String[]{"Salad dressing", "Canned meats/fish", "Pickles/olives", "Peanut butter/jelly", "Bread", "Hot & cold cereal", "Granola bars", "Fruit snacks", "Canned/bottled juice", "Juice boxes"}));
        elementList.add(new StoreElement(new RectF(aisleFrozen16Left + Constants.aisleSpan * 11, 165.32f, aisleFrozen16Left + Constants.aisleSpan * 11 + Constants.aisleWidth, 681.98f), 0.00f, "aisle_6_5", "Aisle 6/5", "v"
                , new String[]{"Hot & cold cereal", "Granola bars", "Fruit snacks", "Canned/bottled juice", "Juice boxes", "Chips/snacks", "Carbonated beverages", "Popcorn/nuts", "Bar mixes"}));
        elementList.add(new StoreElement(new RectF(aisleFrozen16Left + Constants.aisleSpan * 12, 191.37f, aisleFrozen16Left + Constants.aisleSpan * 12 + Constants.aisleWidth, 696.99f), 0.00f, "aisle_5_4", "Aisle 5/4", "v"
                , new String[]{"Chips/snacks", "Carbonated beverages", "Popcorn/nuts", "Bar mixes", "Coffee/tea", "Chocolate syrup/cocoa", "Powdered drinks", "Cookies/crackers"}));
        elementList.add(new StoreElement(new RectF(aisleFrozen16Left + Constants.aisleSpan * 13, 162.89f, aisleFrozen16Left + Constants.aisleSpan * 13 + Constants.aisleWidth, 681.05f), 0.00f, "aisle_4_3", "Aisle 4/3", "v"
                , new String[]{"Coffee/tea", "Chocolate syrup/cocoa", "Powdered drinks", "Cookies/crackers", "Natural foods", "Spring water"}));
        elementList.add(new StoreElement(new RectF(aisleFrozen16Left + Constants.aisleSpan * 14, 162.75f, aisleFrozen16Left + Constants.aisleSpan * 14 + Constants.aisleWidth, 669.30f), 0.00f, "aisle_3_deli", "Aisle 3/Deli", "v"
                , new String[]{"Natural foods", "Spring water", "Deli counter"}));


        elementList.add(new StoreElement(new RectF(700f, 160f, 700f+40f, 160f+30f), 0.00f, "grabngo", "Grab & Go prepared food", "h"
                , new String[]{"Ready-to-eat meals", "Grab & go items"}));

        //breakfast (bacon etc)
        elementList.add(new StoreElement(new RectF(700f, 250f, 700f+150f, 250f+30f), 70f, "breakfast", "Breakfast foods and packaged lunches", "h"
                , new String[]{"Breakfast meats", "Bacon", "Turkey bacon", "Lunchables", "Meat and cheese packs"}));

        //cheese island
        elementList.add(new StoreElement(new RectF(820f, 140f, 820f+80f, 140+30f), 50f, "cheese_island", "Cheese","h"
                , new String[]{"Artisan cheeses", "Gouda", "Feta", "Parmesan", "Mozzarella"}));

        //grab and go deli island
        elementList.add(new StoreElement(new RectF(850f, 270f, 850f+60f, 270+30f), 0.00f, "grabngo_deli_island", "Grab & Go deli items","h"
                , new String[]{"Read-to-eat deli items"}));

        //pastries/bakery islands
        elementList.add(new StoreElement(new RectF(980f, 290f, 980+40f, 290+30f), 0.00f, "pastries_south", "Pastries 1", "h"
                , new String[]{"Pastries", "Croissants", "Donuts", "Cakes", "Pies"}));
        elementList.add(new StoreElement(new RectF(940f, 240f, 940f+40f, 240+30f), 0.00f, "pastries_north", "Pastries 2", "h"
                , new String[]{"Pastries", "Croissants", "Donuts", "Cakes", "Pies"}));

        elementList.add(new StoreElement(new RectF(0, 750f, Constants.aisleWidth, 850f), 0.00f, "alcohol", "Alcohol 1", "v"
                , new String[]{"Beer", "Liquor", "Wine", "Carbonated alcoholic beverages", "Cider"}));

        //stuff on front wall
        elementList.add(new StoreElement(new RectF(0f, 870f, 0+90f, 900f), 0.00f, "alcohol_wall_front", "Alcohol 2", "h"
                , new String[]{"Canned alcoholic beverages", "Beer packs"}));
        elementList.add(new StoreElement(new RectF(100f, 870f, 100+60f, 900f), 0.00f, "donuts_wall_front", "Donuts", "h"
                , new String[]{"Boxed donuts", "Donut holes", "Processed sweet snacks"}));
    }

    public ArrayList<StoreElement> getLineList() {
        return lineList;
    }

    public ArrayList<StoreElement> getElementList() {
        return elementList;
    }

    public StoreElement getElementByName(String name) {
        for (StoreElement e : elementList) {
            if (Objects.equals(e.getName(), name)) {
                return e;
            }
        }

        //element with this name was not found
        return null;
    }

    public static class StoreElement {
        private final RectF rect;
        private final Float rot;

        private final String name;
        private final String niceName;

        //orientation pre-rotation (is it a horizontal or vertical rect)?
        private final String orientation;

        //list of aisle contents as marked overhead on stop and shop signs
        private final String[] contents;

        private StoreElement(RectF rect, Float rot, String name, String niceName, String orientation, String[] contents) {
            this.rect = rect;
            this.rot = rot;
            this.name = name;
            this.orientation = orientation;
            this.contents = contents;
            this.niceName = niceName;
        }

        public Float getRot() {
            return rot;
        }

        public RectF getRect() {
            return rect;
        }

        public String getName() {
            return name;
        }

        public String getNiceName() {
            return niceName;
        }

        public String getOrientation() {
            return orientation;
        }


        public float getSpan() {
            if (Objects.equals(this.orientation, "h")) {
                return this.rect.width();
            }
            else {
                return this.rect.height();
            }
        }

        public String[] getContents() {
            return contents;
        }
    }
}
