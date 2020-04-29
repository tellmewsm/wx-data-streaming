package io.metersphere.streaming.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class LoadTestReportLog implements Serializable {
    private String reportId;

    private String content;

    private static final long serialVersionUID = 1L;
}