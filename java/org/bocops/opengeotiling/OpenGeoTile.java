// Copyright 2017 Andreas Bartels
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//        http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.bocops.opengeotiling;

import com.google.openlocationcode.OpenLocationCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A wrapper around an {@code OpenLocationCode} object, focusing on the area identified by a prefix
 * of the given OpenLocationCode.
 *
 * Using this wrapper class allows to determine whether two locations are in the same or adjacent
 * "tiles", to determine all neighboring tiles of a given one, to calculate a distance in tiles etc.
 *
 * Open Location Code is a technology developed by Google and licensed under the Apache License 2.0.
 * For more information, see https://github.com/google/open-location-code
 *
 * @author Andreas Bartels
 * @version 0.1.0
 */
public class OpenGeoTile {

    public enum TileSize {
        /**
         * An area of 20° x 20°. The side length of this tile varies with its location on the globe,
         * but can be up to approximately 2200km. Tile addresses will be 2 characters long.*/
        GLOBAL(2,20.0),

        /**
         * An area of 1° x 1°. The side length of this tile varies with its location on the globe,
         * but can be up to approximately 110km. Tile addresses will be 4 characters long.*/
        REGION(4,1.0),

        /**
         * An area of 0.05° x 0.05°. The side length of this tile varies with its location on the
         * globe, but can be up to approximately 5.5km. Tile addresses will be 6 characters long.*/
        DISTRICT(6,0.05),

        /**
         * An area of 0.0025° x 0.0025°. The side length of this tile varies with its location on
         * the globe, but can be up to approximately 275m.
         * Tile addresses will be 8 characters long.*/
        NEIGHBORHOOD(8,0.0025),

        /**
         * An area of 0.000125° x 0.000125°. The side length of this tile varies with its location
         * on the globe, but can be up to approximately 14m.
         * Tile addresses will be 10 characters long.*/
        PINPOINT(10,0.000125);

        private final int mCodeLength;
        private final double mCoordinateIncrement;

        TileSize(int codeLength, double coordinateIncrement) {
            mCodeLength = codeLength;
            mCoordinateIncrement = coordinateIncrement;
        }

        public final int getCodeLength() {
            return mCodeLength;
        }

        public final double getCoordinateIncrement() {
            return mCoordinateIncrement;
        }
    }

    // Copy from OpenLocationCode.java
    // A separator used to break the code into two parts to aid memorability.
    private static final char SEPARATOR = '+';

    // Copy from OpenLocationCode.java
    // The character used to pad codes.
    private static final char PADDING_CHARACTER = '0';

    private static final String PADDING_2 = "00";
    private static final String PADDING_4 = "0000";
    private static final String PADDING_6 = "000000";


    private OpenLocationCode mOpenLocationCode;
    private TileSize mTileSize;


    /**
     * Creates a new OpenGeoTile from an existing
     * {@link com.google.openlocationcode.OpenLocationCode}.
     * @param olc OpenLocationCode for the current location. This can be a padded code, in which
     *            case the resulting OpenGeoTile will have a larger TileSize.
     * @throws IllegalArgumentException if olc is not a full code
     */
    public OpenGeoTile(OpenLocationCode olc) {
        if (!olc.isFull()) {
            throw new IllegalArgumentException("Only full OLC supported. Use recover().");
        }

        int codeLength;
        if (OpenLocationCode.isPadded(olc.getCode())) {
            codeLength = olc.getCode().indexOf(PADDING_CHARACTER);
        } else {
            codeLength = Math.min(olc.getCode().length()-1,10);
        }

        if (codeLength==TileSize.GLOBAL.getCodeLength()) {
            mTileSize = TileSize.GLOBAL;
        }
        if (codeLength==TileSize.REGION.getCodeLength()) {
            mTileSize = TileSize.REGION;
        }
        if (codeLength==TileSize.DISTRICT.getCodeLength()) {
            mTileSize = TileSize.DISTRICT;
        }
        if (codeLength==TileSize.NEIGHBORHOOD.getCodeLength()) {
            mTileSize = TileSize.NEIGHBORHOOD;
        }
        if (codeLength==TileSize.PINPOINT.getCodeLength()) {
            mTileSize = TileSize.PINPOINT;
        }

        mOpenLocationCode = olc;
    }

