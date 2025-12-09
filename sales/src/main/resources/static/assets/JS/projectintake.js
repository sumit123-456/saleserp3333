
function aggregateProjects(projects) {
  const typeMap = {};
  const statusMap = {};
  const clientMap = {};
  const dateMap = {};

  projects.forEach(p => {
    const t = p.projectType || "Unknown";
    const s = p.projectStatus || "Unknown";
    const c = p.companyName || "Unknown";
    const d = p.intakeDate || "Unknown";

    typeMap[t] = (typeMap[t] || 0) + 1;
    statusMap[s] = (statusMap[s] || 0) + 1;
    clientMap[c] = (clientMap[c] || 0) + 1;
    dateMap[d] = (dateMap[d] || 0) + 1;
  });

  const toArray = (m, keyName) => Object.keys(m).map(k => ({ [keyName]: k, count: m[k] }));

  const total = projects.length;
  const completed = statusMap["Completed"] || statusMap["completed"] || 0;
  const pending = statusMap["Pending"] || statusMap["pending"] || 0;
  const activePercent = total ? Math.round(((total - (completed + pending)) / total) * 100) : 0;

  return {
    typeCounts: toArray(typeMap, "projectType"),
    statusCounts: toArray(statusMap, "projectStatus"),
    clientCounts: toArray(clientMap, "companyName"),
    overTime: Object.keys(dateMap).sort().map(d => ({ intakeDate: d, count: dateMap[d] })),
    kpis: { total, completed, pending, activePercent }
  };
}

/**
 * Apply filtered data to UI: table, KPIs, and charts.
 * Expects an array of project objects.
 */
function applyFilteredData(projects) {
  filteredProjects = projects.slice(); // keep global copy if needed elsewhere
  renderProjectTable(filteredProjects);

  // Update KPIs
  const agg = aggregateProjects(filteredProjects);
  document.getElementById("totalProject") && (document.getElementById("totalProject").textContent = agg.kpis.total);
  document.getElementById("completeProject") && (document.getElementById("completeProject").textContent = agg.kpis.completed);
  document.getElementById("pendinProjects") && (document.getElementById("pendinProjects").textContent = agg.kpis.pending);
  document.getElementById("activeProject") && (document.getElementById("activeProject").textContent = agg.kpis.activePercent + "%");

  // Feed charts using your existing chart functions but using aggregated arrays:
  // Note: these functions expect arrays shaped like your earlier fetch results.
  try {
    renderCharts(agg.overTime);            // expects [{intakeDate, count}, ...]
  } catch (e) { console.warn("renderCharts failed:", e); }

  try {
    loadProjectTypeChart(agg.typeCounts);  // expects [{ projectType, count }]
  } catch (e) { console.warn("loadProjectTypeChart failed:", e); }

  try {
    loadStatusChart(agg.statusCounts);     // expects [{ projectStatus, count }]
  } catch (e) { console.warn("loadStatusChart failed:", e); }

  try {
    loadClientBarChart(agg.clientCounts);  // expects [{ companyName, count }]
  } catch (e) { console.warn("loadClientBarChart failed:", e); }
}

/**
 * Try server-side filtering first (GET /projects/filter), fall back to client-side filter.
 * backend expects query params: projectType, projectStatus, companyName, dateRange
 */
