package org.alalgo.plugins;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import lombok.extern.slf4j.Slf4j;
@Intercepts({
    @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
    @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),	
})
@Slf4j
public class PaginatorPlugin  implements Interceptor{

	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		Executor executor = (Executor) invocation.getTarget();
		Object[] args = invocation.getArgs();
		MappedStatement mappedStatement = (MappedStatement) args[0];
		Object parameterObject = args[1];
		Object paginator = args[2];
		ResultHandler resultHandler = (ResultHandler) args[3];

		//用拦截器进行物理分页
		if(paginator instanceof Paginator) {
			BoundSql boundSql = mappedStatement.getBoundSql(parameterObject);
			String countSql = "select count(*) from ( " + boundSql.getSql() + " )al";
			BoundSql countBoundSql = new BoundSql(mappedStatement.getConfiguration(), countSql, boundSql.getParameterMappings(), boundSql.getParameterObject());
			MappedStatement mappedStatementTemp = createCountMappedStatement(mappedStatement,mappedStatement.getSqlSource());
			CacheKey cacheKey = executor.createCacheKey(mappedStatementTemp, parameterObject, RowBounds.DEFAULT, boundSql);
			
			//数据库中查询总数量
			List<Object> result = executor.query(mappedStatementTemp, parameterObject, RowBounds.DEFAULT, resultHandler, cacheKey, countBoundSql);
			long count = (long) result.get(0);
			((Paginator) paginator).setCount(count);			
			args[2] = RowBounds.DEFAULT;
			
			//物理分页
			Paginator page = (Paginator) paginator;
			int limit = page.getLimit();
			if(limit < Integer.MAX_VALUE) {
				int offset = page.getOffset();	
				String paginaSql = boundSql.getSql() + "  limit " + (offset-1)*limit + " , " + offset*limit;
				
				if(args.length>5)
					args[5] = new BoundSql(mappedStatement.getConfiguration(), paginaSql , boundSql.getParameterMappings(), boundSql.getParameterObject());
				
				SqlSource source = new StaticSqlSource(mappedStatement.getConfiguration(), paginaSql,boundSql.getParameterMappings());
				args[0] = createLimitMappedStatement(mappedStatement, source);
			}
		}
		
		log.debug("Executor commit before...");
		Object returnObject = invocation.proceed();
		args[2] = paginator;

		log.debug("Executor commit after...");
		return returnObject;
	}
	private MappedStatement createLimitMappedStatement(MappedStatement ms,SqlSource sqlSource) {
        MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(),ms.getId(), sqlSource, ms.getSqlCommandType());
        builder.resource(ms.getResource());
        builder.fetchSize(ms.getFetchSize());
        builder.statementType(ms.getStatementType());
        builder.keyGenerator(ms.getKeyGenerator());
        if(ms.getKeyProperties() != null)
        	builder.keyProperty(Arrays.asList(ms.getKeyProperties()).stream().collect(Collectors.joining(",")));
        builder.timeout(ms.getTimeout());
        builder.parameterMap(ms.getParameterMap());
        builder.resultMaps(ms.getResultMaps());
        builder.resultSetType(ms.getResultSetType());
        builder.cache(ms.getCache());
        builder.flushCacheRequired(ms.isFlushCacheRequired());
        builder.useCache(ms.isUseCache());
        builder.databaseId(ms.getDatabaseId());
        if(ms.getKeyColumns() != null)
        	builder.keyColumn(Arrays.asList(ms.getKeyColumns()).stream().collect(Collectors.joining(",")));
        builder.lang(ms.getLang());
        if(ms.getResultSets() != null)
        	builder.resultSets(Arrays.asList(ms.getResultSets()).stream().collect(Collectors.joining(",")));
        builder.resultOrdered(ms.isResultOrdered());        
        return builder.build();	
	} 
	private MappedStatement createCountMappedStatement(MappedStatement ms,SqlSource sqlSource) {
        MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(), ms.getId()+".count", sqlSource, ms.getSqlCommandType());
        builder.resource(ms.getResource());
        builder.fetchSize(ms.getFetchSize());
        builder.statementType(ms.getStatementType());
        builder.keyGenerator(ms.getKeyGenerator());
        if(ms.getKeyProperties() != null)
        	builder.keyProperty(Arrays.asList(ms.getKeyProperties()).stream().collect(Collectors.joining(",")));
        builder.timeout(ms.getTimeout());
        builder.parameterMap(ms.getParameterMap());
        List<ResultMap> resultMaps = new ArrayList<ResultMap>();
        ResultMap resultMap = new ResultMap.Builder(ms.getConfiguration(), ms.getId(), Long.class, new ArrayList<ResultMapping>(0)).build();        
        resultMaps.add(resultMap);
        builder.resultMaps(resultMaps);
        builder.resultSetType(ms.getResultSetType());
        builder.cache(ms.getCache());
        builder.flushCacheRequired(ms.isFlushCacheRequired());
        builder.useCache(ms.isUseCache());

        return builder.build();		
	}
	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	@Override
	public void setProperties(Properties properties) {
	}

}
