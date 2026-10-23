package murach.controller.nurse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import murach.DAO.InitialAssessmentDB;
import murach.business.Account;
import murach.controller.clinical.ClinicalWeb;

public class InitialAssessmentServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    private final InitialAssessmentDB assessmentDB =
            new InitialAssessmentDB();

    private static final ZoneId ZONE =
            ZoneId.of("Asia/Ho_Chi_Minh");

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException
    {
        try
        {
            long[] identity = resolveIdentity(request, response);
            if (identity == null) return;
            long nurseId = identity[0];
            long userId = identity[1];
            LocalDate selectedDate = readDate(request);
            request.setAttribute("selectedDate", selectedDate);

            String encounterValue = request.getParameter("encounterId");

            if (encounterValue == null
                    || encounterValue.trim().isEmpty())
            {
                request.setAttribute(
                        "encounters",
                        assessmentDB.findWaiting(
                                nurseId, userId, selectedDate));

                request.getRequestDispatcher(
                                "/WEB-INF/views/nurse/danh-sach-benh-nhan.jsp")
                        .forward(request, response);
                return;
            }

            Long encounterId = positiveId(
                    encounterValue, "Mã lượt khám");

            request.setAttribute(
                    "encounter",
                    assessmentDB.findEncounterForNurse(
                            encounterId, nurseId, userId));

            request.setAttribute(
                    "assessment",
                    assessmentDB.findAssessmentForNurse(
                            encounterId, nurseId, userId));

            request.getRequestDispatcher(
                            "/WEB-INF/views/nurse/danh-gia-ban-dau.jsp")
                    .forward(request, response);
        }
        catch (IllegalArgumentException e)
        {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
        catch (IllegalStateException e)
        {
            response.sendError(
                    HttpServletResponse.SC_CONFLICT, e.getMessage());
        }
        catch (RuntimeException e)
        {
            ClinicalWeb.fail(response, e);
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException
    {
        request.setCharacterEncoding("UTF-8");

        try
        {
            long[] identity = resolveIdentity(request, response);
            if (identity == null) return;
            ClinicalWeb.requireCsrf(request);
            long nurseId = identity[0];
            long userId = identity[1];
            Long encounterId = positiveId(
                    request.getParameter("encounterId"),
                    "Mã lượt khám");

            LocalDate selectedDate = readDate(request);
            String action = request.getParameter("action");

            if ("begin".equals(action))
            {
                assessmentDB.startAssessment(
                        encounterId, nurseId, userId);
            }
            else if ("save".equals(action)
                    || "complete".equals(action))
            {
                assessmentDB.saveAssessment(
                        encounterId,
                        nurseId,
                        userId,
                        request.getParameter("chiefComplaint"),
                        decimal(request, "height", "Chiều cao"),
                        decimal(request, "weight", "Cân nặng"),
                        decimal(request, "temperature", "Nhiệt độ"),
                        integer(request, "systolicPressure",
                                "Huyết áp tâm thu"),
                        integer(request, "diastolicPressure",
                                "Huyết áp tâm trương"),
                        integer(request, "heartRate", "Nhịp tim"),
                        integer(request, "respiratoryRate", "Nhịp thở"),
                        decimal(request, "spo2", "SpO2"),
                        request.getParameter("note"),
                        "complete".equals(action));
            }
            else
            {
                throw new IllegalArgumentException(
                        "Thao tác không hợp lệ");
            }

            String destination = request.getContextPath()
                    + "/nurse/assessment?workDate=" + selectedDate;

            if ("complete".equals(action))
            {
                destination += "&completed=1";
            }
            else
            {
                destination += "&encounterId=" + encounterId;

                if ("save".equals(action))
                    destination += "&saved=1";
            }

            response.sendRedirect(destination);
        }
        catch (IllegalArgumentException e)
        {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
        catch (IllegalStateException e)
        {
            response.sendError(
                    HttpServletResponse.SC_CONFLICT, e.getMessage());
        }
        catch (RuntimeException e)
        {
            ClinicalWeb.fail(response, e);
        }
    }

    // Chỉ lấy danh tính từ phiên đăng nhập thật; không dùng cờ JVM để bỏ qua đăng nhập.
    private long[] resolveIdentity(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute("userId") instanceof Number))
        {
            response.sendRedirect(request.getContextPath() + "/staff-login");
            return null;
        }
        if (!"NURSE".equals(session.getAttribute("role"))
                || !(session.getAttribute("nurseId") instanceof Number))
        {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Chức năng dành cho điều dưỡng");
            return null;
        }
        if (ClinicalWeb.requireUser(request, response, Account.Role.NURSE) == null) return null;
        return new long[] {ClinicalWeb.profileId(request, "nurseId"),
                ((Number) session.getAttribute("userId")).longValue()};
    }

    private LocalDate readDate(HttpServletRequest request)
    {
        String value = request.getParameter("workDate");

        if (value == null || value.trim().isEmpty())
            return LocalDate.now(ZONE);

        try
        {
            return LocalDate.parse(value.trim());
        }
        catch (java.time.format.DateTimeParseException e)
        {
            throw new IllegalArgumentException("Ngày khám không hợp lệ");
        }
    }

    private Long positiveId(String value, String label)
    {
        try
        {
            long id = Long.parseLong(value == null ? "" : value.trim());

            if (id <= 0)
                throw new NumberFormatException();

            return id;
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException(label + " không hợp lệ");
        }
    }

    private Double decimal(
            HttpServletRequest request, String name, String label)
    {
        String value = request.getParameter(name);

        if (value == null || value.trim().isEmpty())
            return null;

        try
        {
            double number = Double.parseDouble(value.trim());

            if (!Double.isFinite(number))
                throw new NumberFormatException();

            return number;
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException(
                    label + " phải là số hợp lệ");
        }
    }

    private Integer integer(
            HttpServletRequest request, String name, String label)
    {
        String value = request.getParameter(name);

        if (value == null || value.trim().isEmpty())
            return null;

        try
        {
            return Integer.valueOf(value.trim());
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException(
                    label + " phải là số nguyên hợp lệ");
        }
    }
}
