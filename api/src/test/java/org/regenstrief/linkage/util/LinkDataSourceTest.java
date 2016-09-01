package org.regenstrief.linkage.util;

import junit.framework.Assert;

import java.util.UUID;

import org.junit.Test;

public class LinkDataSourceTest {
    
    @Test
    public void getUniqueIDDataColumn_shouldReturnTheRightColumn() {
        LinkDataSource src = new LinkDataSource("test", null, null, 1);
        Assert.assertNull(src.getUniqueIDDataColumn());
        DataColumn dc = newDataColumn("primary_key");
        src.addDataColumn(dc);
        Assert.assertNull(src.getUniqueIDDataColumn());
        src.setUniqueID("fake");
        Assert.assertNull(src.getUniqueIDDataColumn());
        src.setUniqueID("primary_key");
        Assert.assertEquals(dc, src.getUniqueIDDataColumn());
    }
    
    @Test
    public void getDataColumnByName_shouldReturnTheRightColumn() {
        LinkDataSource src = new LinkDataSource("test2", null, null, 1);
        Assert.assertNull(src.getDataColumnByName("fake"));
        DataColumn nameLast = newDataColumn("name_last"), nameFirst = newDataColumn("name_first");
        src.addDataColumn(nameLast);
        src.addDataColumn(nameFirst);
        Assert.assertEquals(nameLast, src.getDataColumnByName("name_last"));
        Assert.assertEquals(nameFirst, src.getDataColumnByName("name_first"));
    }
    
    private DataColumn newDataColumn(final String name) {
        DataColumn dc = new DataColumn(UUID.randomUUID().toString());
        dc.setName(name);
        return dc;
    }
}
