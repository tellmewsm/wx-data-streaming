package io.metersphere.streaming.base.mapper.ext;

import io.metersphere.streaming.base.domain.LoadTestReportResult;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

@Repository
public interface ExtLoadTestReportResultMapper {

    @Update({"UPDATE load_test_report_result ",
            "SET report_value = #{nextReportValue} ",
            "WHERE report_id = #{reportId} AND report_key = #{reportKey} AND report_value = #{prevReportValue}"})
    int updateReportStatus(@Param("reportId") String reportId,
                           @Param("reportKey") String reportKey,
                           @Param("prevReportValue") String prevReportValue,
                           @Param("nextReportValue") String nextReportValue
    );

    @Update({"UPDATE load_test_report_result ",
            "SET report_value = #{reportResult.reportValue} ",
            "WHERE report_id = #{reportResult.reportId} AND report_key = #{reportResult.reportKey}"})
    int updateReportValue(@Param("reportResult") LoadTestReportResult reportResult);
}
