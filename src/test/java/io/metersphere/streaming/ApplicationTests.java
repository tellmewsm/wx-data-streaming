package io.metersphere.streaming;

import io.metersphere.streaming.base.mapper.LoadTestReportMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ApplicationTests {
    @Resource
    private LoadTestReportMapper loadTestReportMapper;

    @Test
    public void testMapper() {
        long l = loadTestReportMapper.countByExample(null);
        System.out.println(l);
    }
}
