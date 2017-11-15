package org.bocops.opengeotiling;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Created by andreas on 08.07.17.
 */

public class DistanceTest {

    @Test
    public void testDistancesSimpleRegion() {
        OpenGeoTile tile1 = new OpenGeoTile("9F53");
        OpenGeoTile tile2 = new OpenGeoTile("8FXG"); //diff: 9 horizontal, 4 vertical

        Assert.assertTrue(tile1.getManhattanTileDistanceTo(tile2)==tile2.getManhattanTileDistanceTo(tile1));
        Assert.assertEquals((9+4),tile1.getManhattanTileDistanceTo(tile2));
        Assert.assertEquals(9,tile1.getChebyshevTileDistanceTo(tile2));
    }

    @Test
    public void testDistancesSimpleDistrict() {
        OpenGeoTile tile1 = new OpenGeoTile("9F53XX");
        OpenGeoTile tile2 = new OpenGeoTile("8FXGXX"); //diff: 9*20 horizontal, 4*20 vertical

        Assert.assertTrue(tile1.getManhattanTileDistanceTo(tile2)==tile2.getManhattanTileDistanceTo(tile1));
        Assert.assertEquals((9+4)*20,tile1.getManhattanTileDistanceTo(tile2));
        Assert.assertEquals((9*20),tile1.getChebyshevTileDistanceTo(tile2));
    }

    @Test
    public void testDistancesSimpleNeighborhood() {
        OpenGeoTile tile1 = new OpenGeoTile("9F53XXXX");
        OpenGeoTile tile2 = new OpenGeoTile("8FXGXXXX"); //diff: 9*20*20 horizontal, 4*20*20 vertical

        Assert.assertTrue(tile1.getManhattanTileDistanceTo(tile2)==tile2.getManhattanTileDistanceTo(tile1));
        Assert.assertEquals((9+4)*20*20,tile1.getManhattanTileDistanceTo(tile2));
        Assert.assertEquals((9*20*20),tile1.getChebyshevTileDistanceTo(tile2));
    }

    @Test
    public void testDistancesSimplePinpoint() {
        OpenGeoTile tile1 = new OpenGeoTile("9F53XXXXXX");
        OpenGeoTile tile2 = new OpenGeoTile("8FXGXXXXXX"); //diff: 9*20*20*20 horizontal, 4*20*20*20 vertical

        Assert.assertTrue(tile1.getManhattanTileDistanceTo(tile2)==tile2.getManhattanTileDistanceTo(tile1));
        Assert.assertEquals((9+4)*20*20*20,tile1.getManhattanTileDistanceTo(tile2));
        Assert.assertEquals((9*20*20*20),tile1.getChebyshevTileDistanceTo(tile2));
    }

    @Test
    public void testDistancesHalfCircle() {
        OpenGeoTile tile1 = new OpenGeoTile("9C22");
        OpenGeoTile tile2 = new OpenGeoTile("8VX3"); //diff: 9*20+1 horizontal, 1 vertical

        Assert.assertTrue(tile1.getManhattanTileDistanceTo(tile2)==tile2.getManhattanTileDistanceTo(tile1));
        Assert.assertEquals(9*20+1+1,tile1.getManhattanTileDistanceTo(tile2));
        Assert.assertEquals(9*20+1,tile1.getChebyshevTileDistanceTo(tile2));
    }

    @Test
    public void testDistancesWrapEastWest() {
        OpenGeoTile tile1 = new OpenGeoTile("9622");
        OpenGeoTile tile2 = new OpenGeoTile("8VX3"); //diff: 5*20-1 horizontal, 1 vertical

        Assert.assertTrue(tile1.getManhattanTileDistanceTo(tile2)==tile2.getManhattanTileDistanceTo(tile1));
        Assert.assertEquals(5*20-1+1,tile1.getManhattanTileDistanceTo(tile2));
        Assert.assertEquals(5*20-1,tile1.getChebyshevTileDistanceTo(tile2));
    }

    @Test
    public void testDistancesWrapWestEast() {
        OpenGeoTile tile1 = new OpenGeoTile("9H22");
        OpenGeoTile tile2 = new OpenGeoTile("82X3"); //diff: 7*20+1 horizontal, 1 vertical

        Assert.assertTrue(tile1.getManhattanTileDistanceTo(tile2)==tile2.getManhattanTileDistanceTo(tile1));
        Assert.assertEquals(7*20+1+1,tile1.getManhattanTileDistanceTo(tile2));
        Assert.assertEquals(7*20+1,tile1.getChebyshevTileDistanceTo(tile2));
    }
}