async function runFilterAndApply() {
  const projectType = (document.getElementById("filterType")?.value || "").trim();
  const projectStatus = (document.getElementById("filterStatus")?.value || "").trim();
  const companyName = (document.getElementById("filterDistribution")?.value || "").trim();
  const dateRange = (document.getElementById("timeRange")?.value || "").trim();

  // Build query string only for non-empty values
  const params = new URLSearchParams();
  if (projectType) params.append("projectType", projectType);
  if (projectStatus) params.append("projectStatus", projectStatus);
  if (companyName) params.append("companyName", companyName);
  if (dateRange) params.append("dateRange", dateRange);

  const qs = params.toString();

  // First attempt: server-side filter
  if (qs) {
    try {
      const url = `http://localhost:9090/projects/filter${qs ? `?${qs}` : ""}`;
      console.log("[Filters] Requesting filtered projects from", url);
      const res = await fetch(url);
      if (res.ok) {
        const rows = await res.json();
        if (Array.isArray(rows)) {
          console.log("[Filters] Server returned", rows.length, "rows");
          applyFilteredData(rows);
          return;
        } else {
          console.warn("[Filters] Server returned non-array, falling back to client filter", rows);
        }
      } else {
        console.warn("[Filters] Server filter returned", res.status, res.statusText, "falling back to client filter");
      }
    } catch (err) {
      console.warn("[Filters] Server filter call failed, falling back to client filter:", err);
    }
  }

  // Fallback: client-side filter on allProjects
  const filtered = allProjects.filter(p => {
    if (projectType && ((p.projectType || "").toString() !== projectType)) return false;
    if (projectStatus && ((p.projectStatus || "").toString() !== projectStatus)) return false;
    if (companyName && ((p.companyName || "").toString() !== companyName)) return false;

    // dateRange filter: support '30' '14' '7' 'yesterday' 'today' or specific logic
    if (dateRange) {
      if (dateRange === "today") {
        const today = new Date().toISOString().split("T")[0];
        if ((p.intakeDate || "").split("T")[0] !== today) return false;
      } else if (dateRange === "yesterday") {
        const d = new Date(); d.setDate(d.getDate() - 1);
        const yesterday = d.toISOString().split("T")[0];
        if ((p.intakeDate || "").split("T")[0] !== yesterday) return false;
      } else {
        // numeric days: last N days
        const n = parseInt(dateRange, 10);
        if (!isNaN(n)) {
          const cutoff = new Date(); cutoff.setDate(cutoff.getDate() - n);
          const pdate = new Date((p.intakeDate || "").split("T")[0]);
          if (isNaN(pdate.getTime()) || pdate < cutoff) return false;
        }
      }
    }

    return true;
  });

  console.log("[Filters] Client-side filtered rows:", filtered.length);
  applyFilteredData(filtered);
}

// Attach handlers to filter form and reset button.
// Place this code before your DOMContentLoaded or run inside it.
(function attachFilterFormHandlers() {
  const form = document.getElementById("filterForm");
  if (!form) {
    console.warn("[Filters] #filterForm not found. Filters disabled.");
    return;
  }

  form.addEventListener("submit", async (ev) => {
    ev.preventDefault();
    await runFilterAndApply();
  });

  // Optional: reset → show all projects again
  form.addEventListener("reset", (ev) => {
    // small delay to allow native reset to clear inputs
    setTimeout(() => {
      filteredProjects = allProjects.slice();
      applyFilteredData(filteredProjects);
    }, 30);
  });

  // Optional: show filters toggle (if you have a button toggling visibility)
  const filterToggleBtn = document.getElementById("filterToggle");
  if (filterToggleBtn) {
    filterToggleBtn.addEventListener("click", (e) => {
      const container = document.getElementById("filterFormContainer");
      if (container) container.classList.toggle("d-none");
    });
  }
})();


let allProjects = [];
let filteredProjects = [];
let lineChart = null;
let projectChart = null;
let statusChart = null;
let barChart = null;

// ============================
// LOAD ALL PROJECTS (MAIN API)
// ============================
async function loadProjects() {
  try {
    const res = await fetch("http://localhost:9090/projects/all");
    const data = await res.json();

    allProjects = data;
    filteredProjects = data;

    renderProjectTable(data);
    loadAllCharts();

  } catch (error) {
    console.error("Failed to load project data:", error);
  }
}

