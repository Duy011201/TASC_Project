package com.example.userservice.service.impl;

import com.example.userservice.dto.UserDto;
import com.example.userservice.entity.CompanyEntity;
import com.example.userservice.entity.UserEntity;
import com.example.userservice.enums.SystemRole;
import com.example.userservice.repository.CompanyRepository;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.UserService;
import com.example.userservice.util.HandleResponse;
import com.example.userservice.util.HashPassword;
import com.example.userservice.util.ReflectionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.example.userservice.config.JwtConfig;

import java.sql.Timestamp;
import java.util.List;

import static com.example.userservice.config.Constant.*;
import static com.example.userservice.util.Common.setFieldsToNull;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public ResponseEntity<HandleResponse<UserDto>> createUser (UserDto userDto) {
        UserEntity userExit = userRepository.findUserByEmail(userDto.getEmail());

        if (userExit == null) {
            UserEntity userEntity = new UserEntity();
            userEntity.setEmail(userDto.getEmail());
            userEntity.setPassword(HashPassword.encodePassword(userDto.getPassword()));
            userEntity.setRole(userDto.getRole());
            userEntity.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            userEntity.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            userEntity.setCreatedBy(userEntity.getUserID());
            userRepository.save(userEntity);
            userDto.setUserID(userEntity.getUserID());
            return ResponseEntity.ok(new HandleResponse<>(HttpStatus.OK.value(), SING_UP_SUCCESS, userDto));
        } else {
            return ResponseEntity.badRequest().body(new HandleResponse<>(HttpStatus.BAD_REQUEST.value(), ACCOUNT_EXIT));
        }
    }

    @Override
    public ResponseEntity<HandleResponse<UserDto>> getUserByID(String userID) {
        UserEntity userEntity = userRepository.findUserByID(userID);
        if (userEntity != null) {
            setFieldsToNull(userEntity, "password");
            UserDto userDto = ReflectionMapper.map(userEntity, UserDto.class);
            return ResponseEntity.ok(new HandleResponse<>(HttpStatus.OK.value(), GET_SUCCESS, userDto));
        }
        return ResponseEntity.badRequest().body(new HandleResponse<>(HttpStatus.BAD_REQUEST.value(), GET_FAIL, null));
    }

    @Override
    public ResponseEntity<HandleResponse> updateUserByID(UserDto userDto) {
        UserEntity userExit = userRepository.findUserByID(userDto.getUserID());
        if (userExit == null) {
            return ResponseEntity.badRequest().body(new HandleResponse<>(HttpStatus.BAD_REQUEST.value(), ACCOUNT_NOT_EXIT));
        }

        if (!userExit.getEmail().equals(userDto.getEmail())) {
            UserEntity userEmail = userRepository.findUserByEmail(userDto.getEmail());
            if (userEmail != null) {
                return ResponseEntity.badRequest().body(new HandleResponse<>(HttpStatus.BAD_REQUEST.value(), EMAIL_EXIT));
            }
            userExit.setEmail(userDto.getEmail());
        }

        userExit.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        userExit.setUpdatedBy(userDto.getUpdatedBy());

        userRepository.save(userExit);
        return ResponseEntity.ok(new HandleResponse<>(HttpStatus.OK.value(), UPDATE_SUCCESS, userDto));
    }

    @Override
    public ResponseEntity<HandleResponse> deleteUserByID(String userID) {
        UserEntity userEntity = userRepository.findUserByID(userID);
        if (userEntity != null) {
            userRepository.delete(userEntity);
            return ResponseEntity.ok(new HandleResponse<>(HttpStatus.OK.value(), DELETE_SUCCESS));
        }
        return ResponseEntity.badRequest().body(new HandleResponse<>(HttpStatus.BAD_REQUEST.value(), DELETE_FAIL, null));
    }

    @Override
    public ResponseEntity<HandleResponse<List<UserDto>>> getAllUser(UserDto userDto) {
        List<UserEntity> listUserEntity = userRepository.findAll();
        List<UserDto> listUserDto = ReflectionMapper.mapList(listUserEntity, UserDto.class);
        return ResponseEntity.ok(new HandleResponse<>(HttpStatus.OK.value(), GET_SUCCESS, listUserDto));
    }
}
