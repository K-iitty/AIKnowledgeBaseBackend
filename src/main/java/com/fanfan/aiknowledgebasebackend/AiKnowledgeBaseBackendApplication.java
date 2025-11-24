package com.fanfan.aiknowledgebasebackend;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@MapperScan("com.fanfan.aiknowledgebasebackend.mapper")
public class AiKnowledgeBaseBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiKnowledgeBaseBackendApplication.class, args);
    }
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AI知识库API文档")
                        .version("1.0.0")
                        .description("基于Vue3和Spring Boot的智能问答知识库系统")
                        .contact(new Contact()
                                .name("技术支持")
                                .email("support@aiknowledgebase.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")));
    }

}
