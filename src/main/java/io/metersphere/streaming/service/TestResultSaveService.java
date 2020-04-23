package io.metersphere.streaming.service;

import io.metersphere.streaming.base.domain.LoadTestReportResult;
import io.metersphere.streaming.base.mapper.LoadTestReportResultMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class TestResultSaveService {
    @Resource
    private LoadTestReportResultMapper loadTestReportResultMapper;

    public void saveResult(LoadTestReportResult record) {
        loadTestReportResultMapper.insertSelective(record);
    }
}
