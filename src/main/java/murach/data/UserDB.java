//nhận object từ User rồi  lưu dữ liệu của user
package murach.data;
import murach.business.User;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import java.io.Serializable;
import java.sql.Array;

public class UserDB
{
    private static final List<User> users = new ArrayList<>();

    public static void insert(User user)
    {
        if (user == null)
        {
            throw new IllegalArgumentException("User không được để trống");
        }
        users.add(user);
        System.out.println("Đã lưu user: " + user.getEmail());
    }

    //count user trong list
    public static int count()
    {
        return users.size();
    }

    //danh sách user đã lưu
    public static List<User> findAll()
    {
        return new ArrayList<>(users);
    }
}
