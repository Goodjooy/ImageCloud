package com.jacky.imagecloud;

import com.jacky.imagecloud.controller.SecurityController;
import com.jacky.imagecloud.data.Result;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ImagecloudApplicationTests {

	@Autowired
	SecurityController controller;
	@Test
	void contextLoads() {
	}
	@Test
	void emailCheck(){
		assertEquals(controller.CheckEmailExist("12212123@qq.com").err,new Result<Boolean>(true).err);
		assertTrue(controller.CheckEmailExist("23234").err);
	}

}
