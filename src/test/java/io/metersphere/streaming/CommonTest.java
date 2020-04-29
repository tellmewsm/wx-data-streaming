package io.metersphere.streaming;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public class CommonTest {

    @Test
    public void test1() {
        String content = "Generating Example class for table load_test_report_log\n";
        System.out.println(StringUtils.substringBefore(content, " "));
        System.out.println(StringUtils.substringAfter(content, " "));

    }
}
