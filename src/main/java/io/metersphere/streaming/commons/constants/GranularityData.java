package io.metersphere.streaming.commons.constants;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class GranularityData {
    /*
    {start: 0, end: 100, granularity: 1},
    {start: 101, end: 500, granularity: 5},
    {start: 501, end: 1000, granularity: 10},
    {start: 1001, end: 3000, granularity: 30},
    {start: 3001, end: 6000, granularity: 60},
    {start: 6001, end: 30000, granularity: 300},
    {start: 30001, end: 60000, granularity: 600},
    {start: 60001, end: 180000, granularity: 1800},
    {start: 180001, end: 360000, granularity: 3600},
     */
    public static List<Data> dataList = new ArrayList<>();

    static {
        dataList.add(new Data(0, 100, 1));
        dataList.add(new Data(101, 500, 5));
        dataList.add(new Data(501, 1000, 10));
        dataList.add(new Data(1001, 3000, 30));
        dataList.add(new Data(3001, 6000, 60));
        dataList.add(new Data(6001, 30000, 300));
        dataList.add(new Data(30001, 60000, 600));
        dataList.add(new Data(60001, 180000, 1800));
        dataList.add(new Data(180001, 360000, 3600));
        dataList.add(new Data(360000, Integer.MAX_VALUE, 3600));
    }

    @Getter
    @Setter
    public static class Data {
        private Integer start;
        private Integer end;
        private Integer granularity;

        Data(Integer start, Integer end, Integer granularity) {
            this.start = start;
            this.end = end;
            this.granularity = granularity;
        }
    }
}
