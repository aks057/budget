package com.budwiser.auth.mapper;

import com.budwiser.auth.dto.UserDto;
import com.budwiser.auth.entity.User;
import org.mapstruct.Mapper;

@Mapper
public interface IUserMapper {

  UserDto toDto(User user);
}
