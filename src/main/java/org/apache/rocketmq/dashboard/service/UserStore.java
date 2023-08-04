package org.apache.rocketmq.dashboard.service;

import org.apache.rocketmq.dashboard.model.User;


public interface UserStore {

    User queryByName(String name);

    User queryByUsernameAndPassword(String username, String password);

}
