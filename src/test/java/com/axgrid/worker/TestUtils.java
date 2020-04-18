package com.axgrid.worker;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class TestUtils {

    @Test
    public void testCRC32() {
        String prefix = "doB9coug";
        int userCount = 10000;
        int shardCount = 5;
        Map<Integer, Integer> res = IntStream.range(0,userCount)
                .boxed()
                .map(item -> prefix+"-"+item)
                .map(item -> Utils.shardIndex(item, shardCount))
                .collect(Collectors.groupingBy(item -> item, Collectors.summingInt(item -> 1)));

        log.debug("distribution:{}", res);
        Assert.assertEquals(res.size(), shardCount);
        for(int i=0;i<res.size();i++) {
            double count = (double)userCount / shardCount;
            double t = Math.abs(((double)res.get(i) / count)-1);
            log.info("measurement error:{}", t);
            Assert.assertTrue(t < 0.1);
        }
    }
}
