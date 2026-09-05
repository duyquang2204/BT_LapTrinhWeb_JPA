package vn.iotstar.configs;

import vn.iotstar.dao.IUserDao;
import vn.iotstar.dao.impl.UserDaoImpl;
import vn.iotstar.entity.UserModel;

public class Test {

    public static void main(String[] args) {
        try {
            IUserDao userDao = new UserDaoImpl();

            UserModel user = userDao.findById(1);

            if (user == null) {
                System.out.println(
                        "Kết nối và truy vấn thành công, "
                        + "nhưng không có user id = 1.");
            } else {
                System.out.println("Đọc user bằng JPA thành công.");
                System.out.println("ID: " + user.getId());
                System.out.println(
                        "Username: " + user.getUsername());
                System.out.println(
                        "Fullname: " + user.getFullname());
            }
        } catch (RuntimeException e) {
            System.err.println("Kiểm tra JPA thất bại:");
            e.printStackTrace();
        } finally {
            JPAConfig.shutdown();
        }
    }
}