package org.bocops.opengeotiling;

import java.util.ArrayList;

/**
 * A builder to create a {@link TileArea} from an array of coordinates interpreted as vertices
 * of a closed polygon.
 *
 * To create a TileArea:
 * ArrayList<Coordinate> coordinates = ...; // list of coordinates goes here
 * OpenGeoTile.TileSize precision = ...;     // precision of resulting area
 * TileArea area = new TileAreaPolygonalBuilder()
 *         .setPrecision(precision)
 *         .setCoordinatesList(coordinates)
 *         .build();
 *
 * The resulting TileArea can be used to retrieve a list of
 * {@link com.google.openlocationcode.OpenLocationCode} covering the polygon area.
 *
 * @author Andreas Bartels
 * @version 0.1.0
 */
public class TileAreaPolygonalBuilder {

    //min/max values for latitude and longitude in degrees.
    private static final double LATITUDE_MIN  =  -90.0;
    private static final double LONGITUDE_MIN = -180.0;
    private static final double LATITUDE_MAX  =   90.0;
    private static final double LONGITUDE_MAX =  180.0;

    //internal state, updated by set* methods
    private OpenGeoTile.TileSize precision = OpenGeoTile.TileSize.DISTRICT;
    private OpenGeoTile.TileSize maximumTileSize = null;

    private ArrayList<Coordinate> coordinates = null;
    private Coordinate bboxMin, bboxMax;

    public TileAreaPolygonalBuilder() {
        //do nothing yet
    }

    /**
     * Set the minimum tile size of {@link OpenGeoTile} that should be contained
     * in the resulting {@link TileArea}
     * @param precision the precision (or minimum size) for elements of the returned TileArea
     * @return this object, to chain additional setters
     */
    public TileAreaPolygonalBuilder setPrecision(OpenGeoTile.TileSize precision) {
        this.precision = precision;
        return this;
    }

    /**
     * Set the maximum tile size of {@link OpenGeoTile} that should be contained
     * in the resulting {@link TileArea}
     * @param maximumTileSize the maximum TileSize that should be returned by this builder
     * @return this object, to chain additional setters
     */
    public TileAreaPolygonalBuilder setMaximumTileSize(OpenGeoTile.TileSize maximumTileSize) {
        this.maximumTileSize = maximumTileSize;
        return this;
    }

    /**
     * Set an array of coordinates, which will be interpreted as vertices of a closed polygon
     * @param coordinates an array of {@link Coordinate}. Coordinates that are not valid
     *                    latitude/longitude pairs will be dropped, the remaining list needs to
     *                    contain at least three elements to form a valid polygon. The list is not
     *                    checked for problems such as self-intersection.
     * @return this object, to chain additional setters
     */
    public TileAreaPolygonalBuilder setCoordinatesList(ArrayList<Coordinate> coordinates) {
        this.coordinates = new ArrayList<>();

        //set bounding box to inverted values;
        this.bboxMin = new Coordinate(LATITUDE_MAX, LONGITUDE_MAX);
        this.bboxMax = new Coordinate(LATITUDE_MIN, LONGITUDE_MIN);

        //filter invalid coordinates, update bounding box
        for (Coordinate coordinate : coordinates) {
            if (coordinate.latitude() >= LATITUDE_MIN
                    && coordinate.latitude() <= LATITUDE_MAX
                    && coordinate.longitude() >= LONGITUDE_MIN
                    && coordinate.longitude() <= LONGITUDE_MAX) {
                this.coordinates.add(coordinate);

                if (coordinate.latitude() < bboxMin.latitude()) {
                    bboxMin.setLatitude(coordinate.latitude());
                }

                if (coordinate.latitude() > bboxMax.latitude()) {
                    bboxMax.setLatitude(coordinate.latitude());
                }

                if (coordinate.longitude() < bboxMin.longitude()) {
                    bboxMin.setLongitude(coordinate.longitude());
                }

                if (coordinate.longitude() > bboxMax.longitude()) {
                    bboxMax.setLongitude(coordinate.longitude());
                }
            } //coordinate is invalid else; skip it
        }

        return this;
    }

    /**
     * Checks if building the TileArea now would result in a non-null return value
     * @return true, if all necessary values have been set; false otherwise
     */
    public boolean isValid() {
        if (coordinates == null) {
            //array list hasn't been set
            return false;
        }

        if (precision == null) {
            //tile size hasn't been set
            return false;
        }

        if (coordinates.size()<=2) {
            //can't create polygon with less than 3 coordinates
            return false;
        }

        return true;
    }

