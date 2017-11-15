package org.bocops.opengeotiling;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by andreas on 08.07.17.
 */

public class MemberTest {

    @Test
    public void testMembership() {
        OpenGeoTile bigBlock   = new OpenGeoTile("8CFF");
        OpenGeoTile smallBlock = new OpenGeoTile("8CFFXX");
        OpenGeoTile tinyBlock  = new OpenGeoTile("8CFFXXHH");

        Assert.assertTrue(bigBlock.contains(smallBlock));
        Assert.assertTrue(bigBlock.contains(tinyBlock));
        Assert.assertTrue(smallBlock.contains(tinyBlock));

        Assert.assertTrue(bigBlock.contains(bigBlock));

        Assert.assertFalse(smallBlock.contains(bigBlock));
    }

    @Test
    public void testNonMembership() {
        OpenGeoTile smallBlock = new OpenGeoTile("8CFFXX");
        OpenGeoTile tinyBlock  = new OpenGeoTile("8CXXHHFF");

        Assert.assertFalse(smallBlock.contains(tinyBlock));
    }
}
