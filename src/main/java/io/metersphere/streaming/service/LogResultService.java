package io.metersphere.streaming.service;

import io.metersphere.streaming.base.domain.LoadTestReportLog;
import io.metersphere.streaming.base.domain.LoadTestReportLogExample;
import io.metersphere.streaming.base.mapper.LoadTestReportLogMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class LogResultService {
    @Resource
    private LoadTestReportLogMapper loadTestReportLogMapper;


    public void savePartContent(String reportId, String resourceId, String content) {
        LoadTestReportLogExample example = new LoadTestReportLogExample();
        example.createCriteria().andReportIdEqualTo(reportId).andResourceIdEqualTo(resourceId);
        long part = loadTestReportLogMapper.countByExample(example);
        LoadTestReportLog record = new LoadTestReportLog();
        record.setReportId(reportId);
        record.setPart(part + 1);
        record.setContent(content);
        loadTestReportLogMapper.insert(record);
    }
}
