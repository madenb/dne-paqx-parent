package com.dell.cpsd.paqx.dne.rest.model;

import org.junit.Assert;
import org.junit.Test;


public class ClusterInfoTest {

    @Test
    public void testClusterInfo(){
        
        ClusterInfo c = new ClusterInfo("EnterName", 3);
        Assert.assertNotNull(c);

    }
}
