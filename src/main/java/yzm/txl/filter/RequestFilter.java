package yzm.txl.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.hazelcast.core.HazelcastInstance;

/**
 * rest请求过滤器
 *
 * 创建日期 2017年8月2日
 * 
 * @author txl
 * @since $version$
 */
@WebFilter(urlPatterns = "/yzm/*", filterName = "yzmFilter")
public class RequestFilter implements Filter {

	private Logger logger = org.slf4j.LoggerFactory.getLogger(RequestFilter.class);
	@Autowired
	private HazelcastInstance instance;
	@Autowired
	private MongoTemplate mongo;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		logger.debug("Filter started.");
	}

	@Override
	public void destroy() {
		logger.debug("Filter stoped.");
	}
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		logger.debug("instance = "+instance);
		logger.debug("mongo = "+mongo.getCollectionNames().size());
		chain.doFilter(request, response);
	}

}
