package org.bocops.opengeotiling;

import com.google.openlocationcode.OpenLocationCode;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by andreas on 08.07.17.
 */

public class TileSizeTest {

    @Test
    public void testGlobalSize() throws Exception {
        String pluscode = "CCXWXWXW+XW";
        OpenLocationCode olc = new OpenLocationCode(pluscode);
        OpenGeoTile.TileSize tileSize = OpenGeoTile.TileSize.GLOBAL;
        int codeLength = OpenGeoTile.TileSize.GLOBAL.getCodeLength();

        OpenGeoTile block1 = new OpenGeoTile(olc, tileSize);

        Assert.assertEquals(codeLength,2);
        Assert.assertEquals(block1.getTileAddress(),pluscode.substring(0,codeLength));
    }

    @Test
    public void testRegionSize() throws Exception {
        String pluscode = "CCXWXWXW+XW";
        OpenLocationCode olc = new OpenLocationCode(pluscode);
        OpenGeoTile.TileSize tileSize = OpenGeoTile.TileSize.REGION;
        int codeLength = OpenGeoTile.TileSize.REGION.getCodeLength();

        OpenGeoTile block1 = new OpenGeoTile(olc, tileSize);

        Assert.assertEquals(codeLength,4);
        Assert.assertEquals(block1.getTileAddress(),pluscode.substring(0,codeLength));
    }

    @Test
    public void testDistrictSize() throws Exception {
        String pluscode = "CCXWXWXW+XW";
        OpenLocationCode olc = new OpenLocationCode(pluscode);
        OpenGeoTile.TileSize tileSize = OpenGeoTile.TileSize.DISTRICT;
        int codeLength = OpenGeoTile.TileSize.DISTRICT.getCodeLength();

        OpenGeoTile block1 = new OpenGeoTile(olc, tileSize);

        Assert.assertEquals(codeLength,6);
        Assert.assertEquals(block1.getTileAddress(),pluscode.substring(0,codeLength));
    }

    @Test
    public void testNeighborhoodSize() throws Exception {
        String pluscode = "CCXWXWXW+XW";
        OpenLocationCode olc = new OpenLocationCode(pluscode);
        OpenGeoTile.TileSize tileSize = OpenGeoTile.TileSize.NEIGHBORHOOD;
        int codeLength = OpenGeoTile.TileSize.NEIGHBORHOOD.getCodeLength();

        OpenGeoTile block1 = new OpenGeoTile(olc, tileSize);

        Assert.assertEquals(codeLength,8);
        Assert.assertEquals(block1.getTileAddress(),pluscode.substring(0,codeLength));
    }

    @Test
    public void testPinpointSize() throws Exception {
        String pluscode = "CCXWXWXW+XW";
        OpenLocationCode olc = new OpenLocationCode(pluscode);
        OpenGeoTile.TileSize tileSize = OpenGeoTile.TileSize.PINPOINT;
        int codeLength = OpenGeoTile.TileSize.PINPOINT.getCodeLength();

        OpenGeoTile block1 = new OpenGeoTile(olc, tileSize);

        Assert.assertEquals(codeLength,10);
        Assert.assertEquals(block1.getTileAddress(),pluscode.replace("+",""));
    }
}
