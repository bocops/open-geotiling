package org.bocops.opengeotiling;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Implements {@link TileArea} in a way that merges smaller tiles added over time into a larger
 * tile, when possible. Generally, this is the case if all 400 (20x20) subtiles have been added.
 * This threshold can be lowered.
 */
public class MergingTileArea extends TileArea {
    private final static int MIN_SUBTILES_PER_TILE = 2; //0 or 1 would lead to all additions snowballing into a full global tile immediately.
    private final static int MAX_SUBTILES_PER_TILE = 400; //20*20
    private final static String GLOBAL_KEY = "0";

    //keeping tiles in a HashMap of ArrayLists, to keep track of tiles with the same address prefix
    private HashMap<String,ArrayList<OpenGeoTile>> tilesHashMap = new HashMap<>();

    private OpenGeoTile.TileSize smallestTileSize = OpenGeoTile.TileSize.GLOBAL;

    private int subtilesPerTile = MAX_SUBTILES_PER_TILE;
    private OpenGeoTile.TileSize maxAllowedTileSize = OpenGeoTile.TileSize.GLOBAL;

    public MergingTileArea() {
        super();
    }

    public MergingTileArea(int subtilesPerTile) {
        super();

        if (subtilesPerTile<MIN_SUBTILES_PER_TILE) {
            this.subtilesPerTile = MIN_SUBTILES_PER_TILE;
            return;
        }

        if (subtilesPerTile>MAX_SUBTILES_PER_TILE) {
            this.subtilesPerTile = MAX_SUBTILES_PER_TILE;
            return;
        }

        this.subtilesPerTile = subtilesPerTile;
    }

    public MergingTileArea(OpenGeoTile.TileSize maxAllowedTileSize) {
        super();

        this.maxAllowedTileSize = maxAllowedTileSize;
    }

    @Override
    public OpenGeoTile.TileSize getSmallestTileSize() {
        return smallestTileSize;
    }

    @Override
    public boolean contains(OpenGeoTile tile) {
        String addressPrefix = tile.getTileAddress();

        //iterate over all address prefixes of the input tile
        while (addressPrefix.length()>0) {
            //remove final two characters from addressPrefix
            addressPrefix = addressPrefix.substring(0,addressPrefix.length()-2);
            String key = addressPrefix;
            if (addressPrefix.length()==0) {
                key = GLOBAL_KEY;
            }

            //if the address prefix is key in the hashmap, we potentially have a match
            if (tilesHashMap.containsKey(key)) {
                ArrayList<OpenGeoTile> tiles = tilesHashMap.get(key);
                //check all tiles contained in this arraylist
                for (OpenGeoTile memberTile : tiles) {
                    if (memberTile.contains(tile)) {
                        //tile is contained in this TileArea
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public ArrayList<OpenGeoTile> getCoveringTileArrayList() {
        ArrayList<OpenGeoTile> tiles = new ArrayList<>();

        //merge all individual lists into a single one
        for (ArrayList<OpenGeoTile> nextList : tilesHashMap.values()) {
            tiles.addAll(nextList);
        }

        return tiles;
    }

    @Override
    void addNonContainedTile(OpenGeoTile newTile) {

        String tilePrefix = newTile.getTileAddressPrefix();
        if (tilePrefix.length() == 0) {
            tilePrefix = GLOBAL_KEY;
        }
        if (tilesHashMap.containsKey(tilePrefix)) {
            ArrayList<OpenGeoTile> tiles = tilesHashMap.get(tilePrefix);
            //we're NOT merging the group of tiles this newTile belongs to, if
            // 1. the group is not complete yet
            // 2. the group already consists of GLOBAL-sized tiles
            // 3. the tile size already is or exceeds the maximum allowed tile size
            if (tiles.size() < subtilesPerTile -1
                    || tilePrefix.equals(GLOBAL_KEY)
                    || newTile.getTileAddress().length() <= maxAllowedTileSize.getCodeLength()) {
                tiles.add(newTile);
                tilesHashMap.put(tilePrefix,tiles);
                if (newTile.getTileSize().getCodeLength()>smallestTileSize.getCodeLength()) {
                    smallestTileSize = newTile.getTileSize();
                }
            } else {
                //adding this tile completes the list to a bigger tile; remove the list, and call
                //this method recursively with the bigger tile. We can be sure that this bigger tile
                //is not contained yet, either; if it was, the initial call to contains() would have
                //returned true;
                tilesHashMap.remove(tilePrefix);
                OpenGeoTile biggerTile = new OpenGeoTile(newTile.getTileAddressPrefix());
                addNonContainedTile(biggerTile);
            }
        } else {
            //we don't have an entry for this tilePrefix yet; create it and add tile
            ArrayList<OpenGeoTile> tiles = new ArrayList<>();
            tiles.add(newTile);
            tilesHashMap.put(tilePrefix, tiles);
        }
    }
}
