package org.bocops.opengeotiling;

import com.google.openlocationcode.OpenLocationCode;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by andreas on 08.07.17.
 */

public class BlockConstructionTest {

    @Test
    public void constructionsSameBlock() throws Exception {
        String pluscode = "CCXWXWXW+XW";
        OpenLocationCode olc = new OpenLocationCode(pluscode);
        OpenGeoTile.TileSize tileSize = OpenGeoTile.TileSize.DISTRICT;

        OpenGeoTile block1 = new OpenGeoTile(olc, tileSize);
        OpenGeoTile block2 = new OpenGeoTile(pluscode, tileSize);
        OpenGeoTile block3 = new OpenGeoTile(olc.getCode(), tileSize);
        OpenGeoTile block4 = new OpenGeoTile(block1.getTileAddress());
        OpenGeoTile block5 = new OpenGeoTile(block2.getTileAddress());


        Assert.assertTrue(block1.isSameTile(block2));
        Assert.assertTrue(block2.isSameTile(block3));
        Assert.assertTrue(block3.isSameTile(block4));
        Assert.assertTrue(block4.isSameTile(block5));
        Assert.assertTrue(block5.isSameTile(block1));
    }
}
