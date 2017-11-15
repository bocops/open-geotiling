package org.bocops.opengeotiling;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Created by andreas on 08.07.17.
 */

public class DirectionTest {
    @Test
    public void testDirections() {
        OpenGeoTile tile1 = new OpenGeoTile("9F53");
        OpenGeoTile tile2 = new OpenGeoTile("8FX3"); //diff: 4 vertical
        OpenGeoTile tile3 = new OpenGeoTile("9F5G"); //diff: -9 horizontal
        OpenGeoTile tile4 = new OpenGeoTile("8FX7"); //diff: -4 hor., 4 vert.;

        double delta = 0.0001;

        Assert.assertEquals(Math.PI/2,tile1.getDirection(tile2), delta);
        Assert.assertEquals(-Math.PI/2,tile2.getDirection(tile1), delta);

        Assert.assertEquals(Math.PI,tile1.getDirection(tile3),delta);
        Assert.assertEquals(0,tile3.getDirection(tile1), delta);

        Assert.assertEquals(0.75*Math.PI,tile1.getDirection(tile4),delta);
        Assert.assertEquals(-0.25*Math.PI,tile4.getDirection(tile1),delta);
    }
}
