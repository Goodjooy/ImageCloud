package com.jacky.imagecloud;

import com.jacky.imagecloud.FileStorage.StorageProperties;
import org.hibernate.boot.model.source.spi.EmbeddableSourceContributor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class ImagecloudApplication   {

	public static void main(String[] args) {
		SpringApplication.run(ImagecloudApplication.class, args);
	}



}
