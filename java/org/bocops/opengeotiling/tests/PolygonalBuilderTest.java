package org.bocops.opengeotiling;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class PolygonalBuilderTest {

    @Test
    public void testNullPolygon() throws Exception {
        //not setting a polygon results in null tileArea
        TileArea testTileArea = new TileAreaPolygonalBuilder()
                .setPrecision(OpenGeoTile.TileSize.NEIGHBORHOOD)
                .build();
        Assert.assertTrue(testTileArea==null);
    }

    @Test
    public void testInvalidPolygon() throws Exception {
        //setting an invalid polygon (only two valid vertices) results in null tileArea
        ArrayList<Coordinate> coords = new ArrayList<>();
        coords.add(new Coordinate(0,0));
        coords.add(new Coordinate(1.0,1.0));
        coords.add(new Coordinate(500.0, 500.0)); //invalid, will be removed

        TileArea testTileArea = new TileAreaPolygonalBuilder()
                .setPrecision(OpenGeoTile.TileSize.NEIGHBORHOOD)
                .setCoordinatesList(coords)
                .build();
        Assert.assertTrue(testTileArea==null);
    }

    @Test
    public void testValidPolygonSquare() throws Exception {
        //defining a large, axis-aligned area; locations within, even close to the edge,
        //should be contained
        ArrayList<Coordinate> coords = new ArrayList<>();
        coords.add(new Coordinate(0.0,0.0));
        coords.add(new Coordinate(0.0,1.0));
        coords.add(new Coordinate(1.0,1.0));
        coords.add(new Coordinate(1.0,0.0));

        OpenGeoTile corner1 = new OpenGeoTile(0.01,0.01, OpenGeoTile.TileSize.NEIGHBORHOOD);
        OpenGeoTile corner2 = new OpenGeoTile(0.01,0.99, OpenGeoTile.TileSize.NEIGHBORHOOD);
        OpenGeoTile corner3 = new OpenGeoTile(0.99,0.99, OpenGeoTile.TileSize.NEIGHBORHOOD);
        OpenGeoTile corner4 = new OpenGeoTile(0.99,0.01, OpenGeoTile.TileSize.NEIGHBORHOOD);
        OpenGeoTile center = new OpenGeoTile(0.5,0.5, OpenGeoTile.TileSize.NEIGHBORHOOD);

        TileArea testTileArea = new TileAreaPolygonalBuilder()
                .setPrecision(OpenGeoTile.TileSize.DISTRICT)
                .setCoordinatesList(coords)
                .build();
        Assert.assertFalse(testTileArea==null);

        Assert.assertTrue("Contains area near corner 1",testTileArea.contains(corner1));
        Assert.assertTrue("Contains area near corner 2",testTileArea.contains(corner2));
        Assert.assertTrue("Contains area near corner 3",testTileArea.contains(corner3));
        Assert.assertTrue("Contains area near corner 4",testTileArea.contains(corner4));
        Assert.assertTrue("Contains center",  testTileArea.contains(center));
    }

    @Test
    public void testValidLargePolygon() throws Exception {
        //defining a very large area of 10x10x20x20 = 40K small tiles, which could also
        //be represented by a small number of larger tiles (10x10 = 100 REGION-sized tiles);
        //this test should finish in an acceptable time
        ArrayList<Coordinate> coords = new ArrayList<>();
        coords.add(new Coordinate(0.0,0.0));
        coords.add(new Coordinate(0.0,10.0));
        coords.add(new Coordinate(10.0,10.0));
        coords.add(new Coordinate(10.0,0.0));

        TileArea testTileArea = new TileAreaPolygonalBuilder()
                .setPrecision(OpenGeoTile.TileSize.DISTRICT)
                .setCoordinatesList(coords)
                .build();
        Assert.assertFalse(testTileArea==null);
    }

    @Test
    public void testValidPolygonTriangle() throws Exception {
        //edges not axis-aligned should work as well
        ArrayList<Coordinate> coords = new ArrayList<>();
        coords.add(new Coordinate(0.25,0.25));
        coords.add(new Coordinate(0.25,0.75));
        coords.add(new Coordinate(0.75,0.5));

        OpenGeoTile ogt = new OpenGeoTile(0.5,0.5, OpenGeoTile.TileSize.NEIGHBORHOOD);

        TileArea testTileArea = new TileAreaPolygonalBuilder()
                .setPrecision(OpenGeoTile.TileSize.DISTRICT)
                .setCoordinatesList(coords)
                .build();
        Assert.assertFalse(testTileArea==null);

        Assert.assertTrue(testTileArea.contains(ogt));
    }

    @Test
    public void testMaximumMerge() throws Exception {
        ArrayList<Coordinate> coords = new ArrayList<>();
        coords.add(new Coordinate(0.9,0.9));
        coords.add(new Coordinate(0.9,2.1));
        coords.add(new Coordinate(2.1,2.1));
        coords.add(new Coordinate(2.1,0.9));

        //no maximum tile size, builds a TileArea with potentially GLOBAL-sized tiles
        TileArea testTileArea = new TileAreaPolygonalBuilder()
                .setPrecision(OpenGeoTile.TileSize.DISTRICT)
                .setCoordinatesList(coords)
                .build();
        int numTilesGlobalArray = testTileArea.getCoveringTileArrayList().size();

        //builds a TileArea with potentially REGION-sized tiles
        testTileArea = new TileAreaPolygonalBuilder()
                .setPrecision(OpenGeoTile.TileSize.DISTRICT)
                .setMaximumTileSize(OpenGeoTile.TileSize.REGION)
                .setCoordinatesList(coords)
                .build();
        int numTilesRegionArray = testTileArea.getCoveringTileArrayList().size();

        //builds a TileArea with potentially DISTRICT-sized tiles
        testTileArea = new TileAreaPolygonalBuilder()
                .setPrecision(OpenGeoTile.TileSize.DISTRICT)
                .setMaximumTileSize(OpenGeoTile.TileSize.DISTRICT)
                .setCoordinatesList(coords)
                .build();
        int numTilesDistrictArray = testTileArea.getCoveringTileArrayList().size();

        //our area contains one full REGION but no full GLOBAL tile, so we expect the first two
        //to be of equal size, but the third one to be bigger (-1+400 = +399, to be exact)
        Assert.assertTrue(numTilesGlobalArray == numTilesRegionArray);
        Assert.assertTrue(numTilesRegionArray+399 == numTilesDistrictArray);

    }

    @Test
    public void testSquarePolygonStartingPoint() throws Exception {
        //starting from a polygon with vertices A, B, C, D, the same area should be returned whether
        //starting from A or C (using opposite corners of a square polygon tests all combinations of
        //lat/lng, hi/low starting points.

        double latLow = 0.0;
        double latHi  = 0.1;
        double lngLow = 0.0;
        double lngHi  = 0.1;

        Coordinate a = new Coordinate(latLow, lngLow);
        Coordinate b = new Coordinate(latHi,  lngLow);
        Coordinate c = new Coordinate(latHi,  lngHi);
        Coordinate d = new Coordinate(latLow, lngHi);

        ArrayList<Coordinate> coordsStartinglngLow = new ArrayList<>();
        coordsStartinglngLow.add(a);
        coordsStartinglngLow.add(b);
        coordsStartinglngLow.add(c);
        coordsStartinglngLow.add(d);

        ArrayList<Coordinate> coordsStartinglngHi = new ArrayList<>();
        coordsStartinglngHi.add(c);
        coordsStartinglngHi.add(d);
        coordsStartinglngHi.add(a);
        coordsStartinglngHi.add(b);

        //count tiles in first area
        TileArea testTileArea = new TileAreaPolygonalBuilder()
                .setPrecision(OpenGeoTile.TileSize.NEIGHBORHOOD)
                .setCoordinatesList(coordsStartinglngLow)
                .build();
        Assert.assertFalse(testTileArea==null);

        int numNeighborhoodTilesLow = 0;
        int numOtherTilesLow = 0;
        for (OpenGeoTile ogt : testTileArea.getCoveringTileArrayList()) {
            if (ogt.getTileSize().equals(OpenGeoTile.TileSize.NEIGHBORHOOD)) {
                numNeighborhoodTilesLow++;
            } else {
                numOtherTilesLow++;
            }
        }

        //count tiles in second area
        testTileArea = new TileAreaPolygonalBuilder()
                .setPrecision(OpenGeoTile.TileSize.NEIGHBORHOOD)
                .setCoordinatesList(coordsStartinglngHi)
                .build();
        Assert.assertFalse(testTileArea==null);

        int numNeighborhoodTilesHi = 0;
        int numOtherTilesHi = 0;
        for (OpenGeoTile ogt : testTileArea.getCoveringTileArrayList()) {
            if (ogt.getTileSize().equals(OpenGeoTile.TileSize.NEIGHBORHOOD)) {
                numNeighborhoodTilesHi++;
            } else {
                numOtherTilesHi++;
            }
        }

        Assert.assertTrue(numNeighborhoodTilesLow == numNeighborhoodTilesHi);
        Assert.assertTrue(numOtherTilesLow == numOtherTilesHi);
    }

    /*
    This test currently fails! Need to define an "edge strategy" (INCLUSIVE, EXCLUSIVE, ...)
    and make sure that tiles under the polygon's vertices are included

    @Test
    public void testValidPolygonSquareCorners() throws Exception {
        ArrayList<Coordinate> coords = new ArrayList<>();
        coords.add(new Coordinate(0.0,0.0));
        coords.add(new Coordinate(0.0,1.0));
        coords.add(new Coordinate(1.0,1.0));
        coords.add(new Coordinate(1.0,0.0));

        OpenGeoTile corner1 = new OpenGeoTile(0.0,0.0, OpenGeoTile.TileSize.NEIGHBORHOOD);
        OpenGeoTile corner2 = new OpenGeoTile(0.0,1.0, OpenGeoTile.TileSize.NEIGHBORHOOD);
        OpenGeoTile corner3 = new OpenGeoTile(1.0,1.0, OpenGeoTile.TileSize.NEIGHBORHOOD);
        OpenGeoTile corner4 = new OpenGeoTile(1.0,0.0, OpenGeoTile.TileSize.NEIGHBORHOOD);
        OpenGeoTile center = new OpenGeoTile(0.5,0.5, OpenGeoTile.TileSize.NEIGHBORHOOD);

        TileArea testTileArea = new TileAreaPolygonalBuilder()
                .setPrecision(OpenGeoTile.TileSize.DISTRICT)
                .setCoordinatesList(coords)
                .build();
        Assert.assertFalse(testTileArea==null);

        Assert.assertTrue("Contains corner 1",testTileArea.contains(corner1));
        Assert.assertTrue("Contains corner 2",testTileArea.contains(corner2));
        Assert.assertTrue("Contains corner 3",testTileArea.contains(corner3));
        Assert.assertTrue("Contains corner 4",testTileArea.contains(corner4));
        Assert.assertTrue("Contains center",  testTileArea.contains(center));
    }
    */
}
