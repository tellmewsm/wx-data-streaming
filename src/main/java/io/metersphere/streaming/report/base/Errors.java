package io.metersphere.streaming.report.base;

import lombok.Data;

@Data
public class Errors {

    private String errorType;
    private String errorNumber;
    private String precentOfErrors;
    private String precentOfAllSamples;
}
