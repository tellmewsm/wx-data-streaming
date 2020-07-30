package io.metersphere.streaming.report.parse;

import io.metersphere.streaming.base.domain.LoadTestReportDetail;
import io.metersphere.streaming.base.domain.LoadTestReportDetailExample;
import io.metersphere.streaming.commons.utils.CommonBeanFactory;
import io.metersphere.streaming.commons.utils.LogUtil;
import io.metersphere.streaming.commons.utils.MsJMeterUtils;
import io.metersphere.streaming.config.JmeterReportProperties;
import io.metersphere.streaming.report.base.ChartsData;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.jmeter.report.core.Sample;
import org.apache.jmeter.report.core.SampleMetadata;
import org.apache.jmeter.report.dashboard.JsonizerVisitor;
import org.apache.jmeter.report.processor.*;
import org.apache.jmeter.report.processor.graph.AbstractOverTimeGraphConsumer;
import org.mybatis.spring.batch.MyBatisCursorItemReader;
import org.mybatis.spring.batch.builder.MyBatisCursorItemReaderBuilder;
import org.springframework.batch.item.ExecutionContext;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ResultDataParse {

    private static final String DATE_TIME_PATTERN = "yyyy/MM/dd HH:mm:ss";
    private static final String TIME_PATTERN = "HH:mm:ss";

    public static <T> List<T> summaryMapParsing(Map<String, Object> map, Class<T> clazz) {
        List<T> list = new ArrayList<>();
        for (String key : map.keySet()) {
            MapResultData mapResultData = (MapResultData) map.get(key);
            ListResultData items = (ListResultData) mapResultData.getResult("items");
            if (items.getSize() > 0) {
                for (int i = 0; i < items.getSize(); i++) {
                    MapResultData resultData = (MapResultData) items.get(i);
                    ListResultData data = (ListResultData) resultData.getResult("data");
                    int size = data.getSize();
                    String[] strArray = new String[size];
                    if (size > 0) {
                        T t = null;
                        for (int j = 0; j < size; j++) {
                            ValueResultData valueResultData = (ValueResultData) data.get(j);
                            Object value = valueResultData.getValue();
                            if (value == null) {
                                strArray[j] = "";
                            } else {
                                strArray[j] = value.toString();
                            }
                        }

                        try {
                            t = setParam(clazz, strArray);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        list.add(t);
                    }
                }
            }
        }
        return list;
    }

    public static List<ChartsData> graphMapParsing(Map<String, Object> map, String seriesName, String yAxis) {
        List<ChartsData> list = new ArrayList<>();
        // ThreadGroup
        for (String key : map.keySet()) {
            MapResultData mapResultData = (MapResultData) map.get(key);
            ResultData maxY = mapResultData.getResult("maxY");
            ListResultData series = (ListResultData) mapResultData.getResult("series");
            if (series.getSize() > 0) {
                for (int j = 0; j < series.getSize(); j++) {
                    MapResultData resultData = (MapResultData) series.get(j);
                    // data, isOverall, label, isController
                    ListResultData data = (ListResultData) resultData.getResult("data");
                    ValueResultData label = (ValueResultData) resultData.getResult("label");

                    if (data.getSize() > 0) {
                        for (int i = 0; i < data.getSize(); i++) {
                            ListResultData listResultData = (ListResultData) data.get(i);
                            String result = listResultData.accept(new JsonizerVisitor());
                            result = result.substring(1, result.length() - 1);
                            String[] split = result.split(",");
                            ChartsData chartsData = new ChartsData();
                            BigDecimal bigDecimal = new BigDecimal(split[0]);
                            String timeStamp = bigDecimal.toPlainString();
                            String time = null;
                            try {
                                time = formatDate(stampToDate(DATE_TIME_PATTERN, timeStamp));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            chartsData.setxAxis(time);
                            if (StringUtils.equals("yAxis2", yAxis)) {
                                chartsData.setyAxis2(new BigDecimal(split[1].trim()));
                                chartsData.setyAxis(new BigDecimal(-1));
                            } else {
                                chartsData.setyAxis(new BigDecimal(split[1].trim()));
                                chartsData.setyAxis2(new BigDecimal(-1));
                            }
                            if (series.getSize() == 1) {
                                chartsData.setGroupName(seriesName);
                            } else {
                                chartsData.setGroupName((String) label.getValue());
                            }
                            list.add(chartsData);
                        }
                    }
                }

            }
        }
        return list;
    }

    public static Map<String, Object> getGraphDataMap(String reportId, AbstractOverTimeGraphConsumer timeGraphConsumer) {
        JmeterReportProperties jmeterReportProperties = CommonBeanFactory.getBean(JmeterReportProperties.class);
        timeGraphConsumer.setGranularity(jmeterReportProperties.getGranularity());
        timeGraphConsumer.initialize();
        SampleContext sampleContext = initJMeterConsumer(reportId, timeGraphConsumer);
        return sampleContext.getData();
    }

    public static Map<String, Object> getSummaryDataMap(String reportId, AbstractSummaryConsumer<?> summaryConsumer) {
        SampleContext sampleContext = initJMeterConsumer(reportId, summaryConsumer);
        return sampleContext.getData();
    }

    public static Map<String, Object> getSampleDataMap(String reportId, AbstractSampleConsumer sampleConsumer) {
        SampleContext sampleContext = initJMeterConsumer(reportId, sampleConsumer);
        return sampleContext.getData();
    }

    private static SampleContext initJMeterConsumer(String reportId, AbstractSampleConsumer abstractSampleConsumer) {
        int row = 0;
        // 使用反射获取properties
        MsJMeterUtils.loadJMeterProperties("jmeter.properties");
        SampleMetadata sampleMetaData = createTestMetaData();
        SampleContext sampleContext = new SampleContext();
        abstractSampleConsumer.setSampleContext(sampleContext);
        abstractSampleConsumer.startConsuming();

        SqlSessionFactory sqlSessionFactory = CommonBeanFactory.getBean(SqlSessionFactory.class);
        MyBatisCursorItemReader<LoadTestReportDetail> myBatisCursorItemReader = new MyBatisCursorItemReaderBuilder<LoadTestReportDetail>()
                .sqlSessionFactory(sqlSessionFactory)
                .queryId("io.metersphere.streaming.base.mapper.LoadTestReportDetailMapper.selectByExampleWithBLOBs")
                .build();
        try {
            LoadTestReportDetailExample example = new LoadTestReportDetailExample();
            example.createCriteria().andReportIdEqualTo(reportId);
            example.setOrderByClause("part");
            Map<String, Object> param = new HashMap<>();
            param.put("oredCriteria", example.getOredCriteria());
            myBatisCursorItemReader.setParameterValues(param);
            myBatisCursorItemReader.open(new ExecutionContext());
            LoadTestReportDetail loadTestReportDetail;
            while ((loadTestReportDetail = myBatisCursorItemReader.read()) != null) {
                //
                String content = loadTestReportDetail.getContent();
                // 去掉第一行表头行
                if (StringUtils.startsWithIgnoreCase(content, "timeStamp")) {
                    continue;
                }

                StringTokenizer tokenizer = new StringTokenizer(content, "\n");
                while (tokenizer.hasMoreTokens()) {
                    String line = tokenizer.nextToken();
                    String[] data = line.split(",", -1);
                    Sample sample = new Sample(row++, sampleMetaData, data);
                    abstractSampleConsumer.consume(sample, 0);
                }
            }
        } catch (Exception e) {
            LogUtil.error(e);
        } finally {
            myBatisCursorItemReader.close();
        }

        abstractSampleConsumer.stopConsuming();
        return sampleContext;
    }

    // Create a static SampleMetadataObject
    private static SampleMetadata createTestMetaData() {
        String columnsString = "timeStamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,bytes,sentBytes,grpThreads,allThreads,URL,Latency,IdleTime,Connect";

        String[] columns = new String[17];
        int lastComa = 0;
        int columnIndex = 0;
        for (int i = 0; i < columnsString.length(); i++) {
            if (columnsString.charAt(i) == ',') {
                columns[columnIndex] = columnsString.substring(lastComa, i);
                lastComa = i + 1;
                columnIndex++;
            } else if (i + 1 == columnsString.length()) {
                columns[columnIndex] = columnsString.substring(lastComa, i + 1);
            }
        }
        return new SampleMetadata(',', columns);
    }

    private static String stampToDate(String pattern, String timeStamp) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(timeStamp)), ZoneId.systemDefault());
        return localDateTime.format(dateTimeFormatter);
    }

    private static String formatDate(String dateString) throws ParseException {
        SimpleDateFormat before = new SimpleDateFormat(DATE_TIME_PATTERN);
        SimpleDateFormat after = new SimpleDateFormat(TIME_PATTERN);
        return after.format(before.parse(dateString));
    }

    private static <T> T setParam(Class<T> clazz, Object[] args)
            throws Exception {
        if (clazz == null || args == null) {
            throw new IllegalArgumentException();
        }
        T t = clazz.newInstance();
        Field[] fields = clazz.getDeclaredFields();
        if (fields == null || fields.length > args.length) {
            throw new IndexOutOfBoundsException();
        }
        for (int i = 0; i < fields.length; i++) {
            fields[i].setAccessible(true);
            fields[i].set(t, args[i]);
        }
        return t;
    }
}
