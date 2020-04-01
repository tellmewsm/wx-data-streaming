package io.metersphere.streaming.base.mapper.ext;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

@Repository
public interface ExtLoadTestMapper {
    @Update({"UPDATE load_test ",
            "SET status = #{status} ",
            "WHERE id = #{id}"})
    int updateStatus(@Param("id") String id, @Param("status") String status);
}
