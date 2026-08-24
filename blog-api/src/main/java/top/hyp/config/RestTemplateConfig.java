package top.hyp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import top.hyp.config.properties.ProxyProperties;

import org.springframework.util.StringUtils;

/**
 * RestTemplate相关的Bean配置
 *
 * @author: Naccl
 * @date: 2022-01-22
 */
@Configuration
public class RestTemplateConfig {
	@Autowired(required = false)
	private ProxyProperties proxyProperties;

	/**
	 * 默认的RestTemplate
	 *
	 * @return
	 */
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	/**
	 * 配置了代理和超时时间的RestTemplate
	 *
	 * @return
	 */
	@Bean
	public RestTemplate restTemplateByProxy() {
		if (proxyProperties == null
				|| !StringUtils.hasText(proxyProperties.getHost())
				|| proxyProperties.getPort() == null) {
			return new RestTemplate();
		}

		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setProxy(new java.net.Proxy(
				java.net.Proxy.Type.HTTP,
				new java.net.InetSocketAddress(proxyProperties.getHost(), proxyProperties.getPort())));
		if (proxyProperties.getTimeout() != null) {
			requestFactory.setConnectTimeout(proxyProperties.getTimeout());
		}
		return new RestTemplate(requestFactory);
	}
}
