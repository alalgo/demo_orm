package org.alalgo.mapper;

import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alalgo.entity.UserDO;
import org.alalgo.plugins.Paginator;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.Before;
import org.junit.Test;

import lombok.extern.slf4j.Slf4j;
@Slf4j
public class MapperTest {
	private SqlSession sqlSession;
	/**
	 * 不使用xml配置文件，完全代码配置环境，构建SqlSessionFactory
	 * @return 
	 * @date: 2020年12月30日
	 * @author: security
	 */
	private SqlSession buildNoxml() {
		PooledDataSource dataSource = new PooledDataSource();
		dataSource.setDriver("com.mysql.jdbc.Driver");
		dataSource.setUsername("root");
		dataSource.setPassword ("root");
		dataSource.setUrl("jdbc:mysql://192.168.6.201:3306/cloudIntegration");
		dataSource.setDefaultAutoCommit(false);	
		Environment environment = new Environment("develop"
				, new JdbcTransactionFactory(), dataSource);
		
		Configuration configuration = new Configuration();
		configuration.setEnvironment(environment);
		configuration.addMapper(UserDO.class);		
		configuration.getTypeAliasRegistry().registerAlias("Integer", java.lang.Integer.class);
		configuration.getTypeAliasRegistry().registerAlias("String", java.lang.String.class);
		
		SqlSessionFactory sqlFactory = new SqlSessionFactoryBuilder().build(configuration);	
		System.out.println(sqlFactory.getConfiguration().getMappedStatements());
		TransactionFactory a;
		return sqlFactory.openSession();
	}
	/**
	 * 使用xml配置文件，通过读取xml构建SqlSessionFactory
	 * @return
	 * @throws IOException 
	 * @date: 2020年12月30日
	 * @author: security
	 */
	private SqlSession buildxml() throws IOException {
		InputStream inputStream = Resources.getResourceAsStream("mybatis-config.xml");
		SqlSessionFactory sqlFactory = new SqlSessionFactoryBuilder().build(inputStream);
		return sqlFactory.openSession();
	}
	@Before
	public void init() {
		try {
			sqlSession = buildxml();
		} catch (IOException e) {
			log.error("", e);
		}		
	}
	@Test
	public void queryByName() {
		try{
			//两种方式调用映射sql
			//List re = sqlSession.selectList("org.alalgo.usc.model.UserMapper.getUserByName", "xu");
			
			UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
			Paginator page = new Paginator();			
			List<UserDO> result = userMapper.getUserByName("xu", page);
			log.debug("result:{}",result);
			log.debug("分页信息 {},{},{}",page.getCount(),page.getLimit(),page.getOffset());
			
			sqlSession.commit();
		}catch(Exception e) {
			sqlSession.rollback();
			log.error("", e);
			//assertFalse(true);
		}finally {
			sqlSession.close();
		}		
	}
	@Test
	public void insertOrUpdates(){
		try{
			List<UserDO> list = new ArrayList();
			for(int i=6314 ; i<6322 ;i++) {
				UserDO userDo = new UserDO();
				userDo.setUserId(i);
				userDo.setUsername("xu");
				userDo.setPassword("343432");
				userDo.setPhoneNumber("12345676543");
				userDo.setEnable(true);
				userDo.setCreateTime(new Date());
				userDo.setUpdateTime(new Date());
				list.add(userDo);
			}
			sqlSession.insert("org.alalgo.usc.model.UserMapper.insertOrUpdateUsers",list);
			sqlSession.commit();
		}catch(Exception e) {
			sqlSession.rollback();
			log.error("", e);
			assertFalse(true);
		}finally {
			sqlSession.close();
		}		
	}	
	@Test
	public void insertMany(){
		try{
			List<UserDO> list = new ArrayList();
			for(int i=0 ; i<10 ;i++) {
				UserDO userDo = new UserDO();
				userDo.setUsername("JACK");
				userDo.setPassword("343432");
				userDo.setPhoneNumber("12345676543");
				userDo.setEnable(true);
				userDo.setCreateTime(new Date());
				userDo.setUpdateTime(new Date());
				list.add(userDo);
			}
			sqlSession.insert("org.alalgo.usc.model.UserMapper.insertUsers",list);
			sqlSession.commit();
		}catch(Exception e) {			
			sqlSession.rollback();
			log.error("", e);
			assertFalse(true);
		}finally {
			sqlSession.close();
		}		
	}
	@Test
	public void insertBatch1(){
		Long time1 = System.currentTimeMillis();
		try{
			UserDO userDo = new UserDO();
			userDo.setUsername("JACK");
			userDo.setPassword("343432");
			userDo.setPhoneNumber("12345676543");
			userDo.setEnable(true);
			userDo.setCreateTime(new Date());
			userDo.setUpdateTime(new Date());
			for(int i=0 ; i<1000 ;i++) {
				sqlSession.insert("org.alalgo.usc.model.UserMapper.insertUser",userDo);
			}
			sqlSession.commit();
		}catch(Exception e) {
			sqlSession.rollback();
			log.error("", e);
			assertFalse(true);
		}finally {
			sqlSession.close();
		}		
		Long time2 = System.currentTimeMillis();
		log.debug("insertBatch1 耗时 {}", time2-time1);
	}	
	@Test
	public void insertBatch2(){
		Long time1 = System.currentTimeMillis();
		try{
			List<UserDO> list = new ArrayList();
			UserDO userDo = new UserDO();
			for(int i=0 ; i<1000 ;i++) {
				userDo.setUsername("JACK");
				userDo.setPassword("343432");
				userDo.setPhoneNumber("12345676543");
				userDo.setEnable(true);
				userDo.setCreateTime(new Date());
				userDo.setUpdateTime(new Date());
				list.add(userDo);
			}
			sqlSession.insert("org.alalgo.usc.model.UserMapper.insertUsers",list);
			sqlSession.commit();
		}catch(Exception e) {
			sqlSession.rollback();
			log.error("", e);
			assertFalse(true);
		}finally {
			sqlSession.close();
		}		
		Long time2 = System.currentTimeMillis();
		log.debug("insertBatch2 耗时 {}", time2-time1);
	}	
	@Test
	public void callable() {
		try{
		    Map<String, String> param = new HashMap<String, String>();
            param.put("username", "xu");
			List<UserDO> list = sqlSession.selectList("org.alalgo.usc.model.UserMapper.callablequeryuser",param);
			log.debug("count:{};result:{}",param.get("count"),list);
			sqlSession.commit();
		}catch(Exception e) {
			sqlSession.rollback();
			log.error("", e);
			assertFalse(true);
		}finally {
			sqlSession.close();
		}			
	}
	public void view() {
		UnpooledDataSource  s;
	}   
}
