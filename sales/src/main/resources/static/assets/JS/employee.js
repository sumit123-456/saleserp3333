/* ============================================================
0. AUTH CHECK & BASE API URL
============================================================ */
// Check if user is logged in
function checkAuth() {
    const token = localStorage.getItem('authToken');
    if (!token) {
        alert('Please login first (admin@saleserp.com)');
        window.location.href = 'login.html';
        return false;
    }
    return true;
}

// Check auth on page load
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', checkAuth);
} else {
    checkAuth();
}

const API_BASE = "http://localhost:9090/api/v1/employees";

// Helper function to get auth headers
function getAuthHeaders() {
    const token = localStorage.getItem('authToken');
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
    };
}

/* ============================================================
1. GLOBAL DATA STORAGE
============================================================ */
let allData = [];

/* ============================================================
2. FETCH WEEKLY SUMMARY
============================================================ */
async function loadWeeklySummary() {
    try {
        const token = localStorage.getItem('authToken');
        const headers = token ? { 
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        } : {};
        
        console.log("📡 Fetching from database: http://localhost:9090/api/v1/employees/weekly-summary");
        
        const res = await fetch("http://localhost:9090/api/v1/employees/weekly-summary", {
            headers: headers
        });
        
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        
        const body = await res.json();
        console.log("✅ API Response:", body);
        
        const data = body.data || [];
        
        if (!Array.isArray(data) || data.length === 0) {
            console.warn("⚠️ No employees in database");
            alert("No employees found in database");
            allData = [];
            updateWeeklySummaryTable([]);
            return;
        }
        
        allData = data.map(emp => ({
            empId: emp.empId,
            name: emp.name,
            team: emp.team,
            monthlyTarget: emp.monthlyTarget,
            achieved: emp.achieved,
            monthlyCallTarget: emp.monthlyCallTarget,
            callsMade: emp.callsMade,
            date: new Date().toISOString().slice(0, 10),
            calls: emp.callsMade || 0,
            duration: 300,
            inbound: Math.floor((emp.callsMade || 0) * 0.6),
            outbound: Math.floor((emp.callsMade || 0) * 0.4),
            missed: Math.floor((emp.callsMade || 0) * 0.1)
        }));
        
        console.log(`✅ Loaded ${allData.length} employees from database`);
        
    } catch (err) {
        console.error("❌ Database fetch failed:", err.message);
        alert(`Error loading data: ${err.message}`);
        allData = [];
        updateWeeklySummaryTable([]);
        return;
    }

    const filtered = applyFilter();
    updateKPIs(filtered);
    updateCharts(filtered);
    updateWeeklySummaryTable(allData);
}

/* ============================================================
3. FILTER FUNCTION
============================================================ */
function applyFilter() {
    let name = document.getElementById("filterName")?.value.toLowerCase() || "";
    let team = document.getElementById("filterTeam")?.value || "";
    let range = document.getElementById("timeRange")?.value || "30";

    let today = new Date();
    today.setHours(0, 0, 0, 0);
    let days = range === "today" ? 1 : parseInt(range);
    let start = new Date(today);
    start.setDate(today.getDate() - (days - 1));

    return allData.filter(d => {
        let dt = new Date(d.date);
        return (!name || d.name.toLowerCase().includes(name)) &&
               (!team || d.team === team) &&
               dt >= start && dt <= today;
    });
}

/* ============================================================
4. KPI UPDATE
============================================================ */
function updateKPIs(filtered) {
    if (!filtered.length) return;

    let total = filtered.reduce((s, d) => s + d.calls, 0);
    let dur = filtered.reduce((s, d) => s + d.duration, 0);
    let avg = total ? dur / total : 0;

    document.getElementById("kpiTotalCalls").textContent = total;
    document.getElementById("kpiSuccess").textContent = "80%";
    document.getElementById("kpiDuration").textContent =
        Math.floor(avg / 60) + "m " + Math.round(avg % 60) + "s";
    document.getElementById("kpiAgents").textContent = filtered.length;
}

