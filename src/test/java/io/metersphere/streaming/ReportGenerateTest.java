package io.metersphere.streaming;

import io.metersphere.streaming.base.mapper.LoadTestReportDetailMapper;
import io.metersphere.streaming.service.TestResultService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ReportGenerateTest {
    @Resource
    private LoadTestReportDetailMapper loadTestReportDetailMapper;
    @Resource
    private TestResultService testResultService;


    @Test
    public void test2() throws Exception {
        String reportId = "005aa930-c645-4eae-ab74-e9f0c671d873";
        testResultService.generateReport(reportId);
        Thread.sleep(1000 * 1000L);
    }
}