// ============================
// RENDER TABLE
// ============================
function renderProjectTable(data) {
  const tbody = document.getElementById("projectTableBody");
  if (!tbody) return;

  tbody.innerHTML = "";

  data.forEach((p, index) => {
    const row = `
      <tr>
        <td>${index + 1}</td>
        <td>${p.employeeName || "-"}</td>
        <td>${p.projectType || "-"}</td>
        <td>${p.projectStatus || "-"}</td>
        <td>${p.companyName || "-"}</td>
        <td>${p.intakeDate || "-"}</td>
      </tr>
    `;
    tbody.innerHTML += row;
  });
}

// ============================
// LOAD ALL CHARTS
// ============================
async function loadAllCharts() {
  try {
    const [typeRes, statusRes, clientRes, overtimeRes] = await Promise.all([
      fetch("http://localhost:9090/projects/group-by-type").then(r => r.json()),
      fetch("http://localhost:9090/projects/group-by-status").then(r => r.json()),
      fetch("http://localhost:9090/projects/group-by-client").then(r => r.json()),
      fetch("http://localhost:9090/projects/over-time").then(r => r.json())
    ]);

    renderCharts(overtimeRes);
    loadProjectTypeChart(typeRes);
    loadStatusChart(statusRes);
    loadClientBarChart(clientRes);

  } catch (error) {
    console.error("Chart loading error:", error);
  }
}

// ============================
// LINE CHART (DATE WISE COUNT)
// ============================
function renderCharts(projectData) {
  if (!projectData || projectData.length === 0) return;

  const ctx = document.getElementById("chartjs-line");
  if (!ctx) return;

  const labels = projectData.map(d => d.intakeDate);
  const values = projectData.map(d => d.count);

  let cumulative = [];
  let sum = 0;
  values.forEach(v => {
    sum += v;
    cumulative.push(sum);
  });

  if (lineChart) lineChart.destroy();

  lineChart = new Chart(ctx, {
    type: "line",
    data: {
      labels,
      datasets: [{
        label: "Total Projects",
        data: cumulative,
        borderColor: "#0066ff",
        backgroundColor: "rgba(0,102,255,0.2)",
        fill: true,
        tension: 0.4,
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false
    }
  });
}

// ============================
// PIE CHART – TYPE
// ============================
function loadProjectTypeChart(data) {
  const ctx = document.getElementById("projectChart");
  if (!ctx) return;

  if (projectChart) projectChart.destroy();

  projectChart = new Chart(ctx, {
    type: "pie",
    data: {
      labels: data.map(d => d.projectType),   // FIXED
      datasets: [{
        data: data.map(d => d.count),
        backgroundColor: ["#4e79a7", "#f28e2b", "#76b7b2", "#e15759"]
      }]
    },
    options: { responsive: true }
  });
}



// ============================
// DOUGHNUT – STATUS
// ============================
function loadStatusChart(data) {
  const ctx = document.getElementById("statusPieChart");
  if (!ctx) return;

  if (statusChart) statusChart.destroy();

  statusChart = new Chart(ctx, {
    type: "doughnut",
    data: {
      labels: data.map(d => d.projectStatus),  // FIXED
      datasets: [{
        data: data.map(d => d.count),
        backgroundColor: ["#59a14f", "#edc948", "#e15759"]
      }]
    },
    options: { responsive: true }
  });
}

// ============================
// BAR CHART – CLIENT
// ============================
function loadClientBarChart(data) {
  const ctx = document.getElementById("barChart");
  if (!ctx) return;

  if (barChart) barChart.destroy();

  barChart = new Chart(ctx, {
    type: "bar",
    data: {
      labels: data.map(d => d.companyName),   // FIXED
      datasets: [{
        label: "Projects",
        data: data.map(d => d.count),
        backgroundColor: "#0080FF"
      }]
    },
    options: {
      responsive: true,
      scales: {
        y: { beginAtZero: true }
      }
    }
  });
}


// ============================
// ON LOAD
// ============================
document.addEventListener("DOMContentLoaded", () => {
  loadProjects();
});
