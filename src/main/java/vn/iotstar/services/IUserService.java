package vn.iotstar.services;

import vn.iotstar.entity.UserModel;

public interface IUserService {
	UserModel login(String username, String password);

	UserModel findByUserName(String username);
	
	UserModel findById(int id);

	void updateProfile(int userId, String fullname, String phone, String images);
	
}