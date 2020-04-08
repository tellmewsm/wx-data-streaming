package io.metersphere.streaming.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class LoadTestWithBLOBs extends LoadTest implements Serializable {
    private String loadConfiguration;

    private String advancedConfiguration;

    private String schedule;

    private static final long serialVersionUID = 1L;
}