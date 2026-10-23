package murach.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import murach.util.JPAUtil;

public class TestJPAServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        response.setContentType(
                "text/plain; charset=UTF-8"
        );

        PrintWriter out = response.getWriter();
        EntityManager em = null;

        try {
            em = JPAUtil.getEntityManager(); //khi gọi getEntityManager thì Hibernate sẽ đọc toàn bộ Entity trong ClinicPU nên lúc này trong presistence có bao nhiêu Entity sẽ được update hết

            out.println("Kết nối JPA thành công.");
            out.println(
                    "EntityManager đang mở: "
                            + em.isOpen()
            );
        } catch (RuntimeException e) {
            log("Lỗi khởi tạo JPA", e);

            response.setStatus(
                    HttpServletResponse
                            .SC_INTERNAL_SERVER_ERROR
            );

            out.println("Kết nối JPA thất bại.");
            out.println(e.getMessage());
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
}