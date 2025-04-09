package br.com.insanos.insanosbpv;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

import java.security.Security;
import java.util.TimeZone;

@SpringBootApplication
@EnableFeignClients
public class InsanosBpvApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));
        BouncyCastleProvider provider = new BouncyCastleProvider();
        Security.addProvider(provider);
        SpringApplication.run(InsanosBpvApplication.class, args);
    }

}
