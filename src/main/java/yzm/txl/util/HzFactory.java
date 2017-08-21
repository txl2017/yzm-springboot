package yzm.txl.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;

@Component
public class HzFactory {
	
	@Value("${hz.url}")
	private String url;
	@Value("${hz.name}")
	private String name;
	@Value("${hz.pass}")
	private String pass;
	
	@Bean
	public HazelcastInstance createInstance(){
		try{
			String[] addresses = url.split(";");
			ClientConfig cfg = new ClientConfig();
			cfg.getGroupConfig().setName(name).setPassword(pass);
			for(String address: addresses){
			    cfg.getNetworkConfig().addAddress(address);
			}
			return HazelcastClient.newHazelcastClient(cfg);
		}catch(Exception e){
			return null;
		}
	}

}
