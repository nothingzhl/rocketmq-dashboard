/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.dashboard.service.impl;

import com.alibaba.excel.util.StringUtils;
import org.apache.rocketmq.dashboard.config.RMQConfigure;
import org.apache.rocketmq.dashboard.config.ServiceConfig;
import org.apache.rocketmq.dashboard.exception.ServiceException;
import org.apache.rocketmq.dashboard.model.User;
import org.apache.rocketmq.dashboard.service.UserStore;
import org.apache.rocketmq.dashboard.service.UserService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.io.FileReader;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserServiceImpl implements UserService , InitializingBean {
    @Resource
    private RMQConfigure configure;

    private UserStore userStore;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public User queryByName(String name) {
        return userStore.queryByName(name);
    }

    @Override
    public User queryByUsernameAndPassword(String username, String password) {
        return userStore.queryByUsernameAndPassword(username, password);
    }



    @Override
    public void afterPropertiesSet() throws Exception {
        if (configure.isLoginRequired()) {
            if (!StringUtils.isEmpty(configure.getUserService())&&configure.getUserService().startsWith("DB")){
                userStore = applicationContext.getBean("dbUserService",UserStore.class);
            }else {
                userStore =  new FileBasedUserInfoStore(configure);
            }
        }
    }

    public static class FileBasedUserInfoStore extends AbstractFileStore implements UserStore {
        private static final String FILE_NAME = "users.properties";

        public static Map<String, User> userMap = new ConcurrentHashMap<>();

        public FileBasedUserInfoStore(RMQConfigure configure) {
            super(configure, FILE_NAME);
        }

        @Override
        public void load(InputStream inputStream) {
            Properties prop = new Properties();
            try {
                if (inputStream == null) {
                    prop.load(new FileReader(filePath));
                } else {
                    prop.load(inputStream);
                }
            } catch (Exception e) {
                log.error("load user.properties failed", e);
                throw new ServiceException(0, String.format("Failed to load loginUserInfo property file: %s", filePath));
            }

            Map<String, User> loadUserMap = new HashMap<>();
            String[] arrs;
            int role;
            for (String key : prop.stringPropertyNames()) {
                String v = prop.getProperty(key);
                if (v == null)
                    continue;
                arrs = v.split(",", 2);
                if (arrs.length == 0) {
                    continue;
                } else if (arrs.length == 1) {
                    role = 0;
                } else {
                    role = Integer.parseInt(arrs[1].trim());
                }

                loadUserMap.put(key, new User(key, arrs[0].trim(), role));
            }

            userMap.clear();
            userMap.putAll(loadUserMap);
        }

        @Override
        public User queryByName(String name) {
            return userMap.get(name);
        }

        @Override
        public User queryByUsernameAndPassword(@NotNull String username, @NotNull String password) {
            User user = queryByName(username);
            if (user != null && password.equals(user.getPassword())) {
                return user.cloneOne();
            }
            return null;
        }
    }


    @Component(value = "dbUserService")
    @ConditionalOnProperty(value = "service.userService",havingValue = "DB")
    public static class DBUserInfoStore implements UserStore,InitializingBean{

        @Autowired
        private JdbcTemplate jdbcTemplate;

        @Resource
        private ServiceConfig serviceConfig;

        @Override
        public User queryByName(String name) {
            if (StringUtils.isEmpty(name)){
                return null;
            }
            return jdbcTemplate.queryForObject("select * from user where name = ?",
                new BeanPropertyRowMapper<>(User.class), name);
        }

        @Override
        public User queryByUsernameAndPassword(String username, String password) {
            if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)){
                return null;
            }
            return jdbcTemplate.queryForObject("select * from user where name = ? and password=?",
                new BeanPropertyRowMapper<>(User.class), username,password);
        }

        @Override
        public void afterPropertiesSet() throws Exception {
           initDB();
        }

        private void initDB() {

            jdbcTemplate.execute("create table user(\n" + "d int primary key not null ,\n"
                + "                     name varchar(30) not null ,\n" + "password varchar(30) not null\n" + ")");



        }

    }


}
