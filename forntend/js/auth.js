const API_BASE_URL = "https://url-shortenerr-q1cl.onrender.com";

// =========================
// REGISTER
// =========================

const registerForm = document.getElementById("registerForm");

if (registerForm) {

    registerForm.addEventListener("submit", async function (event) {

        event.preventDefault();

        const name = document.getElementById("name").value.trim();
        const email = document.getElementById("email").value.trim();
        const password = document.getElementById("password").value;

        const errorMessage =
            document.getElementById("errorMessage");

        errorMessage.textContent = "";

        try {

            const response = await fetch(
                `${API_BASE_URL}/api/auth/register`,
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({
                        name: name,
                        email: email,
                        password: password
                    })
                }
            );

            if (response.ok) {

                alert("Registration successful!");

                window.location.href = "login.html";

                return;
            }

            let message = "Registration failed.";

            try {
                const data = await response.json();

                message =
                    data.message ||
                    data.error ||
                    message;

            } catch (error) {
                // Response has no JSON body
            }

            errorMessage.textContent =
                `${message} (${response.status})`;

        } catch (error) {

            console.error("Registration error:", error);

            errorMessage.textContent =
                "Unable to connect to the server.";
        }

    });
}


// =========================
// LOGIN
// =========================

const loginForm = document.getElementById("loginForm");

if (loginForm) {

    loginForm.addEventListener("submit", async function (event) {

        event.preventDefault();

        const email =
            document.getElementById("email").value.trim();

        const password =
            document.getElementById("password").value;

        const errorMessage =
            document.getElementById("errorMessage");

        errorMessage.textContent = "";

        try {

            const response = await fetch(
                `${API_BASE_URL}/api/auth/login`,
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({
                        email: email,
                        password: password
                    })
                }
            );

            const data = await response.json();

            if (response.ok) {
                localStorage.setItem("accessToken", data.token);
                localStorage.setItem("refreshToken", data.refreshToken);
                window.location.href = "dashboard.html";
                return;
            }

            errorMessage.textContent =
                data.message ||
                data.error ||
                `Login failed (${response.status})`;

        } catch (error) {

            console.error("Login error:", error);

            errorMessage.textContent =
                "Unable to connect to the server.";
        }

    });
}