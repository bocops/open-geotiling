package org.bocops.opengeotiling;

import java.util.ArrayList;

/**
 * Simplest implementation of {@link TileArea} possible. This just collects all tiles that are added
 * to it, but does not promise cleaning up its internal collection (for example by merging tiles,
 * by removing smaller tiles when a larger, encompassing one is added) or by managing territory
 * beyond its individual, potentially non-contiguous tiles.
 */
public class SimpleTileArea extends TileArea {

    private ArrayList<OpenGeoTile> tiles = new ArrayList<>();
    private OpenGeoTile.TileSize smallestTileSize = OpenGeoTile.TileSize.GLOBAL;

    public SimpleTileArea() {
        super();
    }

    @Override
    public OpenGeoTile.TileSize getSmallestTileSize() {
        return smallestTileSize;
    }

    @Override
    public boolean contains(OpenGeoTile tile) {
        for (OpenGeoTile memberTile : tiles) {
            if (memberTile.contains(tile)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ArrayList<OpenGeoTile> getCoveringTileArrayList() {
        return tiles;
    }

    @Override
    void addNonContainedTile(OpenGeoTile newTile) {
        tiles.add(newTile);
        if (newTile.getTileSize().getCodeLength()>smallestTileSize.getCodeLength()) {
            smallestTileSize = newTile.getTileSize();
        }
    }
}
