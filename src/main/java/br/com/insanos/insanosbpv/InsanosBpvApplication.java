package br.com.insanos.insanosbpv;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.TimeZone;

@SpringBootApplication(scanBasePackages = {"br.com.insanos.insanosbpv"})
@ComponentScan(basePackages = {"br.com.insanos.insanosbpv.**.queue"})
@Configuration
@ComponentScan(basePackages = {
        //"br.com.insanos.insanosbpv.**.config",
        "br.com.insanos.insanosbpv.**.service",
        //"br.com.insanos.insanosbpv.**.controller",
        //"br.com.insanos.insanosbpv.**.client",
        "br.com.insanos.insanosbpv.**.queue"
})
@EntityScan(basePackages = {"br.com.uniproof.integration.**.bean"})
@EnableTransactionManagement
//@EnableRetry
@Retryable
@EnableWebSecurity
@Slf4j
public class InsanosBpvApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));
        SpringApplication.run(InsanosBpvApplication.class, args);
    }

    @Bean
    public MessageConverter jsonFinancialMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

}