package com.scut.mailsystem.mapper;

import com.scut.mailsystem.entity.UserSettings;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserSettingsMapper {

    UserSettings selectByUserId(@Param("userId") Long userId);

    int insert(UserSettings settings);

    int updateByUserId(UserSettings settings);
}
