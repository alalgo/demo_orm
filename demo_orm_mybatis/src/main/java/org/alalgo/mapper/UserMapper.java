package org.alalgo.mapper;

import java.util.List;

import org.alalgo.entity.UserDO;
import org.alalgo.plugins.Paginator;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
	
	public List<UserDO> getUser(String username,Paginator paginator);
	public List<UserDO> getUserByName(String username,Paginator paginator);
   // @Update("update user set username=#{username},password=#{password},phoneNumber=#{phoneNumber},enable=#{enable} where userId=#{userId}")  	
    public void updateUser(UserDO userDO);
    
    public void insertUser(UserDO userDO);
}
