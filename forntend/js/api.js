const API_BASE_URL = "https://url-shortenerr-q1cl.onrender.com";

async function apiRequest(endpoint, options = {}) {

    const token = localStorage.getItem("accessToken");

    const headers = {
        "Content-Type": "application/json",
        ...(options.headers || {})
    };

    if (token) {
        headers.Authorization = `Bearer ${token}`;
    }

    const response = await fetch(
        `${API_BASE_URL}${endpoint}`,
        {
            ...options,
            headers
        }
    );

    if (response.status === 401 || response.status === 403) {
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");

        window.location.href = "login.html";
        return;
    }

    return response;
}