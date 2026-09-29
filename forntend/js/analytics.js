document.addEventListener("DOMContentLoaded", () => {

    const params = new URLSearchParams(window.location.search);
    const urlId = params.get("id");

    if (!urlId) {
        document.getElementById("urlDetails").innerHTML =
            "<p>No URL selected.</p>";
        return;
    }

    loadAnalytics(urlId);

    document.getElementById("backButton")
        .addEventListener("click", () => {
            window.location.href = "dashboard.html";
        });
});


async function loadAnalytics(urlId) {

    try {

        const response =
            await apiRequest(`/api/urls/${urlId}/analytics`);

        if (!response) return;

        const data = await response.json();

        if (!response.ok) {

            document.getElementById("urlDetails").innerHTML =
                `<p>${data.message || "Unable to load analytics."}</p>`;

            return;
        }


        // URL Details

        const shortUrl =
            `${API_BASE_URL}/${data.shortCode}`;

        document.getElementById("urlDetails").innerHTML = `
            <p>
                <strong>Short URL:</strong>
                <a href="${shortUrl}" target="_blank">
                    ${shortUrl}
                </a>
            </p>

            <p>
                <strong>Original URL:</strong>
                ${data.originalUrl}
            </p>
        `;


        // Click Statistics

        document.getElementById("clickStats").innerHTML = `

            <p>
                <strong>Total Clicks:</strong>
                ${data.totalClicks}
            </p>

            <p>
                <strong>Today:</strong>
                ${data.clicksToday}
            </p>

            <p>
                <strong>This Week:</strong>
                ${data.clicksThisWeek}
            </p>

            <p>
                <strong>This Month:</strong>
                ${data.clicksThisMonth}
            </p>

        `;


        // Browser Statistics

        displayStats(
            "browserStats",
            data.browsers
        );


        // Operating System Statistics

        displayStats(
            "osStats",
            data.operatingSystems
        );


        // Device Statistics

        displayStats(
            "deviceStats",
            data.devices
        );

    } catch (error) {

        console.error(error);

        document.getElementById("urlDetails").innerHTML =
            "<p>Unable to connect to the server.</p>";
    }
}


function displayStats(elementId, stats) {

    const container =
        document.getElementById(elementId);

    if (!stats || Object.keys(stats).length === 0) {

        container.innerHTML =
            "<p>No data available.</p>";

        return;
    }

    container.innerHTML = "";

    Object.entries(stats).forEach(([name, count]) => {

        const item =
            document.createElement("p");

        item.innerHTML = `
            <strong>${name}:</strong> ${count}
        `;

        container.appendChild(item);

    });
}