/* ============================================================
5. CHART VARIABLES
============================================================ */
let lineChart, topChart, bottomChart, donutChart, pieChart;

/* ============================================================
6. UPDATE ALL CHARTS
============================================================ */
function updateCharts(filtered) {
    updateLineChart(filtered);
    loadTopBottomCharts(filtered);
    loadDonutChart();
    loadPieChart();
}

/* ============================================================
7. LINE CHART
============================================================ */
function updateLineChart(filtered) {
    let range = parseInt(document.getElementById("timeRange").value || 30);
    let today = new Date();
    today.setHours(0, 0, 0, 0);
    let start = new Date(today);
    start.setDate(today.getDate() - (range - 1));

    let labels = [];
    let callMap = {};

    for (let i = 0; i < range; i++) {
        let d = new Date(start);
        d.setDate(start.getDate() + i);
        let key = d.toISOString().slice(0, 10);
        labels.push(key);
        callMap[key] = 0;
    }

    filtered.forEach(e => {
        if (callMap[e.date] !== undefined) callMap[e.date] += e.calls;
    });

    if (lineChart) lineChart.destroy();

    let ctx = document.getElementById("chartjs-line");
    if (ctx) {
        lineChart = new Chart(ctx, {
            type: "line",
            data: {
                labels,
                datasets: [
                    {
                        label: "Calls per Day",
                        data: Object.values(callMap),
                        borderColor: "#007bff",
                        backgroundColor: "rgba(0,123,255,0.2)",
                        fill: true,
                        tension: 0.4
                    }
                ]
            }
        });
    }
}

/* ============================================================
8. TOP / BOTTOM CHART
============================================================ */
async function loadTopBottomCharts(filtered) {
    try {
        const res = await fetch(API_BASE + "/performance/top-bottom");
        const data = await res.json();
        renderTopBottom(data.top5Employees, data.bottom5Employees);
    } catch (e) {
        let fallback = filtered
            .map(d => ({
                empName: d.name,
                performancePercentage: Math.round((d.calls / 20) * 100)
            }))
            .sort((a, b) => b.performancePercentage - a.performancePercentage);

        renderTopBottom(fallback.slice(0, 5), fallback.slice(-5));
    }
}

function renderTopBottom(top, bottom) {
    if (topChart) topChart.destroy();
    if (bottomChart) bottomChart.destroy();

    topChart = new Chart(document.getElementById("topPerformers"), {
        type: "bar",
        data: {
            labels: top.map(e => e.empName),
            datasets: [
                {
                    label: "Performance (%)",
                    data: top.map(e => e.performancePercentage),
                    backgroundColor: "#007bff"
                }
            ]
        }
    });

    bottomChart = new Chart(document.getElementById("bottomPerformers"), {
        type: "bar",
        data: {
            labels: bottom.map(e => e.empName),
            datasets: [
                {
                    label: "Performance (%)",
                    data: bottom.map(e => e.performancePercentage),
                    backgroundColor: "#e15759"
                }
            ]
        }
    });
}

/* ============================================================
9. DONUT CHART (CALL TYPE - INBOUND/OUTBOUND)
============================================================ */
function loadDonutChart() {
    try {
        // Calculate inbound/outbound from fallback data
        let inbound = allData.reduce((s, d) => s + (d.inbound || 0), 0);
        let outbound = allData.reduce((s, d) => s + (d.outbound || 0), 0);

        // If no data, use defaults for demo
        if (inbound === 0 && outbound === 0) {
            inbound = 45;
            outbound = 55;
        }

        if (donutChart) donutChart.destroy();

        const donutCtx = document.getElementById("donutChart");
        if (donutCtx) {
            donutChart = new Chart(donutCtx, {
                type: "doughnut",
                data: {
                    labels: ["Inbound Calls", "Outbound Calls"],
                    datasets: [
                        {
                            data: [inbound, outbound],
                            backgroundColor: ["#36A2EB", "#FF6384"],
                            borderColor: ["#ffffff", "#ffffff"],
                            borderWidth: 2
                        }
                    ]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: true,
                    plugins: {
                        legend: {
                            position: "bottom"
                        }
                    }
                }
            });
        }
    } catch (e) {
        console.error("Donut chart error:", e);
        // Fallback with demo data
        if (donutChart) donutChart.destroy();
        const donutCtx = document.getElementById("donutChart");
        if (donutCtx) {
            donutChart = new Chart(donutCtx, {
                type: "doughnut",
                data: {
                    labels: ["Inbound Calls", "Outbound Calls"],
                    datasets: [
                        {
                            data: [45, 55],
                            backgroundColor: ["#36A2EB", "#FF6384"],
                            borderColor: ["#ffffff", "#ffffff"],
                            borderWidth: 2
                        }
                    ]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: true,
                    plugins: {
                        legend: {
                            position: "bottom"
                        }
                    }
                }
            });
        }
    }
}

