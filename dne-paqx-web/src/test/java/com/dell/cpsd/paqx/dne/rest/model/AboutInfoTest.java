package com.dell.cpsd.paqx.dne.rest.model;

import org.junit.Assert;
import org.junit.Test;

public class AboutInfoTest {
    @Test
    public void testAboutInfo(){
        AboutInfo info = new AboutInfo("EnterMessage");
        Assert.assertNotNull(info);
        Assert.assertEquals(info.getMessage(), "EnterMessage");

    }
}
