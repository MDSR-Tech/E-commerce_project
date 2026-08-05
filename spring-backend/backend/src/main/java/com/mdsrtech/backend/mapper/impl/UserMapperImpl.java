package com.mdsrtech.backend.mapper.impl;

import com.mdsrtech.backend.domain.dtos.entities.UserDTO;
import com.mdsrtech.backend.domain.entities.User;
import com.mdsrtech.backend.mapper.Mapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class UserMapperImpl implements Mapper<User, UserDTO> {

    private final ModelMapper modelMapper;

    public UserMapperImpl(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public UserDTO mapFromEntityToDTO(User user) {
        return modelMapper.map(user, UserDTO.class);
    }

    @Override
    public User mapFromDTOToEntity(UserDTO userDTO) {
        return modelMapper.map(userDTO, User.class);
    }

}
