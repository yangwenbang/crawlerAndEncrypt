package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.demo.crawler.GetCSDNInfo;

@SpringBootApplication
public class MongodbDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(MongodbDemoApplication.class, args);
		craw();
	}

	private static void craw() {
		GetCSDNInfo info = new GetCSDNInfo("https://blog.csdn.net/", "qq_24598601");
		for (int i = 0; i < 10; i++) {
			info.getUrl();
			info.getBlogInfo();
		}
	}

}