/* ============================================================
10. PIE CHART (DISPOSITION BREAKDOWN)
============================================================ */
async function loadPieChart() {
    try {
        const token = localStorage.getItem('authToken');
        const headers = token ? { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' } : { 'Content-Type': 'application/json' };

        console.log('📡 Fetching disposition summary from API');
        const res = await fetch('http://localhost:9090/api/calls/disposition-summary', { headers });

        if (!res.ok) {
            throw new Error(`HTTP ${res.status}`);
        }

        const body = await res.json();
        console.log('🔁 Disposition API response:', body);

        // Unwrap CommonUtil-style responses: { status, message, data }
        let payload = body;
        if (body && typeof body === 'object' && ('status' in body || 'data' in body)) {
            payload = body.data || {};
            if (body.status && body.status !== 'success') {
                console.warn('Disposition API returned non-success status:', body.status, body.message);
            }
        }

        // Support payload as object map { connected: 10, missed: 2 } or array [{label, value}, ...]
        let labels = [];
        let values = [];

        if (Array.isArray(payload)) {
            // array of objects expected: [{ label: 'connected', value: 10 }, ...]
            payload.forEach(item => {
                if (typeof item === 'object') {
                    const label = item.label || item.name || Object.keys(item)[0];
                    const value = Number(item.value ?? item.count ?? Object.values(item)[0]) || 0;
                    labels.push(String(label));
                    values.push(value);
                }
            });
        } else if (payload && typeof payload === 'object') {
            Object.entries(payload).forEach(([k, v]) => {
                labels.push(String(k));
                values.push(Number(v) || 0);
            });
        }

        if (labels.length === 0 || values.reduce((a, b) => a + b, 0) === 0) {
            console.warn('⚠️ Disposition API returned empty/noisy data, using fallback');
            loadPieChartFallback();
            return;
        }

        // Normalize labels (capitalize)
        const displayLabels = labels.map(l => String(l).charAt(0).toUpperCase() + String(l).slice(1));

        if (pieChart) pieChart.destroy();
        const pieCtx = document.getElementById('pieChart');
        if (!pieCtx) {
            console.warn('Pie canvas not found');
            return;
        }

        pieChart = new Chart(pieCtx, {
            type: 'pie',
            data: {
                labels: displayLabels,
                datasets: [{ data: values, backgroundColor: ["#007bff", "#f28e2b", "#e15759", "#76b7b2", "#59a14f"] }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: { legend: { position: 'bottom' } }
            }
        });

        console.log('✅ Disposition pie chart rendered', { labels: displayLabels, values });
    } catch (err) {
        console.error('❌ loadPieChart failed:', err);
        loadPieChartFallback();
    }
}

function loadPieChartFallback() {
    let missed = allData.reduce((s, d) => s + (d.missed || 0), 0);
    let connected = allData.reduce((s, d) => s + ((d.calls || 0) - (d.missed || 0)), 0);
    
    if (missed === 0 && connected === 0) {
        missed = 20;
        connected = 80;
    }

    let labels = ["Connected", "Missed"];
    let values = [connected, missed];

    if (pieChart) pieChart.destroy();
    const pieCtx = document.getElementById("pieChart");
    if (pieCtx) {
        pieChart = new Chart(pieCtx, {
            type: "pie",
            data: {
                labels,
                datasets: [
                    {
                        data: values,
                        backgroundColor: ["#007bff", "#f28e2b"]
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    legend: { position: "bottom" }
                }
            }
        });
    }
}

/* ============================================================
11. UPDATE WEEKLY SUMMARY TABLE
============================================================ */
function updateWeeklySummaryTable(employees) {
    const tbody = document.getElementById('summaryTable')?.querySelector('tbody');
    if (!tbody) return;
    
    tbody.innerHTML = '';
    
    // Show first 5 employees (or all if fewer)
    const toShow = employees.slice(0, 5);
    
    if (toShow.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted">No employees found</td></tr>';
        return;
    }
    
    toShow.forEach(emp => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td><strong>${emp.empId || '-'}</strong></td>
            <td>${emp.name || emp.empName || 'N/A'}</td>
            <td>${emp.team || 'N/A'}</td>
            <td>${(emp.monthlyTarget || 0).toLocaleString('en-IN')}</td>
            <td>${(emp.achieved || 0).toLocaleString('en-IN')}</td>
            <td>${emp.monthlyCallTarget || 0}</td>
            <td>${emp.callsMade || 0}</td>
        `;
        tbody.appendChild(row);
    });
    
    // Update generated date
    const dateSpan = document.getElementById('generatedDate');
    if (dateSpan) {
        dateSpan.textContent = new Date().toLocaleDateString('en-IN', { 
            year: 'numeric', 
            month: 'long', 
            day: 'numeric' 
        });
    }
}
function loadProfile() {
    const user = JSON.parse(localStorage.getItem("user"));

    if (user) {
        console.log("User Loaded:", user);
        document.getElementById('userName').innerHTML = user.name || "User";
        document.getElementById('userEmail').innerHTML = user.email || "user@email.com";
         document.getElementById('navbarProfileEmail').innerHTML = user.email || "User";
          document.getElementById('navbarProfileName').innerHTML = user.name || "User";
    } else {
        console.warn("User not found in localStorage");
    }
}

/* ============================================================
12. INIT
============================================================ */
document.addEventListener("DOMContentLoaded", () => {
    loadWeeklySummary();
    loadProfile();
    
    // Event listeners for filter and toggle buttons
    const filterToggle = document.getElementById('filterToggle');
    if (filterToggle) {
        filterToggle.addEventListener('click', () => {
            const container = document.getElementById('filterFormContainer');
            if (container) container.classList.toggle('d-none');
        });
    }
    
    const dailyDetailsBtn = document.getElementById('dailyDetailsBtn');
    if (dailyDetailsBtn) {
        dailyDetailsBtn.addEventListener('click', () => {
            const container = document.getElementById('dailyCallFormContainer');
            if (container) container.classList.toggle('d-none');
        });
    }
    
    const filterForm = document.getElementById('filterForm');
    if (filterForm) {
        filterForm.addEventListener('submit', (e) => {
            e.preventDefault();
            const filtered = applyFilter();
            updateKPIs(filtered);
            updateCharts(filtered);
            updateWeeklySummaryTable(filtered);
        });
    }
});

// Lead section
 
const leadsGenerate = document.getElementById("leadsGenerate");
const leadSection = document.getElementById("leadDetailsSection");
const leadFields = leadSection?.querySelectorAll("input, select");
 
if (leadsGenerate && leadSection && leadFields) {
  leadsGenerate.addEventListener("change", function () {
    if (this.value === "yes") {
      leadSection.classList.add("show");
      leadFields.forEach((el) => (el.required = true));
    } else {
      leadSection.classList.remove("show");
      leadFields.forEach((el) => {
        el.required = false;
        el.classList.remove("is-invalid");
      });
    }
  });
}
 
 
// -------------------- 13. LIVE FIELD VALIDATION --------------------
const dailyAgentInput = document.getElementById("dailyAgent");
if (dailyAgentInput) {
  dailyAgentInput.addEventListener("input", () => {
    // Allow only letters and spaces for Employee Name
    dailyAgentInput.value = dailyAgentInput.value.replace(/[^a-zA-Z\s]/g, "");
  });
}
 
// General text inputs (excluding dailyAgent which has specific validation)
document.querySelectorAll('input[type="text"]').forEach((input) => {
  if (input.id !== "dailyAgent") {
    input.addEventListener("input", () => {
      // Allow letters, numbers, and spaces for other text inputs
      input.value = input.value.replace(/[^a-zA-Z0-9\s]/g, "");
    });
  }
});
 
 
document.querySelectorAll('input[type="number"]').forEach((input) => {
  input.addEventListener("input", () => {
    // Enforce positive numbers
    if (input.value.startsWith("-")) input.value = "";
  });
});
 
// -------------------- 15. PHONE NUMBER VALIDATION --------------------
const phoneInput = document.getElementById("phone_number");
if (phoneInput) {
  phoneInput.addEventListener("input", () => {
    // Allow only digits
    phoneInput.value = phoneInput.value.replace(/[^0-9]/g, "");
    // Limit to 10 digits
    if (phoneInput.value.length > 10) {
      phoneInput.value = phoneInput.value.slice(0, 10);
    }
  });
}
 
// today string used for date inputs (ISO yyyy-mm-dd)
const today = new Date().toISOString().split("T")[0];
 
// cache date input elements so later code can use them safely
const dailyDate = document.getElementById("dailyDate");
const leadDate = document.getElementById("lead_date");
 
// initialize and restrict date pickers to today (if present)
if (dailyDate) { dailyDate.max = today; dailyDate.value = today; }
if (leadDate)  { leadDate.max = today;  leadDate.value = today; }
 
 
// -------------------- 14. FORM VALIDATION --------------------
const dailyCallForm = document.getElementById("dailyCallForm");
if (dailyCallForm) {
  dailyCallForm.addEventListener("submit", (e) => {
    e.preventDefault();
    let isValid = true;
    let form = e.target;
 
    form.querySelectorAll("[required]").forEach((field) => {
 
      // Custom check for name field to ensure it is not empty AND only contains allowed characters
      if (field.id === "dailyAgent" && field.value.trim() && !field.checkValidity()) {
        field.classList.add("is-invalid");
        field.classList.remove("is-valid");
        isValid = false;
        return;
      }
 
      if (!field.value.trim()) {
        field.classList.add("is-invalid");
        field.classList.remove("is-valid");
        isValid = false;
      } else {
        field.classList.remove("is-invalid");
        field.classList.add("is-valid");
      }
    });
 
    // Enable Bootstrap validation styles
    document.getElementById('dailyCallForm').addEventListener('submit', function (event) {
      if (!this.checkValidity()) {
        event.preventDefault();
        event.stopPropagation();
      }
      this.classList.add('was-validated');
    });
 
    // Additional check for successful calls <= total calls
    const totalCallsInput = document.getElementById("dailyCalls");
    const successCallsInput = document.getElementById("dailySuccessCalls");
    if (totalCallsInput && successCallsInput) {
      const total = parseInt(totalCallsInput.value) || 0;
      const success = parseInt(successCallsInput.value) || 0;
      if (success > total) {
        successCallsInput.classList.add("is-invalid");
        successCallsInput.classList.remove("is-valid");
        successCallsInput.nextElementSibling.textContent = "Successful calls cannot exceed total calls.";
        isValid = false;
      } else {
        if (successCallsInput.value.trim()) { // Only re-validate if it passed the empty check
          successCallsInput.classList.remove("is-invalid");
          successCallsInput.classList.add("is-valid");
          successCallsInput.nextElementSibling.textContent = "Please enter successful calls.";
        }
      }
    }
 
        if (isValid) {
      const leadsGenerateValue = document.getElementById("leadsGenerate").value;
      
      // Get empId from the lead details section as a string (supports "EMP02" format)
      const empIdValue = document.getElementById("emp_id").value.trim();
      
      const payload = {
        empId: empIdValue,  // Send as string, not parsed as integer
        callType: document.getElementById("callDirection").value,
        callDate: document.getElementById("dailyDate").value,
        disposition: document.getElementById("dispositionBreackdown").value,
        duration: parseInt(document.getElementById("dailyDuration").value) || 0,
        team: ""
      };
 
      const token = localStorage.getItem("authToken");
      
      // Function to submit call details
      const submitCallDetails = () => {
        return fetch("http://localhost:9090/api/calls", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            ...(token ? { "Authorization": `Bearer ${token}` } : {})
          },
          body: JSON.stringify(payload)
        })
          .then(res => {
            if (!res.ok) {
              return res.text().then(t => { throw new Error(t || "Failed to save daily call details"); });
            }
            return res.json();
          });
      };
      
      // Function to submit lead details if leads generate = "yes"
      const submitLeadDetails = () => {
        if (leadsGenerateValue !== "yes") {
          return Promise.resolve(); // Skip if not generating leads
        }
        
        const leadPayload = {
          empId: empIdValue,  // Send as string (e.g., "EMP02")
          phone: document.getElementById("phone_number").value.trim(),
          source: document.getElementById("source").value.trim(),
          convertedToDeal: document.getElementById("converted_to_deal").value === "yes" ? "Yes" : "No",
          dealValue: parseFloat(document.getElementById("deal_value").value.trim()) || 0,
          createdDate: document.getElementById("lead_date").value
        };
        
        return fetch("http://localhost:9090/leads", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            ...(token ? { "Authorization": `Bearer ${token}` } : {})
          },
          body: JSON.stringify(leadPayload)
        })
          .then(res => {
            if (!res.ok) {
              return res.text().then(t => { throw new Error(t || "Failed to save lead details"); });
            }
            return res.json();
          });
      };
      
      // Submit call details first, then lead details if applicable
      submitCallDetails()
        .then(() => submitLeadDetails())
        .then(data => {
          const message = leadsGenerateValue === "yes" 
            ? "Daily Call Detail and Lead Form submitted successfully!" 
            : "Daily Call Detail Form submitted successfully!";
          alert(message);
          dailyCallForm.reset();
          dailyCallForm.querySelectorAll(".form-control, .form-select")
            .forEach(f => f.classList.remove("is-invalid", "is-valid"));
          if (leadSection) {
            leadSection.classList.remove("show");
            leadFields.forEach(el => (el.required = false));
          }
          if (dailyDate) dailyDate.value = today;
          if (leadDate) leadDate.value = today;
        })
        .catch(err => {
          console.error("Error saving form details:", err);
          alert("Failed to save form details: " + err.message);
        });
    }
 
  });
 
  dailyCallForm.addEventListener("reset", () => {
    dailyCallForm.querySelectorAll(".form-control, .form-select").forEach((f) => f.classList.remove("is-invalid", "is-valid"));
    if (leadSection) {
      leadSection.classList.remove("show");
      leadFields.forEach((el) => (el.required = false));
    }
    if (dailyDate) dailyDate.value = today;
    if (leadDate) leadDate.value = today;
  });
}

document.getElementById("downloadPdfBtn").addEventListener("click", async () => {
    const { jsPDF } = window.jspdf;
 
    const pdf = new jsPDF("p", "mm", "a4");
    const element = document.getElementById("summaryTable");
 
    if (!element) {
        alert("Summary table not found!");
        return;
    }
 
    await html2canvas(element, { scale: 2 }).then((canvas) => {
        const imgData = canvas.toDataURL("image/png");
        const pageWidth = pdf.internal.pageSize.getWidth();
        const imgWidth = pageWidth - 20;
        const imgHeight = (canvas.height * imgWidth) / canvas.width;
 
        pdf.text("Weekly Summary Report", 10, 10);
        pdf.addImage(imgData, "PNG", 10, 20, imgWidth, imgHeight);
        pdf.save("Weekly_Report.pdf");
    });
});