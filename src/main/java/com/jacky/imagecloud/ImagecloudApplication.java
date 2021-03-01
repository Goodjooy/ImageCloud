package com.jacky.imagecloud;

import com.jacky.imagecloud.configs.StorageProperties;
import com.jacky.imagecloud.configs.EmailConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({StorageProperties.class,EmailConfig.class})
public class ImagecloudApplication   {

	public static void main(String[] args) {
		SpringApplication.run(ImagecloudApplication.class, args);
	}



}
