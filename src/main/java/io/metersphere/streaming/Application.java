package io.metersphere.streaming;

import io.metersphere.streaming.config.JmeterReportProperties;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication(exclude = {QuartzAutoConfiguration.class})
@EnableConfigurationProperties({
        JmeterReportProperties.class
})
@PropertySource(value = {"file:/opt/metersphere/conf/metersphere.properties"}, encoding = "UTF-8", ignoreResourceNotFound = true)
public class Application {

    public static void main(String[] args) {
        // 非web的方式启动
        new SpringApplicationBuilder(Application.class).web(WebApplicationType.NONE).run(args);
    }

}
