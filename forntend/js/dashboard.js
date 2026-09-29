document.addEventListener("DOMContentLoaded", () => {

    loadUrls();

    // Create URL
    const createForm = document.getElementById("createUrlForm");

    createForm.addEventListener("submit", async (event) => {

        event.preventDefault();

        const originalUrl =
            document.getElementById("originalUrl").value;

        const customCode =
            document.getElementById("customCode").value;
        const expiresAt =
            document.getElementById("expiresAt").value;

        const message =
            document.getElementById("createMessage");
        const customCodeInput =
            document.getElementById("customCode");

        const customCodeHint =
            document.getElementById("customCodeHint");

        customCodeInput.addEventListener("input", () => {

            const value = customCodeInput.value;

            if (value.length === 0) {
                customCodeHint.textContent =
                    "3–20 characters. Leave empty to generate automatically.";

                customCodeHint.className = "input-hint";
                return;
            }

            if (value.length < 3 || value.length > 20) {
                customCodeHint.textContent =
                    "⚠ Custom short code must be 3–20 characters.";

                customCodeHint.className =
                    "input-hint input-error";

                return;
            }

            customCodeHint.textContent =
                "✓ Custom short code looks good.";

            customCodeHint.className =
                "input-hint input-success";
        });

        if (customCode && (customCode.length < 3 || customCode.length > 20)) {
            message.textContent =
                "Custom short code must be between 3 and 20 characters.";

            message.style.color = "red";
            return;
        }

        try {

            const response = await apiRequest("/api/urls", {
                method: "POST",
                body: JSON.stringify({
                    originalUrl,
                    customCode: customCode || null,
                    expiresAt: expiresAt || null
                })
            });

            if (!response) return;

            const data = await response.json();

            if (response.ok) {

                const shortUrl =
                    `${API_BASE_URL}/${data.shortCode}`;

                message.textContent =
                    `Short URL created: ${shortUrl}`;

                message.style.color = "green";

                createForm.reset();

                loadUrls();

            } else {

                message.textContent =
                    data.message || "Failed to create URL.";

                message.style.color = "red";
            }

        } catch (error) {

            console.error(error);

            message.textContent =
                "Unable to connect to the server.";

            message.style.color = "red";
        }

    });


    // Refresh URLs
    document.getElementById("refreshButton")
        .addEventListener("click", loadUrls);


    // Logout
    document.getElementById("logoutButton")
        .addEventListener("click", () => {

            localStorage.removeItem("accessToken");
            localStorage.removeItem("refreshToken");

            window.location.href = "login.html";

        });

});


// Load user's URLs
async function loadUrls() {

    const urlList =
        document.getElementById("urlList");

    try {

        const response =
            await apiRequest("/api/urls");

        if (!response) return;

        const data = await response.json();

        if (!response.ok) {

            urlList.innerHTML =
                "<p>Failed to load URLs.</p>";

            return;
        }

        if (data.length === 0) {

            urlList.innerHTML =
                "<p>No shortened URLs yet.</p>";

            return;
        }

        urlList.innerHTML = "";

        data.forEach(url => {

            const card =
                document.createElement("div");

            card.className = "url-item";

            const shortUrl = `${API_BASE_URL}/${url.shortCode}`;

            card.innerHTML = `
                <div>
                    <strong>${url.shortCode}</strong>

                    <p>
                        ${url.originalUrl}
                    </p>

                    <p>
                        Clicks: ${url.clickCount}
                    </p>
                </div>

                <div>
                    <a href="${shortUrl}" target="_blank">Open</a>
                    <a href="analytics.html?id=${url.id}">Analytics</a>
                    
                    <button class="edit-button"
                            onclick="editUrl(${url.id}, '${url.originalUrl}')">
                        Edit
                    </button>
                    
                    <button class="status-button"
                            onclick="toggleUrlStatus(${url.id}, ${url.active})">
                        ${url.active ? "Deactivate" : "Activate"}
                    </button>
                    
                    <button class="delete-button"
                            onclick="deleteUrl(${url.id})">
                        Delete
                    </button>
                    <button
                        class="qr-button"
                        onclick="showQrCode('${url.shortCode}')">
                        QR Code
                    </button>
                </div>
            `;

            urlList.appendChild(card);

        });

    } catch (error) {

        console.error(error);

        urlList.innerHTML =
            "<p>Unable to connect to the server.</p>";
    }
    window.deleteUrl = async function(urlId) {
        const confirmed = confirm(
            "Are you sure you want to delete this URL?"
        );

        if (!confirmed) {
            return;
        }

        try {
            const response = await apiRequest(`/api/urls/${urlId}`, {
                method: "DELETE"
            });

            if (!response) return;

            if (response.ok) {
                alert("URL deleted successfully.");
                loadUrls();
            } else {
                const data = await response.json().catch(() => ({}));

                alert(
                    data.message || "Failed to delete URL."
                );
            }

        } catch (error) {
            console.error(error);
            alert("Unable to connect to the server.");
        }
    }
    window.editUrl = async function(urlId, currentUrl) {

        const newUrl = prompt(
            "Enter the new original URL:",
            currentUrl
        );

        if (newUrl === null) {
            return;
        }

        if (!newUrl.trim()) {
            alert("URL cannot be empty.");
            return;
        }

        try {
            const response = await apiRequest(`/api/urls/${urlId}`, {
                method: "PUT",
                body: JSON.stringify({
                    originalUrl: newUrl.trim(),
                    expiresAt: null
                })
            });

            if (!response) return;

            if (response.ok) {
                alert("URL updated successfully.");
                loadUrls();
            } else {
                const data = await response.json().catch(() => ({}));

                alert(
                    data.message || "Failed to update URL."
                );
            }

        } catch (error) {
            console.error(error);
            alert("Unable to connect to the server.");
        }
    };
    window.toggleUrlStatus = async function(urlId, currentStatus) {

        const newStatus = !currentStatus;

        try {

            const response = await apiRequest(
                `/api/urls/${urlId}/status`,
                {
                    method: "PATCH",
                    body: JSON.stringify({
                        active: newStatus
                    })
                }
            );

            if (!response) return;

            if (response.ok) {

                alert(
                    newStatus
                        ? "URL activated successfully."
                        : "URL deactivated successfully."
                );

                loadUrls();

            } else {

                const data = await response.json().catch(() => ({}));

                alert(
                    data.message || "Failed to update URL status."
                );
            }

        } catch (error) {

            console.error(error);

            alert("Unable to connect to the server.");
        }
    };
    window.showQrCode = function(shortCode) {

        const qrUrl =
            `${API_BASE_URL}/api/qr/${shortCode}`;

        const overlay = document.createElement("div");
        overlay.className = "qr-overlay";

        overlay.innerHTML = `
        <div class="qr-modal">
            <button class="qr-close" onclick="this.closest('.qr-overlay').remove()">
                ×
            </button>

            <h2>QR Code</h2>

            <p>Scan this QR code to open your short URL.</p>

            <img src="${qrUrl}" alt="QR Code">

            <button class="qr-close-button"
                    onclick="this.closest('.qr-overlay').remove()">
                Close
            </button>
        </div>
    `;

        document.body.appendChild(overlay);
    };
}