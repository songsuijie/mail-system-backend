package com.scut.mailsystem.mapper;

import com.scut.mailsystem.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysUserMapper {

    SysUser selectByUsername(@Param("username") String username);

    SysUser selectActiveByUsername(@Param("username") String username);

    SysUser selectActiveById(@Param("id") Long id);

    int insert(SysUser user);
}
