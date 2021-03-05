package org.alalgo.entity;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;
@Data
public class UserDO implements Serializable{
	private Integer userId;
	private String username ;
	private String password ;
	private String phoneNumber;
	private Date createTime;
	private Date updateTime;
	private Boolean enable;
}
