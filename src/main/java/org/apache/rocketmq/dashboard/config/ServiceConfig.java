package org.apache.rocketmq.dashboard.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhanghanlin
 * @date 2023/8/4
 **/
@Configuration
@ConfigurationProperties(prefix = "service")
@Data
public class ServiceConfig {

    @Value("${service.userService}")
    private String userService;


}
