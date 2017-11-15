package org.bocops.opengeotiling;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by andreas on 08.07.17.
 */

public class AdjacencyTest {

    static OpenGeoTile originalBlock;

    @BeforeClass
    public static void setupOriginalBlock() {
        //random block we're testing against
        originalBlock = new OpenGeoTile("8CRW2X");
    }

    @Test
    public void testAdjacency() throws Exception {
        //neighboring blocks at the same scale are considered adjacent
        OpenGeoTile neighborBlock1 = new OpenGeoTile("8CRW3W");
        OpenGeoTile neighborBlock2 = new OpenGeoTile("8CRW3X");
        OpenGeoTile neighborBlock3 = new OpenGeoTile("8CRX32");
        OpenGeoTile neighborBlock4 = new OpenGeoTile("8CRX22");
        OpenGeoTile neighborBlock5 = new OpenGeoTile("8CQXX2");
        OpenGeoTile neighborBlock6 = new OpenGeoTile("8CQWXX");
        OpenGeoTile neighborBlock7 = new OpenGeoTile("8CQWXW");
        OpenGeoTile neighborBlock8 = new OpenGeoTile("8CRW2W");
        Assert.assertTrue(originalBlock.isNeighbor(neighborBlock1));
        Assert.assertTrue(originalBlock.isNeighbor(neighborBlock2));
        Assert.assertTrue(originalBlock.isNeighbor(neighborBlock3));
        Assert.assertTrue(originalBlock.isNeighbor(neighborBlock4));
        Assert.assertTrue(originalBlock.isNeighbor(neighborBlock5));
        Assert.assertTrue(originalBlock.isNeighbor(neighborBlock6));
        Assert.assertTrue(originalBlock.isNeighbor(neighborBlock7));
        Assert.assertTrue(originalBlock.isNeighbor(neighborBlock8));
    }

    @Test
    public void testAdjacencyWrapping() throws Exception {
        //adjacency wraps correctly
        OpenGeoTile pacificLeft = new OpenGeoTile("8V");
        OpenGeoTile pacificRight = new OpenGeoTile("72");
        Assert.assertTrue(pacificLeft.isNeighbor(pacificRight));
    }

    @Test
    public void testNonAdjacencyRandomBlock() throws Exception {
        //non-neighboring blocks are not considered adjacent
        OpenGeoTile noNeighborBlock = new OpenGeoTile("3FHP99");
        Assert.assertFalse(originalBlock.isNeighbor(noNeighborBlock));
    }

    @Test
    public void testNonAdjacencyDifferentScale() throws Exception {
        //neighboring blocks at different scales are not considered adjacent
        OpenGeoTile neighborBlockDifferentSize1 = new OpenGeoTile("8CRW2W8X");
        OpenGeoTile neighborBlockDifferentSize2 = new OpenGeoTile("8CRX");
        Assert.assertFalse(originalBlock.isNeighbor(neighborBlockDifferentSize1));
        Assert.assertFalse(originalBlock.isNeighbor(neighborBlockDifferentSize2));
    }

    @Test
    public void testNonAdjacencySelf() throws Exception {
        //no block is adjacent to itself, even at the poles
        OpenGeoTile polarBlock = new OpenGeoTile("CC");
        Assert.assertFalse(originalBlock.isNeighbor(originalBlock));
        Assert.assertFalse(polarBlock.isNeighbor(polarBlock));
    }
}