    /**
     * Creates a new OpenGeoTile from an existing
     * {@link com.google.openlocationcode.OpenLocationCode}.
     * @param olc OpenLocationCode for the current location
     * @param tileSize tile size to use for this OpenGeoTile
     * @throws IllegalArgumentException when trying to pass a short (non-full) OLC, or if OLC has
     * too much padding for given tileSize
     */
    public OpenGeoTile(OpenLocationCode olc, TileSize tileSize) {
        if (!olc.isFull()) {
            throw new IllegalArgumentException("Only full OLC supported. Use recover().");
        }

        if (OpenLocationCode.isPadded(olc.getCode())) {
            if (olc.getCode().indexOf(PADDING_CHARACTER)<tileSize.getCodeLength()) {
                throw new IllegalArgumentException("OLC padding larger than allowed by tileSize");
            }
        }
        mTileSize = tileSize;
        mOpenLocationCode = olc;
    }

    /**
     * Creates a new OpenGeoTile from lat/long coordinates.
     * @param latitude latitude of the location
     * @param longitude longitude of the location
     * @param tileSize tile size to use for this OpenGeoTile
     * @throws IllegalArgumentException passed through from
     *         {@link OpenLocationCode#OpenLocationCode(double, double, int)}
     */
    public OpenGeoTile(double latitude, double longitude, TileSize tileSize)
            throws IllegalArgumentException {
        mTileSize = tileSize;
        mOpenLocationCode = new OpenLocationCode(latitude, longitude, TileSize.PINPOINT.getCodeLength());
    }

    /**
     * Creates a new OpenGeoTile from a tile address.
     * @param tileAddress a tile address is a [2/4/6/8/10]-character string that corresponds to a
     *                     valid {@link com.google.openlocationcode.OpenLocationCode} after removing
     *                     '+' and an additional number of trailing characters; tile size is
     *                     determined by the length of this address
     * @throws IllegalArgumentException passed through from
     *         {@link OpenLocationCode#OpenLocationCode(String)} or thrown if tileAddress is of
     *         invalid length
     */
    public OpenGeoTile(String tileAddress) throws IllegalArgumentException {
        TileSize detectedTileSize = null;
        StringBuilder olcBuilder = new StringBuilder();

        if (tileAddress.length() == TileSize.GLOBAL.getCodeLength()) {
            detectedTileSize = TileSize.GLOBAL;
            olcBuilder.append(tileAddress);
            olcBuilder.append(PADDING_6);
            olcBuilder.append(SEPARATOR);
        }

        if (tileAddress.length() == TileSize.REGION.getCodeLength()) {
            detectedTileSize = TileSize.REGION;
            olcBuilder.append(tileAddress);
            olcBuilder.append(PADDING_4);
            olcBuilder.append(SEPARATOR);
        }

        if (tileAddress.length() == TileSize.DISTRICT.getCodeLength()) {
            detectedTileSize = TileSize.DISTRICT;
            olcBuilder.append(tileAddress);
            olcBuilder.append(PADDING_2);
            olcBuilder.append(SEPARATOR);
        }

        if (tileAddress.length() == TileSize.NEIGHBORHOOD.getCodeLength()) {
            detectedTileSize = TileSize.NEIGHBORHOOD;
            olcBuilder.append(tileAddress);
            olcBuilder.append(SEPARATOR);
        }

        if (tileAddress.length() == TileSize.PINPOINT.getCodeLength()) {
            detectedTileSize = TileSize.PINPOINT;
            olcBuilder.append(tileAddress.substring(0,8));
            olcBuilder.append(SEPARATOR);
            olcBuilder.append(tileAddress.substring(8,10));
        }

        if (detectedTileSize == null) {
            throw new IllegalArgumentException("Invalid tile address");
        }

        mTileSize = detectedTileSize;
        mOpenLocationCode = new OpenLocationCode(olcBuilder.toString());
    }

