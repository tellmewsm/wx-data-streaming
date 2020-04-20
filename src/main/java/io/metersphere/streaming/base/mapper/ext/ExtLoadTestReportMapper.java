package io.metersphere.streaming.base.mapper.ext;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

@Repository
public interface ExtLoadTestReportMapper {
    @Update({"UPDATE load_test_report ",
            "SET content = concat(content, #{line}) ",
            "WHERE id = #{id}"})
    int appendLine(@Param("id") String id, @Param("line") String line);

    @Update({"UPDATE load_test_report ",
            "SET status = #{nextStatus} ",
            "WHERE id = #{id} AND status = #{prevStatus}"})
    int updateStatus(@Param("id") String id, @Param("nextStatus") String nextStatus, @Param("prevStatus") String prevStatus);
}
