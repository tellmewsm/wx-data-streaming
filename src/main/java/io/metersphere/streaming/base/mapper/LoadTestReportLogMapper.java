package io.metersphere.streaming.base.mapper;

import io.metersphere.streaming.base.domain.LoadTestReportLog;
import io.metersphere.streaming.base.domain.LoadTestReportLogExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LoadTestReportLogMapper {
    long countByExample(LoadTestReportLogExample example);

    int deleteByExample(LoadTestReportLogExample example);

    int insert(LoadTestReportLog record);

    int insertSelective(LoadTestReportLog record);

    List<LoadTestReportLog> selectByExampleWithBLOBs(LoadTestReportLogExample example);

    List<LoadTestReportLog> selectByExample(LoadTestReportLogExample example);

    int updateByExampleSelective(@Param("record") LoadTestReportLog record, @Param("example") LoadTestReportLogExample example);

    int updateByExampleWithBLOBs(@Param("record") LoadTestReportLog record, @Param("example") LoadTestReportLogExample example);

    int updateByExample(@Param("record") LoadTestReportLog record, @Param("example") LoadTestReportLogExample example);
}