    /**
     * Creates a new OpenGeoTile from an Open Location Code.
     * @param pluscode the Open Location Code
     * @param tileSize the tile size to use
     * @throws IllegalArgumentException passed through from
     *         {@link OpenLocationCode#OpenLocationCode(String)}
     */
    OpenGeoTile(String pluscode, TileSize tileSize) throws IllegalArgumentException {
        OpenLocationCode intermediate = new OpenLocationCode(pluscode);
        if (!intermediate.isFull()) {
            throw new IllegalArgumentException("Only full OLC supported. Use recover().");
        }
        mTileSize = tileSize;
        mOpenLocationCode = intermediate;
    }

    /**
     * The exact {@link com.google.openlocationcode.OpenLocationCode} wrapped by this OpenGeoTile.
     * For the plus code of the whole tile, see {@link #getTileOpenLocationCode()}.
     * @return the exact plus code wrapped by this OpenGeoTile
     */
    public OpenLocationCode getWrappedOpenLocationCode() {
        return mOpenLocationCode;
    }

    /**
     * Get the {@link TileSize} of this OpenGeoTile.
     * @return the {@link TileSize} of this OpenGeoTile
     */
    public TileSize getTileSize() {
        return mTileSize;
    }

    /**
     * A tile address is a string of length 2, 4, 6, 8, or 10, which corresponds to a valid
     * {@link com.google.openlocationcode.OpenLocationCode} after padding with an appropriate
     * number of '0' and '+' characters. Example: Address "CVXW" corresponds to OLC "CVXW0000+"
     * @return the tile address of this OpenGeoTile;
     */
    public String getTileAddress() {
        String intermediate = mOpenLocationCode.getCode().replace(String.valueOf(SEPARATOR),"");
        return intermediate.substring(0, mTileSize.getCodeLength());
    }

    /**
     * The full {@link com.google.openlocationcode.OpenLocationCode} for this tile. Other than
     * {@link #getWrappedOpenLocationCode()}, this will return a full plus code for the whole tile.
     * @return a plus code for the whole tile, probably padded with '0' characters
     */
    public OpenLocationCode getTileOpenLocationCode() {
        OpenGeoTile intermediate = new OpenGeoTile(getTileAddress());
        return intermediate.getWrappedOpenLocationCode();
    }

    /**
     * Get an array of the typically 8  neighboring tiles of the same size.
     * @return an array of the typically 8 neighboring tiles of the same size;
     * may return less than 8 neighbors for tiles near the poles.
     */
    public OpenGeoTile[] getNeighbors() {
        final double[] deltas = {20.0, 1.0, 0.05, 0.0025, 0.000125};
        double delta = deltas[(getTileSize().getCodeLength()-2)/2];

        OpenLocationCode.CodeArea codeArea = mOpenLocationCode.decode();
        double latitude = codeArea.getCenterLatitude();
        double longitude = codeArea.getCenterLongitude();

        int[] latDiff = {+1,+1,+1, 0,-1,-1,-1, 0};
        int[] lngDiff = {-1, 0,+1,+1,+1, 0,-1,-1};

        ArrayList<OpenGeoTile> arNeighbors = new ArrayList<>();

        for (int i=0;i<8;i++) {
            //OLC constructor clips and normalizes,
            //so we don't have to deal with invalid lat/long values directly
            double neighborLatitude  = latitude  + (delta * latDiff[i]);
            double neighborLongitude = longitude + (delta * lngDiff[i]);

            OpenGeoTile n = new OpenGeoTile(neighborLatitude, neighborLongitude, getTileSize());
            if (!n.isSameTile(this)) {
                //don't add tiles that are the same as this one due to clipping near the poles
                arNeighbors.add(n);
            }
        }

        OpenGeoTile[] neighbors = new OpenGeoTile[arNeighbors.size()];
        arNeighbors.toArray(neighbors);
        return neighbors;
    }

