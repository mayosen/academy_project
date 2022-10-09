package com.mayosen.academy;

import com.mayosen.academy.controllers.MainController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AcademyProjectApplicationTests {
    private MainController controller;

    @Autowired
    AcademyProjectApplicationTests(MainController controller) {
        this.controller = controller;
    }

    @Test
    void contextLoads() {
        assertThat(controller).isNotNull();
    }

}
