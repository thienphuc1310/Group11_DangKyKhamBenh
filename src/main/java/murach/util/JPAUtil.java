package murach.util;


import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

//final: không cho các class ngoài kế thừa
public final class JPAUtil
{
    //Công cụ để tìm kiếm, thêm, sửa, xoá dữ liệu dưới database: EntityManager
    private static final EntityManagerFactory FACTORY = Persistence.createEntityManagerFactory("ClinicPU");

    //tạo luồng cho ngoài có thể vào
    public static EntityManager getEntityManager()
    {
        return FACTORY.createEntityManager();
    }

    private JPAUtil()
    {

    }

    public static void close()
    {
        if (FACTORY.isOpen())
        {
            FACTORY.close();
        }
    }

}

//tiện ích duùng để khởi tạo và cung cấp công cụ thực hiện tác  vụ
//nó sẽ tạo ra EntityManager và nó được dùng bên trong DAO