    /**
     * Check if a tile describes the same area as this one.
     * @param potentialSameTile the OpenGeoTile to check
     * @return true if tile sizes and addresses are the same; false if not
     */
    public boolean isSameTile(OpenGeoTile potentialSameTile) {
        if (potentialSameTile.getTileSize() != mTileSize) {
            return false;
        }

        return potentialSameTile.getTileAddress().equals(getTileAddress());
    }

    /**
     * Check if a tile is neighboring this one.
     * @param potentialNeighbor the OpenGeoTile to check
     * @return true if this and potentialNeighbor are adjacent (8-neighborhood);
     *         false if not
     */
    public boolean isNeighbor(OpenGeoTile potentialNeighbor) {
        if (potentialNeighbor.getTileSize() == mTileSize) {
            //avoid iterating over neighbors for same tile
            if (potentialNeighbor.isSameTile(this)) {
                return false;
            }

            OpenGeoTile[] neighbors = getNeighbors();
            for (OpenGeoTile n : neighbors) {
                if (potentialNeighbor.isSameTile(n)) {
                    return true;
                }
            }
            return false;
        } else {
            //tiles of different size are adjacent if at least one neighbor of the smaller tile,
            //but not the smaller tile itself, is contained within the bigger tile
            OpenGeoTile smallerTile;
            OpenGeoTile biggerTile;
            if (potentialNeighbor.getTileSize().getCodeLength()>mTileSize.getCodeLength()) {
                smallerTile = potentialNeighbor;
                biggerTile = this;
            } else {
                smallerTile = this;
                biggerTile = potentialNeighbor;
            }

            if (biggerTile.contains(smallerTile)) {
                return false;
            }

            OpenGeoTile[] neighbors = smallerTile.getNeighbors();
            for (OpenGeoTile n : neighbors) {
                if (biggerTile.contains(n)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Check if this tile contains another one.
     * @param potentialMember the OpenGeoTile to check
     * @return true if the area potentialMember falls within the area of this tile, including cases
     * where both are the same; false if not
     */
    public boolean contains(OpenGeoTile potentialMember) {
        //if A contains B, then B's address has A's address as a prefix
        return potentialMember.getTileAddress().startsWith(getTileAddress());
    }

    /**
     * Calculates the Manhattan (city block) distance between this and another tile of the same size.
     * @param otherTile another tile of the same size as this one
     * @return an integer value corresponding to the number of tiles of the given size that need to
     * be traversed getting from one to the other tile
     * @throws IllegalArgumentException thrown if otherTile has different {@link TileSize}
     */
    public int getManhattanTileDistanceTo(OpenGeoTile otherTile) throws IllegalArgumentException {
        if (otherTile.getTileSize() != mTileSize) {
            throw new IllegalArgumentException("Tile sizes don't match");
        }

        return getLatitudinalTileDistance(otherTile, true)
                + getLongitudinalTileDistance(otherTile, true);
    }

    /**
     * Calculates the Chebyshev (chessboard) distance between this and another tile of the same size.
     * @param otherTile another tile of the same size as this one
     * @return an integer value corresponding to the number of tiles of the given size that need to
     * be traversed getting from one to the other tile
     * @throws IllegalArgumentException thrown if otherTile has different {@link TileSize}
     */
    public int getChebyshevTileDistanceTo(OpenGeoTile otherTile) throws IllegalArgumentException {
        if (otherTile.getTileSize() != mTileSize) {
            throw new IllegalArgumentException("Tile sizes don't match");
        }

        return Math.max(getLatitudinalTileDistance(otherTile, true),
                getLongitudinalTileDistance(otherTile, true));
    }

    /**
     * Returns the approximate direction of the other tile relative to this. The return value can
     * have a large margin of error, especially for big or far away tiles, so this should only be
     * interpreted as a very rough approximation and used as such.
     * @param otherTile another tile of the same size as this one
     * @return an angle in radians, 0 being an eastward direction, +/- PI being westward direction
     * @throws IllegalArgumentException thrown if otherTile has different {@link TileSize}
     */
    public double getDirection(OpenGeoTile otherTile) throws IllegalArgumentException {
        if (otherTile.getTileSize() != mTileSize) {
            throw new IllegalArgumentException("Tile sizes don't match");
        }

        int xDiff = getLongitudinalTileDistance(otherTile, false);
        int yDiff = getLatitudinalTileDistance(otherTile, false);
        return Math.atan2(yDiff, xDiff);
    }


    private static int getCharacterIndex(char c) throws IllegalArgumentException {
        //following definitions copied from OpenLocationCode.java
        final char[] ALPHABET = "23456789CFGHJMPQRVWX".toCharArray();
        final Map<Character, Integer> CHARACTER_TO_INDEX = new HashMap<>();
        int index = 0;
        for (char character : ALPHABET) {
            char lowerCaseCharacter = Character.toLowerCase(character);
            CHARACTER_TO_INDEX.put(character, index);
            CHARACTER_TO_INDEX.put(lowerCaseCharacter, index);
            index++;
        }
        //end copy from OpenLocationCode.java

        if (!(CHARACTER_TO_INDEX.containsKey(c))) {
            throw new IllegalArgumentException("Character does not exist in alphabet");
        }

        return CHARACTER_TO_INDEX.get(c);
    }

    private static int characterDistance(char c1, char c2) throws IllegalArgumentException {
        return getCharacterIndex(c1) - getCharacterIndex(c2);
    }

    private int getLatitudinalTileDistance(OpenGeoTile otherTile, boolean absolute)
            throws IllegalArgumentException {
        if (otherTile.getTileSize() != mTileSize) {
            throw new IllegalArgumentException("Tile sizes don't match");
        }

        int numIterations = mTileSize.getCodeLength()/2; //1..5
        int tileDistance = 0;
        for (int i=0;i<numIterations;i++) {
            tileDistance*=20;
            char c1 = getTileAddress().charAt(i*2);
            char c2 = otherTile.getTileAddress().charAt(i*2);
            tileDistance += characterDistance(c1,c2);
        }

        if (absolute) {
            return Math.abs(tileDistance);
        }
        return tileDistance;
    }

    private int getLongitudinalTileDistance(OpenGeoTile otherTile, boolean absolute)
            throws IllegalArgumentException {
        if (otherTile.getTileSize() != mTileSize) {
            throw new IllegalArgumentException("Tile sizes don't match");
        }

        int numIterations = mTileSize.getCodeLength()/2; //1..5
        int tileDistance = 0;
        for (int i=0;i<numIterations;i++) {
            tileDistance*=20;
            char c1 = getTileAddress().charAt(i*2+1);
            char c2 = otherTile.getTileAddress().charAt(i*2+1);
            if (i==0) {
                //for the first longitudinal value, we need to take care of wrapping - basically,
                //if it's shorter to go the other way around, do so
                int firstDiff = characterDistance(c1, c2);
                final int NUM_CHARACTERS_USED = 18; //360°/20° = 18
                if (Math.abs(firstDiff)>(NUM_CHARACTERS_USED/2)) {
                    if (firstDiff>0) {
                        firstDiff -= NUM_CHARACTERS_USED;
                    } else {
                        firstDiff += NUM_CHARACTERS_USED;
                    }
                }
                tileDistance += firstDiff;
            } else {
                tileDistance += characterDistance(c1, c2);
            }
        }

        if (absolute) {
            return Math.abs(tileDistance);
        }
        return tileDistance;
    }
}
