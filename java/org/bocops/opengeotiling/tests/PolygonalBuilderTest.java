package org.bocops.opengeotiling;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class PolygonalBuilderTest {

    @Test
    public void testNullPolygon() throws Exception {
        //not setting a polygon results in null tileArea
        TileArea testTileArea = new TileAreaPolygonalBuilder()
                .setTileSize(OpenGeoTile.TileSize.NEIGHBORHOOD)
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
                .setTileSize(OpenGeoTile.TileSize.NEIGHBORHOOD)
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
                .setTileSize(OpenGeoTile.TileSize.DISTRICT)
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
    public void testValidPolygonTriangle() throws Exception {
        //edges not axis-aligned should work as well
        ArrayList<Coordinate> coords = new ArrayList<>();
        coords.add(new Coordinate(0.25,0.25));
        coords.add(new Coordinate(0.25,0.75));
        coords.add(new Coordinate(0.75,0.5));

        OpenGeoTile ogt = new OpenGeoTile(0.5,0.5, OpenGeoTile.TileSize.NEIGHBORHOOD);

        TileArea testTileArea = new TileAreaPolygonalBuilder()
                .setTileSize(OpenGeoTile.TileSize.DISTRICT)
                .setCoordinatesList(coords)
                .build();
        Assert.assertFalse(testTileArea==null);

        Assert.assertTrue(testTileArea.contains(ogt));
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
                .setTileSize(OpenGeoTile.TileSize.DISTRICT)
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
