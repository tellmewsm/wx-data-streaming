package io.metersphere.streaming.base.mapper;

import io.metersphere.streaming.base.domain.LoadTestReportResult;
import io.metersphere.streaming.base.domain.LoadTestReportResultExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LoadTestReportResultMapper {
    long countByExample(LoadTestReportResultExample example);

    int deleteByExample(LoadTestReportResultExample example);

    int deleteByPrimaryKey(Long id);

    int insert(LoadTestReportResult record);

    int insertSelective(LoadTestReportResult record);

    List<LoadTestReportResult> selectByExampleWithBLOBs(LoadTestReportResultExample example);

    List<LoadTestReportResult> selectByExample(LoadTestReportResultExample example);

    LoadTestReportResult selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") LoadTestReportResult record, @Param("example") LoadTestReportResultExample example);

    int updateByExampleWithBLOBs(@Param("record") LoadTestReportResult record, @Param("example") LoadTestReportResultExample example);

    int updateByExample(@Param("record") LoadTestReportResult record, @Param("example") LoadTestReportResultExample example);

    int updateByPrimaryKeySelective(LoadTestReportResult record);

    int updateByPrimaryKeyWithBLOBs(LoadTestReportResult record);

    int updateByPrimaryKey(LoadTestReportResult record);
}