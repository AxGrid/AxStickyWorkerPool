package com.axgrid.worker;


import com.axgrid.worker.dto.AxErrorsCollection;
import org.junit.Assert;
import org.junit.Test;

public class TestErrorCollection {

    @Test
    public void testErrorCheck() throws Exception {
        AxErrorsCollection errorsCollection = new AxErrorsCollection();
        Assert.assertEquals(errorsCollection.getErrorCount("w3", 100), 0);
        errorsCollection.put("w3");
        Assert.assertEquals(errorsCollection.getErrorCount("w3", 100), 1);
        errorsCollection.put("w3");
        Thread.sleep(50);
        errorsCollection.put("w3");
        Assert.assertEquals(errorsCollection.getErrorCount("w3", 100), 3);
        Thread.sleep(60);
        Assert.assertEquals(errorsCollection.getErrorCount("w3", 100), 1);
        Thread.sleep(60);
        Assert.assertEquals(errorsCollection.getErrorCount("w3", 100), 0);

    }
}
