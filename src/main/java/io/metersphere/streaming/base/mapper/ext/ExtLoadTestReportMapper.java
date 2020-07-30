package io.metersphere.streaming.base.mapper.ext;

import io.metersphere.streaming.base.domain.LoadTestReportDetail;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.mapping.ResultSetType;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExtLoadTestReportMapper {

    @Update({"UPDATE load_test_report ",
            "SET status = #{nextStatus} ",
            "WHERE id = #{id} AND status = #{prevStatus}"})
    int updateStatus(@Param("id") String id, @Param("nextStatus") String nextStatus, @Param("prevStatus") String prevStatus);

    @Select(value = {
            "SELECT report_id as reportId, content, part ",
            "FROM load_test_report_detail ",
            "WHERE report_id = #{reportId} AND part > 1 ",
            "ORDER BY part "
    })
    @Options(fetchSize = Integer.MIN_VALUE, resultSetType = ResultSetType.FORWARD_ONLY)
    List<LoadTestReportDetail> fetchTestReportDetails(@Param("reportId") String reportId);

}
