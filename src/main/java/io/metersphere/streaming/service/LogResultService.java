package io.metersphere.streaming.service;

import io.metersphere.streaming.base.mapper.ext.ExtLoadTestReportLogMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class LogResultService {
    private static final String SEPARATOR = " ";
    @Resource
    private ExtLoadTestReportLogMapper extLoadTestReportLogMapper;

    public void save(String value) {
        String reportId = StringUtils.substringBefore(value, SEPARATOR);
        String content = StringUtils.substringAfter(value, SEPARATOR);
        String resourceId = StringUtils.substringBefore(content, SEPARATOR);
        content = StringUtils.substringAfter(content, SEPARATOR);
        content = StringUtils.appendIfMissing(content, "\n");
        extLoadTestReportLogMapper.appendLine(reportId, resourceId, content);
    }
}
