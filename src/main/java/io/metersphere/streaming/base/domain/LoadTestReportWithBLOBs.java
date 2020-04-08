package io.metersphere.streaming.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class LoadTestReportWithBLOBs extends LoadTestReport implements Serializable {
    private String description;

    private String content;

    private static final long serialVersionUID = 1L;
}