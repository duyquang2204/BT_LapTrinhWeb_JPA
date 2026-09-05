package vn.iotstar.services.impl;

import vn.iotstar.utils.PasswordUtil;
import vn.iotstar.dao.IUserDao;
import vn.iotstar.dao.impl.UserDaoImpl;
import vn.iotstar.entity.UserModel;
import vn.iotstar.services.IUserService;

public class UserService implements IUserService{
	// Lấy toàn bộ hàm trong tầng Dao của user
	IUserDao userDao = new UserDaoImpl();
	@Override
	public UserModel login(String username, String password) {
	    if (username == null
	            || username.isBlank()
	            || password == null
	            || password.isEmpty()) {
	        return null;
	    }

	    UserModel user = userDao.findByUserName(username.trim());

	    if (user == null) {
	        return null;
	    }

	    if (!PasswordUtil.verify(password, user.getPassword())) {
	        return null;
	    }

	    if (user.getActive() != 1) {
	        return null;
	    }

	    return user;
	}

	@Override
	public UserModel findByUserName(String username) {
		
		return userDao.findByUserName(username);
	}
	
	@Override
	public UserModel findById(int id) {
	    return userDao.findById(id);
	}

	@Override
	public void updateProfile(
	        int userId,
	        String fullname,
	        String phone,
	        String images) {

	    if (userId <= 0) {
	        throw new IllegalArgumentException(
	                "Tài khoản không hợp lệ.");
	    }

	    String normalizedName = fullname == null
	            ? ""
	            : fullname.trim();

	    String normalizedPhone = phone == null
	            ? ""
	            : phone.trim();

	    if (normalizedName.isBlank()) {
	        throw new IllegalArgumentException(
	                "Họ tên không được để trống.");
	    }

	    if (normalizedName.length() > 100) {
	        throw new IllegalArgumentException(
	                "Họ tên không được vượt quá 100 ký tự.");
	    }

	    if (!normalizedPhone.matches("0[0-9]{9}")) {
	        throw new IllegalArgumentException(
	                "Số điện thoại phải gồm 10 chữ số, bắt đầu bằng 0.");
	    }

	    userDao.updateProfile(
	            userId,
	            normalizedName,
	            normalizedPhone,
	            images);
	}
}
