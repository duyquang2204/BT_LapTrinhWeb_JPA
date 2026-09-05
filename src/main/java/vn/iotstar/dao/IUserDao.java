package vn.iotstar.dao;

import java.util.List;

import vn.iotstar.entity.UserModel;

public interface IUserDao {

    List<UserModel> findAll();

    UserModel findById(int id);

    UserModel findByUserName(String username);

    void insert(UserModel user);
    
    void updateProfile(int userId, String fullname, String phone, String images);
}