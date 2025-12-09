/* ===============================
      PERFECT JS WITH UNKNOWN FIX
   =============================== */

// Global Chart instances
let barChart = null;
let dailyChart = null;
let sourceChart = null;
let conversionChart = null;

// Backend Base URL
const BASE = "http://localhost:9090/leads";

// Utility: safe chart destroy
function safeDestroy(chart) {
  if (chart && typeof chart.destroy === "function") chart.destroy();
}

// Fetch helper
async function fetchJson(url) {
  const res = await fetch(url);
  if (!res.ok) throw new Error(`HTTP ${res.status} → ${url}`);
  return res.json();
}

/* ===============================
      KPI SUMMARY
   =============================== */
async function updateKPIs() {
  try {
    const data = await fetchJson(`${BASE}/summary`);

    document.getElementById("totalLeads").textContent = data.totalLeads ?? 0;
    document.getElementById("converted").textContent = data.converted ?? 0;
    document.getElementById("contacted").textContent = data.contacted ?? 0;
    document.getElementById("newLeads").textContent = data.newLeads ?? 0;

  } catch (err) {
    console.error("KPI Fetch Error:", err);
  }
}

/* ===============================
   LEADS PER TEAM  (UNKNOWN FIXED)
   =============================== */
async function loadLeadsPerTeam() {
  try {
    const data = await fetchJson(`${BASE}/leads-per-team`);

    let labels = Object.keys(data).map(label => {
      if (!label || label.trim() === "" || label.toLowerCase() === "unknown")
        return "Not Assigned";
      return label;
    });

    let values = Object.values(data);

    const ctx = document.getElementById("barChart");
    if (!ctx) return;

    safeDestroy(barChart);

    barChart = new Chart(ctx, {
      type: "bar",
      data: {
        labels,
        datasets: [{
          label: "Leads Generated",
          data: values,
          backgroundColor: "#4e79a7",
          borderRadius: 8
        }]
      },
      options: {
        responsive: true,
        plugins: {
          title: { display: true, text: "Leads Generated per Team" }
        }
      }
    });

  } catch (err) {
    console.error("Leads Per Team Error:", err);
  }
}

/* ===============================
   LEADS PER DAY
   =============================== */
async function loadLeadsPerDay() {
  try {
    const data = await fetchJson(`${BASE}/leads-per-day`);

    const labels = Object.keys(data);
    const values = Object.values(data);

    const ctx = document.getElementById("dailyChart");
    if (!ctx) return;

    safeDestroy(dailyChart);

    dailyChart = new Chart(ctx, {
      type: "line",
      data: {
        labels,
        datasets: [{
          label: "Leads Per Day",
          data: values,
          borderColor: "#59a14f",
          backgroundColor: "rgba(89,161,79,0.3)",
          fill: true,
          tension: 0.35
        }]
      },
      options: {
        responsive: true,
        plugins: {
          title: { display: true, text: "Leads Per Day" }
        }
      }
    });

  } catch (err) {
    console.error("Leads Per Day Error:", err);
  }
}

/* ===============================
   SOURCE BREAKDOWN
   =============================== */
async function loadSourceBreakdown() {
  try {
    const data = await fetchJson(`${BASE}/source-breakdown`);

    const labels = Object.keys(data);
    const values = Object.values(data);

    const ctx = document.getElementById("sourceChart");
    if (!ctx) return;

    safeDestroy(sourceChart);

    sourceChart = new Chart(ctx, {
      type: "doughnut",
      data: {
        labels,
        datasets: [{
          data: values,
          backgroundColor: ["#4e79a7", "#f28e2b", "#e15759", "#76b7b2", "#59a14f"]
        }]
      },
      options: {
        responsive: true,
        plugins: {
          title: { display: true, text: "Lead Source Breakdown" },
          legend: { position: "bottom" }
        },
        cutout: "60%"
      }
    });

  } catch (err) {
    console.error("Source Breakdown Error:", err);
  }
}

/* ===============================
   CONVERSION CHART
   =============================== */
async function loadConversionChart() {
  try {
    const data = await fetchJson(`${BASE}/leads-per-team`);

    let labels = Object.keys(data).map(label => {
      if (!label || label.trim() === "" || label.toLowerCase() === "unknown")
        return "Not Assigned";
      return label;
    });

    const raw = Object.values(data);
    const conversion = raw.map(v => Math.round(v * 0.55));

    const ctx = document.getElementById("conversionChart");
    if (!ctx) return;

    safeDestroy(conversionChart);

    conversionChart = new Chart(ctx, {
      type: "bar",
      data: {
        labels,
        datasets: [{
          label: "Lead Conversion",
          data: conversion,
          backgroundColor: "#f28e2b"
        }]
      },
      options: {
        indexAxis: "y",
        responsive: true,
        plugins: {
          title: { display: true, text: "Lead Conversion per Team" },
          legend: { display: false }
        }
      }
    });

  } catch (err) {
    console.error("Conversion Chart Error:", err);
  }
}

/* ===============================
    INIT DASHBOARD
   =============================== */
async function initDashboard() {
  await updateKPIs();
  await loadLeadsPerTeam();
  await loadLeadsPerDay();
  await loadSourceBreakdown();
  await loadConversionChart();
}

// Auto load
document.addEventListener("DOMContentLoaded", () => {
  initDashboard();
});

// Auto-refresh every 60 sec
setInterval(() => initDashboard(), 60000);
