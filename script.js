/* ================= PAGE ================= */

function showPage(pageId) {

    document.querySelectorAll(".page")
        .forEach(page => {
            page.classList.remove("active");
        });

    const page = document.getElementById(pageId);

    if (page) {
        page.classList.add("active");
    }

    // Nếu mở trang đăng ký khám
    if (pageId === "appointment") {
        checkLogin();
    }

    window.scrollTo(0, 0);
}


/* ================= REGISTER ================= */

function register(event) {

    event.preventDefault();

    const name =
        document.getElementById("registerName").value;

    const email =
        document.getElementById("registerEmail").value;

    const phone =
        document.getElementById("registerPhone").value;

    const password =
        document.getElementById("registerPassword").value;


    const user = {
        name: name,
        email: email,
        phone: phone,
        password: password
    };


    localStorage.setItem(
        "UCareUser",
        JSON.stringify(user)
    );


    alert(
        "Đăng ký tài khoản thành công!"
    );


    showPage("login");
}


/* ================= USER LOGIN ================= */

function login(event) {

    event.preventDefault();

    const email =
        document.getElementById("loginEmail").value;

    const password =
        document.getElementById("loginPassword").value;


    const savedUser =
        JSON.parse(
            localStorage.getItem("UCareUser")
        );


    if (!savedUser) {

        alert(
            "Bạn chưa có tài khoản. Vui lòng đăng ký!"
        );

        return;
    }


    if (
        email === savedUser.email &&
        password === savedUser.password
    ) {

        localStorage.setItem(
            "loggedIn",
            "true"
        );


        alert(
            "Đăng nhập thành công!"
        );


        updateLoginStatus();

        showPage("profile");

    } else {

        alert(
            "Email hoặc mật khẩu không đúng!"
        );

    }
}


/* ================= CHECK LOGIN ================= */

function checkLogin() {

    const loggedIn =
        localStorage.getItem("loggedIn");


    const appointmentForm =
        document.getElementById(
            "appointmentForm"
        );

    const loginRequired =
        document.getElementById(
            "loginRequired"
        );


    if (loggedIn === "true") {

        appointmentForm.classList.remove(
            "hidden"
        );

        loginRequired.classList.add(
            "hidden"
        );


        // Điền thông tin người dùng
        const user =
            JSON.parse(
                localStorage.getItem(
                    "UCareUser"
                )
            );


        if (user) {

            document.getElementById(
                "patientName"
            ).value = user.name;


            document.getElementById(
                "patientPhone"
            ).value = user.phone;

        }

    } else {

        appointmentForm.classList.add(
            "hidden"
        );

        loginRequired.classList.remove(
            "hidden"
        );

    }
}


/* ================= UPDATE HEADER ================= */

function updateLoginStatus() {

    const loggedIn =
        localStorage.getItem("loggedIn");

    const userStatus =
        document.getElementById(
            "userStatus"
        );

    const loginBtn =
        document.getElementById(
            "loginBtn"
        );

    const logoutBtn =
        document.getElementById(
            "logoutBtn"
        );


    if (loggedIn === "true") {

        const user =
            JSON.parse(
                localStorage.getItem(
                    "UCareUser"
                )
            );


        userStatus.innerText =
            user ? user.name : "Người dùng";


        loginBtn.classList.add(
            "hidden"
        );

        logoutBtn.classList.remove(
            "hidden"
        );

    } else {

        userStatus.innerText =
            "Khách";

        loginBtn.classList.remove(
            "hidden"
        );

        logoutBtn.classList.add(
            "hidden"
        );

    }
}


/* ================= PROFILE ================= */

function loadProfile() {

    const loggedIn =
        localStorage.getItem("loggedIn");


    if (loggedIn !== "true") {

        showPage("login");

        return;
    }


    const user =
        JSON.parse(
            localStorage.getItem(
                "UCareUser"
            )
        );


    if (!user) return;


    document.getElementById(
        "profileName"
    ).innerText = user.name;


    document.getElementById(
        "profileEmail"
    ).innerText = user.email;


    document.getElementById(
        "profilePhone"
    ).innerText = user.phone;
}


/* ================= APPOINTMENT ================= */

function bookAppointment(event) {

    event.preventDefault();


    if (
        localStorage.getItem("loggedIn")
        !== "true"
    ) {

        alert(
            "Bạn phải đăng nhập trước!"
        );

        showPage("login");

        return;
    }


    const appointment = {

        id:
            "APP" +
            Date.now(),

        specialty:
            document.getElementById(
                "specialty"
            ).value,

        status:
            "Chờ khám"

    };


    localStorage.setItem(
        "appointment",
        JSON.stringify(appointment)
    );


    alert(
        "Đăng ký khám thành công!"
    );


    showPage("profile");

    loadAppointments();
}


/* ================= APPOINTMENT LIST ================= */

function loadAppointments() {

    const appointment =
        JSON.parse(
            localStorage.getItem(
                "appointment"
            )
        );


    const table =
        document.getElementById(
            "myAppointments"
        );


    if (!table) return;


    table.innerHTML = "";


    if (!appointment) {

        table.innerHTML = `
            <tr>
                <td colspan="5">
                    Chưa có lịch khám
                </td>
            </tr>
        `;

        return;
    }


    table.innerHTML = `

        <tr>

            <td>
                ${appointment.id}
            </td>

            <td>
                ${appointment.specialty}
            </td>

            <td>
                Nguyễn Minh Anh
            </td>

            <td>
                Chưa xác định
            </td>

            <td>
                🟡 ${appointment.status}
            </td>

        </tr>

    `;
}


/* ================= STAFF LOGIN ================= */

function staffLogin(event) {

    event.preventDefault();


    const username =
        document.getElementById(
            "staffUsername"
        ).value;

    const password =
        document.getElementById(
            "staffPassword"
        ).value;

    const role =
        document.getElementById(
            "staffRole"
        ).value;


    if (
        username === "admin" &&
        password === "123456" &&
        role === "admin"
    ) {

        localStorage.setItem(
            "staffLogin",
            "admin"
        );


        alert(
            "Đăng nhập Admin thành công!"
        );


        showPage("admin");

        return;
    }


    if (
        username === "doctor" &&
        password === "123456" &&
        role === "doctor"
    ) {

        localStorage.setItem(
            "staffLogin",
            "doctor"
        );


        alert(
            "Đăng nhập Bác sĩ thành công!"
        );


        showPage("doctor");

        return;
    }


    alert(
        "Thông tin đăng nhập không chính xác!"
    );
}


/* ================= LOGOUT ================= */

function logout() {

    localStorage.removeItem(
        "loggedIn"
    );

    localStorage.removeItem(
        "staffLogin"
    );


    updateLoginStatus();

    alert(
        "Đã đăng xuất!"
    );

    showPage("home");
}


/* ================= START ================= */

document.addEventListener(
    "DOMContentLoaded",
    function () {

        updateLoginStatus();

        loadProfile();

        loadAppointments();

    }
);