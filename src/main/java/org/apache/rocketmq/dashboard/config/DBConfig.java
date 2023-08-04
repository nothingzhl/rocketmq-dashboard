package org.apache.rocketmq.dashboard.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import javax.sql.DataSource;
import java.sql.Driver;

/**
 * @author zhanghanlin
 * @date 2023/8/4
 **/
@Configuration
@ConfigurationProperties(prefix = "jdbc")
@Data
public class DBConfig {

    @Value("${jdbc.url}")
    String jdbcUrl;

    @Value("${jdbc.username}")
    String jdbcUsername;

    @Value("${jdbc.password}")
    String jdbcPassword;

    @Value("${jdbc.driver_class}")
    String dirver;


    @Bean
    DataSource createDataSource() throws ClassNotFoundException {
        final SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        dataSource.setUrl(jdbcUrl);
        dataSource.setUsername(jdbcUsername);
        dataSource.setPassword(jdbcPassword);
        final Class<? extends Driver> aClass = (Class<? extends Driver>)Class.forName(dirver);
        dataSource.setDriverClass(aClass);
        return dataSource;
    }

    @Bean
    JdbcTemplate createJdbcTemplate( DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

}
