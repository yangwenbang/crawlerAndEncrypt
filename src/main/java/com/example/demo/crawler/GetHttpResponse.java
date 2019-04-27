package com.example.demo.crawler;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;

public class GetHttpResponse {

	public static HttpResponse getHttpClient(String url) throws ClientProtocolException, IOException {
		//创建HttpClient对象实例
		HttpClient httpClient = HttpClients.createDefault();
		
		//创建get请求对象
		HttpGet httpGet = new HttpGet(url);
		
		RequestConfig config = RequestConfig.custom()
				.setConnectTimeout(5000)  			//设置响应时间
				.setConnectionRequestTimeout(5000)  //设置请求超时
				.setCookieSpec(CookieSpecs.IGNORE_COOKIES) //设置cookie策略
				.build();
		httpGet.setConfig(config);
		
		//设置头信息，不然请求不到网页
        httpGet.setHeader("Accept-Language", "zh-CN,zh;q=0.8");
        httpGet.setHeader("User-Agent", 
        		"Mozilla/5.0 (Windows NT 6.1; rv:6.0.2) Gecko/20100101 Firefox/6.0.2");  
        
        HttpResponse httpResponse = httpClient.execute(httpGet);;
        
        return httpResponse;
	}
}

