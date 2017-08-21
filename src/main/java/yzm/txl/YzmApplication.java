package yzm.txl;

import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * <p>验证码相关服务应用入口。</p>
 *
 * 创建日期 2017年8月2日
 * @author txl
 * @since $version$
 */
@SpringBootApplication
@ServletComponentScan
@ApplicationPath("/yzm")
public class YzmApplication extends Application{
	
    public static void main(String[] args) throws Exception {
        new SpringApplicationBuilder(YzmApplication.class).bannerMode(Banner.Mode.OFF).run(args);
    }
    
}
