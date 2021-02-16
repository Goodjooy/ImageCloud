package com.jacky.imagecloud;

import com.jacky.imagecloud.FileStorage.StorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class ImagecloudApplication {

	public static void main(String[] args) {
		SpringApplication.run(ImagecloudApplication.class, args);
	}

}
