// --- Заглушка пользователей ---
const users = {
    "direc": { password: "direc1234", role: "DIRECTOR" },
    "teacher": { password: "teach123", role: "TEACHER" },
    "student": { password: "stud123", role: "STUDENT" }
};

// --- Логин ---
function login() {
    const login = document.getElementById("login").value.trim();
    const password = document.getElementById("password").value.trim();

    if (users[login] && users[login].password === password) {

        // сохраняем данные в браузер
        localStorage.setItem("username", login);
        localStorage.setItem("role", users[login].role);

        // переход на главную
        window.location.href = "home.html";
    } else {
        alert("Неверный логин или пароль!");
    }
}

// --- Проверка роли на странице ---
function checkRole(allowedRoles) {
    const role = localStorage.getItem("role");

    if (!role || !allowedRoles.includes(role)) {
        window.location.href = "no-rights.html";
    }
}

// --- Получение текущего пользователя ---
function getCurrentRole() {
    return localStorage.getItem("role");
}

function getCurrentUser() {
    return localStorage.getItem("username");
}
