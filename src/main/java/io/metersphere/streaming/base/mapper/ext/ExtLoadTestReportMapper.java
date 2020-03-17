package io.metersphere.streaming.base.mapper.ext;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

@Repository
public interface ExtLoadTestReportMapper {
    @Update({"UPDATE load_test_report ",
            "SET content = concat(content, #{line}) ",
            "WHERE id = #{testId}"})
    int appendLine(@Param("testId") String id, @Param("line") String line);
}
