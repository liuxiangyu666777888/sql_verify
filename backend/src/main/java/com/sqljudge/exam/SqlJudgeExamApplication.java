package com.sqljudge.exam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@MapperScan("com.sqljudge.exam")
@SpringBootApplication
public class SqlJudgeExamApplication {
    public static void main(String[] args) {
        SpringApplication.run(SqlJudgeExamApplication.class, args);
    }
}
