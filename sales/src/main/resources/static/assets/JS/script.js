// ------------ BASE URL -------------
const BASE = "http://localhost:9090/api/v1/dashboard";

let salesChart = null;
let topPerformanceChart = null;

async function safeFetchJson(url, options = {}) {
    try {
        // Attach Authorization header automatically when token exists
        const token = localStorage.getItem('authToken');
        options.headers = options.headers || {};
        if (token && !options.headers['Authorization'] && !options.headers['authorization']) {
            options.headers['Authorization'] = `Bearer ${token}`;
        }

        let res = await fetch(url, options);
        if (!res.ok) {
            console.warn("Fetch not ok", url, "->", res.status);
            return null;
        }

        const body = await res.json();
        // Unwrap CommonUtil-style responses { status, message, data }
        if (body && typeof body === 'object' && ('status' in body || 'message' in body)) {
            return body.data !== undefined ? body.data : null;
        }
        return body;
    } catch (e) {
        console.error("Fetch error:", url, e);
        return null;
    }
}

// ================== DASHBOARD STATS (Counts) ==================
async function loadDashboardStats() {
    console.log("Loading dashboard stats...");
    const data = await safeFetchJson(`${BASE}/stats`);
    if (!data) {
        console.error("Failed to load stats");
        return;
    }

    console.log("Stats data:", data);
    
    // Set employee count
    const empCount = document.getElementById("employeeCount");
    if (empCount) empCount.textContent = (data.employeeCount ?? data.employeeCount === 0) ? data.employeeCount : 0;
    
    // Set meet target percentage
    const meetTargetEl = document.getElementById("meetTarget");
    const meetVal = data.meetTarget ?? data.meetTarget === 0 ? data.meetTarget : (data.meetTargetPercentage || 0);
    if (meetTargetEl) meetTargetEl.textContent = `${meetVal}%`;
    
    // Set sales target
    const salesTargetEl = document.getElementById("salesTarget");
    if (salesTargetEl) salesTargetEl.textContent = (data.salesTarget ?? 0);
    
    // Set sales achieved
    const salesAchievedEl = document.getElementById("salesAchieved");
    if (salesAchievedEl) salesAchievedEl.textContent = (data.salesAchieved ?? 0);
}

// ================== SALES OVERVIEW CHART ==================
async function loadSalesOverview() {
    console.log("Loading sales overview...");
    const data = await safeFetchJson(`${BASE}/sales-overview`);
    if (!data) {
        console.error("Failed to load sales overview");
        return;
    }

    console.log("Sales overview data:", data);

    const ctx = document.getElementById("salesChart");
    if (!ctx) {
        console.error("Sales chart canvas not found");
        return;
    }

    // Destroy old chart if exists
    if (salesChart) {
        salesChart.destroy();
    }

    const labels = data.labels || [];
    const chartData = data.data || [];

    salesChart = new Chart(ctx, {
        type: "line",
        data: {
            labels: labels,
            datasets: [
                {
                    label: "Monthly Sales",
                    data: chartData,
                    borderColor: "#0d6efd",
                    backgroundColor: "rgba(13, 110, 253, 0.1)",
                    borderWidth: 2,
                    fill: true,
                    tension: 0.4
                }
            ]
        },
        options: {
            responsive: true,
            plugins: {
                legend: {
                    display: true,
                    position: "top"
                }
            },
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });
}

// ================== RECENT SALES ==================
async function loadRecentSales() {
    console.log("Loading recent sales...");
    const data = await safeFetchJson(`${BASE}/recent-sales`);
    if (!data || !Array.isArray(data)) {
        console.error("Failed to load recent sales");
        return;
    }

    console.log("Recent sales data:", data);

    const tbody = document.getElementById("salesTableBody");
    if (!tbody) return;

    tbody.innerHTML = "";
    
    // Show first 10 rows
    data.slice(0, 10).forEach(sale => {
        const row = `
            <tr>
                <td>${sale.employeeName || "N/A"}</td>
                <td>${sale.salesTarget || 0}</td>
                <td>
                    <span class="badge ${sale.achievementRate >= 80 ? 'bg-success' : 'bg-warning'}">
                        ${sale.achievementRate || 0}%
                    </span>
                </td>
            </tr>
        `;
        tbody.innerHTML += row;
    });
}

// ================== TOP PERFORMERS ==================
async function loadTopPerformers() {
    console.log("Loading top performers...");
    const data = await safeFetchJson(`${BASE}/top-performers`);
    if (!data || !Array.isArray(data)) {
        console.error("Failed to load top performers");
        return;
    }

    console.log("Top performers data:", data);

    const ctx = document.getElementById("topPerformanceChart");
    if (!ctx) {
        console.error("Top performance chart canvas not found");
        return;
    }

    // Destroy old chart if exists
    if (topPerformanceChart) {
        topPerformanceChart.destroy();
    }

    const names = data.map(p => p.employeeName || "Unknown");
    const scores = data.map(p => Math.round(p.performanceScore * 100) / 100 || 0);

    topPerformanceChart = new Chart(ctx, {
        type: "bar",
        data: {
            labels: names,
            datasets: [
                {
                    label: "Performance Score",
                    data: scores,
                    backgroundColor: [
                        "rgba(13, 110, 253, 0.8)",
                        "rgba(25, 135, 84, 0.8)",
                        "rgba(255, 193, 7, 0.8)",
                        "rgba(220, 53, 69, 0.8)",
                        "rgba(111, 66, 193, 0.8)",
                        "rgba(23, 162, 184, 0.8)",
                        "rgba(255, 127, 39, 0.8)",
                        "rgba(108, 117, 125, 0.8)",
                        "rgba(253, 126, 20, 0.8)",
                        "rgba(112, 76, 182, 0.8)"
                    ],
                    borderColor: [
                        "#0d6efd",
                        "#198754",
                        "#ffc107",
                        "#dc3545",
                        "#6f42c1",
                        "#17a2b8",
                        "#ff7f27",
                        "#6c757d",
                        "#fd7e14",
                        "#704cb8"
                    ],
                    borderWidth: 1
                }
            ]
        },
        options: {
            indexAxis: "y",
            responsive: true,
            plugins: {
                legend: {
                    display: true,
                    position: "top"
                }
            },
            scales: {
                x: {
                    beginAtZero: true
                }
            }
        }
    });
}

// ================== EMPLOYEE PERFORMANCE ==================
async function loadEmployeePerformance() {
    console.log("Loading employee performance...");
    const data = await safeFetchJson(`${BASE}/employee-performance`);
    if (!data) {
        console.error("Failed to load employee performance");
        return;
    }

    console.log("Employee performance data:", data);
    // This data can be used for additional analytics if needed
}

// ================== LOAD EVERYTHING ==================
document.addEventListener("DOMContentLoaded", () => {
    console.log("DOM Content Loaded - Initializing dashboard...");
    
    // Load all data with small delays to avoid overwhelming the server
    loadDashboardStats();
    
    setTimeout(() => {
        loadSalesOverview();
    }, 500);
    
    setTimeout(() => {
        loadRecentSales();
    }, 1000);
    
    setTimeout(() => {
        loadTopPerformers();
    }, 1500);
    
    setTimeout(() => {
        loadEmployeePerformance();
    }, 2000);
});

// Optional: Refresh data every 30 seconds
setInterval(() => {
    console.log("Auto-refreshing dashboard data...");
    loadDashboardStats();
    loadSalesOverview();
    loadRecentSales();
    loadTopPerformers();
}, 30000);