    /**
     * Build and return a {@link TileArea}
     * @return a TileArea corresponding to the closed polygon input, if all vertices of that polygon
     * are valid lat/long coordinates; a TileArea created from all valid vertices if not; null, if
     * the state of this builder is not valid
     */
    public TileArea build() {
        //return null value if TileArea could not be constructed
        if (!isValid()) {
            return null;
        }

        TileArea rasterizedArea;
        if (maximumTileSize==null) {
            rasterizedArea = new MergingTileArea();
        } else {
            rasterizedArea = new MergingTileArea(maximumTileSize);
        }

        //rasterize polygon using scanlines, based on public-domain code by Darel Rex Finley, 2007;
        //http://alienryderflex.com/polygon_fill/

        double increment = precision.getCoordinateIncrement();

        //determine min and max latitude/longitude we want to use
        //go through OGT to retrieve center coordinates, pad by one increment to not exclude
        //border tiles in some situations
        OpenGeoTile minOGT = new OpenGeoTile(bboxMin.latitude(), bboxMin.longitude(), precision);
        OpenGeoTile maxOGT = new OpenGeoTile(bboxMax.latitude(), bboxMax.longitude(), precision);
        double minLatitude =
                minOGT.getWrappedOpenLocationCode().decode().getCenterLatitude() - increment;
        double maxLatitude =
                maxOGT.getWrappedOpenLocationCode().decode().getCenterLatitude() + increment;
        double minLongitude =
                minOGT.getWrappedOpenLocationCode().decode().getCenterLongitude() - increment;
        double maxLongitude =
                maxOGT.getWrappedOpenLocationCode().decode().getCenterLongitude() + increment;

        //loop through latitude ("scanlines")
        for (double latitude=minLatitude; latitude<maxLatitude; latitude+=increment) {

            //for each latitude scanline, build a list of intersection points with the polygon
            double[] intersectionLongitudes = new double[coordinates.size()];
            int nextIndex = 0;

            int j = coordinates.size() -1;
            for (int i=0; i<coordinates.size();i++) {
                if ((coordinates.get(i).latitude()<latitude
                        && coordinates.get(j).latitude()>latitude)
                        || (coordinates.get(i).latitude()>latitude
                        && coordinates.get(j).latitude()<latitude)) {
                    //the polygon edge between vertices i and j intersects this latitude scanline;
                    //approximate longitudinal value of the intersection point
                    intersectionLongitudes[nextIndex] =
                            (coordinates.get(i).longitude()+
                                    ((latitude-coordinates.get(i).latitude()))
                                    /(coordinates.get(j).latitude()-coordinates.get(i).latitude())
                                    *(coordinates.get(j).longitude()-coordinates.get(i).longitude()));
                    nextIndex++;
                }

                j=i;
            }

            //Sort the intersection longitudes via a simple bubble sort.
            int i = 0;
            while (i<=nextIndex-2) { //nextIndex is the first index not filled with valid data, so we need to iterate up to this -2
                if (intersectionLongitudes[i]>intersectionLongitudes[i+1]) {
                    double swap = intersectionLongitudes[i];
                    intersectionLongitudes[i] = intersectionLongitudes[i+1];
                    intersectionLongitudes[i+1] = swap;
                    if (i > 0) {
                        i--;
                    }
                } else {
                    i++;
                }
            }

            //add all tiles on this scanline between pairs of intersection longitudes
            for (i = 0; i<nextIndex-1; i+=2) {
                if (intersectionLongitudes[i]>=maxLongitude) {
                    break;
                }

                if (intersectionLongitudes[i+1]>minLongitude) {
                    if (intersectionLongitudes[i] < minLongitude) {
                        intersectionLongitudes[i] = minLongitude;
                    }

                    if (intersectionLongitudes[i+1]>maxLongitude) {
                        intersectionLongitudes[i+1] = maxLongitude;
                    }

                    for (double longitude = intersectionLongitudes[i];
                         longitude<intersectionLongitudes[i+1];longitude+=increment) {
                        //latitude and longitude define a tile inside the polygon; add that to area
                        OpenGeoTile ogt = new OpenGeoTile(latitude,longitude, precision);
                        rasterizedArea.addTile(ogt);
                    }
                }
            }
        }

        return rasterizedArea;
    }
}
