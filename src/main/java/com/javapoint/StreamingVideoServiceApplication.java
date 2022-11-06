package com.javapoint;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import com.javapoint.config.VideoStorageProperties;

@SpringBootApplication
@EnableConfigurationProperties({
		VideoStorageProperties.class
})
public class StreamingVideoServiceApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(StreamingVideoServiceApplication.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(StreamingVideoServiceApplication.class);
	}
}
