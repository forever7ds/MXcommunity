package com.example.mxcommunity.dao;

import com.example.mxcommunity.entity.model.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {

    public User selectUserByEmail(String email);
    public User selectUserByUsername(String username);

    public User selectUserById(long id);
    public long insertUser(User user);

    public List<User> selectAllUsers();

}
