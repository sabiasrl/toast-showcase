package com.toast.api.config;

import com.toast.api.service.UserService;
import com.toast.api.service.impl.UserServiceImpl;
import com.toast.common.repository.UserRepository;
import dagger.Module;
import dagger.Provides;
import org.springframework.context.annotation.Configuration;

import javax.inject.Singleton;

@Module
@Configuration
public class DaggerConfig {

    @Provides
    @Singleton
    public UserService userService(UserRepository userRepository) {
        return new UserServiceImpl(userRepository);
    }
} 