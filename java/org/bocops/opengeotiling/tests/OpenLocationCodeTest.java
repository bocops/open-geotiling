package org.bocops.opengeotiling;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by andreas on 09.07.17.
 */

public class OpenLocationCodeTest {
    @Test
    public void returnTileOLC() {
        OpenGeoTile tile = new OpenGeoTile("C9");

        Assert.assertEquals("C9000000+",tile.getTileOpenLocationCode().getCode());
    }
}
