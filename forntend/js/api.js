const API_BASE_URL =
    window.location.hostname === "localhost"
        ? "http://localhost:8080"
        : "https://url-shortenerr-q1cl.onrender.com";
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

        console.error(
            "Authentication failed:",
            response.status,
            response.statusText
        );

        alert(
            `Authentication failed: ${response.status}`
        );

        return response;
    }

    return response;
}