package com.telco.userservice.mapper;

import com.telco.userservice.model.User;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

@Mapper
public interface UserMapper {

    @Select("SELECT user_id, phone_number, data_plan_limit, current_usage, created_at, updated_at " +
            "FROM users WHERE user_id = #{userId}")
    @Results({
            @Result(property = "userId", column = "user_id"),
            @Result(property = "phoneNumber", column = "phone_number"),
            @Result(property = "dataPlanLimit", column = "data_plan_limit"),
            @Result(property = "currentUsage", column = "current_usage"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    Optional<User> findById(@Param("userId") String userId);

    @Select("SELECT user_id, phone_number, data_plan_limit, current_usage, created_at, updated_at " +
            "FROM users WHERE phone_number = #{phoneNumber}")
    @Results({
            @Result(property = "userId", column = "user_id"),
            @Result(property = "phoneNumber", column = "phone_number"),
            @Result(property = "dataPlanLimit", column = "data_plan_limit"),
            @Result(property = "currentUsage", column = "current_usage"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    Optional<User> findByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    @Select("SELECT user_id, phone_number, data_plan_limit, current_usage, created_at, updated_at " +
            "FROM users ORDER BY created_at DESC")
    @Results({
            @Result(property = "userId", column = "user_id"),
            @Result(property = "phoneNumber", column = "phone_number"),
            @Result(property = "dataPlanLimit", column = "data_plan_limit"),
            @Result(property = "currentUsage", column = "current_usage"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    List<User> findAll();

    @Select("SELECT user_id, phone_number, data_plan_limit, current_usage, created_at, updated_at " +
            "FROM users WHERE (current_usage::decimal / data_plan_limit::decimal) * 100 >= #{threshold}")
    @Results({
            @Result(property = "userId", column = "user_id"),
            @Result(property = "phoneNumber", column = "phone_number"),
            @Result(property = "dataPlanLimit", column = "data_plan_limit"),
            @Result(property = "currentUsage", column = "current_usage"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    List<User> findUsersAboveThreshold(@Param("threshold") double threshold);

    @Insert("INSERT INTO users (user_id, phone_number, data_plan_limit, current_usage) " +
            "VALUES (#{userId}, #{phoneNumber}, #{dataPlanLimit}, #{currentUsage})")
    int insert(User user);

    @Update("UPDATE users SET phone_number = #{phoneNumber}, data_plan_limit = #{dataPlanLimit}, " +
            "current_usage = #{currentUsage}, updated_at = CURRENT_TIMESTAMP " +
            "WHERE user_id = #{userId}")
    int update(User user);

    @Update("UPDATE users SET current_usage = #{currentUsage}, updated_at = CURRENT_TIMESTAMP " +
            "WHERE user_id = #{userId}")
    int updateUsage(@Param("userId") String userId, @Param("currentUsage") long currentUsage);

    @Delete("DELETE FROM users WHERE user_id = #{userId}")
    int deleteById(@Param("userId") String userId);

    @Select("SELECT COUNT(*) FROM users")
    long count();

    @Select("SELECT COUNT(*) FROM users WHERE (current_usage::decimal / data_plan_limit::decimal) * 100 >= #{threshold}")
    long countUsersAboveThreshold(@Param("threshold") double threshold);
}
