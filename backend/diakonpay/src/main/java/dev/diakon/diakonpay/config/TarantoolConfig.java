package dev.diakon.diakonpay.config;

import io.tarantool.client.TarantoolClient;
import io.tarantool.client.factory.TarantoolBoxClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class TarantoolConfig {

    @Value("${tarantool.host:localhost}")
    private String host;

    @Value("${tarantool.port:3301}")
    private int port;

    @Value("${tarantool.user:admin}")
    private String user;

    @Value("${tarantool.password:tarantool_secret}")
    private String password;

    @Bean(destroyMethod = "close")
    public TarantoolClient tarantoolClient() throws Exception {
        log.info("Connecting to Tarantool at {}:{}", host, port);
        return new TarantoolBoxClientBuilder()
                .withHost(host)
                .withPort(port)
                .withUser(user)
                .withPassword(password)
                .build();
    }
}