package org.bocops.opengeotiling;

import com.google.openlocationcode.OpenLocationCode;

import java.util.ArrayList;

/**
 * An area defined by one or more {@link OpenGeoTile} tiles
 */
public abstract class TileArea {

    /**
     * default constructor
     */
    protected TileArea() {
        //nothing to do here yet
    }

    /**
     * Construct a TileArea from a list of tiles.
     * @param tiles an ArrayList of tiles that should be added to this object
     */
    protected TileArea(ArrayList<OpenGeoTile> tiles) {
        if (tiles!=null) {
            for (OpenGeoTile newTile : tiles) {
                addTile(newTile);
            }
        }
    }

    /**
     * Get a list of tiles that fully cover this TileArea as currently defined. Note that this is
     * not necessarily the same list that went into this object over time. In case of a contiguous
     * TileArea, it can also include tiles that never have been added.
     * @return an ArrayList of {@link OpenGeoTile} tiles which fully cover the area of this TileArea
     */
    public abstract ArrayList<OpenGeoTile> getCoveringTileArrayList();

    /**
     * Check if the area defined by {@link OpenGeoTile} tile is completely inside this object's
     * area.
     * @param tile an OpenGeoTile, the area of which will be checked
     * @return true if the whole area of {@code tile} is inside this object's area, false if not
     */
    public abstract boolean contains(OpenGeoTile tile);

    /**
     * Gets the {@link org.bocops.opengeotiling.OpenGeoTile.TileSize} of the smallest
     * {@link OpenGeoTile} used to define the area of this object.
     * @return the smallest tile size (=longest address) used by one of the tiles of this area
     */
    public abstract OpenGeoTile.TileSize getSmallestTileSize();

    /**
     * Package-private method to add a code that has already been checked to NOT be contained yet.
     * @param newTile a full OpenGeoTile, the area of which will be added to this object's area
     */
    abstract void addNonContainedTile(OpenGeoTile newTile);

    /**
     * Adds the area defined by the {@link OpenGeoTile} newTile to the area represented by this
     * object. Subsequent calls to {@link #contains(OpenGeoTile)} must return true for the
     * same tile address (e.g. "C9C9") as well as for all longer addresses (e.g. "C9C9XXXX")
     * @param newTile a full OpenGeoTile, the area of which will be added to this object's area
     */
    public void addTile(OpenGeoTile newTile) {
        if ((!contains(newTile))) {
            addNonContainedTile(newTile);
        }
    }

    /**
     * Adds the area defined by another TileArea to the area represented by this
     * object.
     * @param newTileArea another TileArea
     */
    public void addTileArea(TileArea newTileArea) {
        for (OpenGeoTile newTile : newTileArea.getCoveringTileArrayList()) {
            addTile(newTile);
        }
    }

    /**
     * Check if the area defined by {@link OpenGeoTile} code is completely inside this object's
     * area.
     * @param code a full {@link OpenLocationCode}, the area of which will be checked
     * @return true if the whole area of {@code code} is inside this object's area, false if not
     */
    public boolean contains(OpenLocationCode code) {
        return contains(new OpenGeoTile(code));
    }

    /**
     * Check if a location is inside this object's area.
     * @param latitude latitude value of the location to be checked
     * @param longitude longitude value of the location to be checked
     * @return true if inside, false if not
     */
    public boolean contains(double latitude, double longitude) {
        return contains(new OpenGeoTile(latitude, longitude, getSmallestTileSize()));
    }
}
