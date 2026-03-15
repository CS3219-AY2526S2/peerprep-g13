package com.g13cs3219.server.mapper.impl;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.g13cs3219.server.dto.User;
import com.g13cs3219.server.model.UserEntity;
import com.g13cs3219.server.mapper.Mapper;

import lombok.RequiredArgsConstructor;

/**
 * Implementation of the Mapper interface for User and UserEntity.
 */
@Component
public class UserMapperImpl implements Mapper<UserEntity, User> {

    private final ModelMapper modelMapper;

    public UserMapperImpl(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public UserEntity toEntity(User user) {
        return modelMapper.map(user, UserEntity.class);
    }

    @Override
    public User toDto(UserEntity entity) {
        return modelMapper.map(entity, User.class);
    }
}
