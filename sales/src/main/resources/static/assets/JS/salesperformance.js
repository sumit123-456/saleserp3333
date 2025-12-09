document.addEventListener("DOMContentLoaded", () => {
  const TEAMS = ["Backend", "Frontend", "Full Stack", "DevOps", "QA"];
  const today = new Date();

  function dateToYMD(d){
    const y = d.getFullYear();
    const m = String(d.getMonth()+1).padStart(2,'0');
    const day = String(d.getDate()).padStart(2,'0');
    return `${y}-${m}-${day}`;
  }

  // create a 6-month sliding dates array for demo data
  const startMonthDate = new Date(today.getFullYear(), today.getMonth() - 5, 1);
  const endMonthDate = new Date(today.getFullYear(), today.getMonth() + 1, 0);
  let allDates = [];
  let cur = new Date(startMonthDate);
  while(cur <= endMonthDate){ allDates.push(dateToYMD(cur)); cur.setDate(cur.getDate() + 1); }

  function randCount(){ return Math.floor(Math.random()*3); }

  const dashboardData = TEAMS.map(team => ({
    team,
    projects: allDates.map(d => ({ date: d, count: randCount() }))
  }));

  document.getElementById("totalTeams").textContent = dashboardData.length;

  // Populate filter selects
  const yearSelect = document.getElementById("filterYear");
  const monthSelect = document.getElementById("filterMonth");
  const teamSelect = document.getElementById("filterTeam");

  teamSelect.innerHTML = `<option value="All Teams">All Teams</option>` + dashboardData.map(t => `<option value="${t.team}">${t.team}</option>`).join('');

  // Years 2025-2030
  const years = [];
  for(let y=2025;y<=2030;y++){ years.push(y); }
  yearSelect.innerHTML = years.map(y=>`<option value="${y}">${y}</option>`).join('');

  // Months Jan-Dec with "All Months" first
  const months = [];
  for(let m=1;m<=12;m++){
    const name = new Date(2000,m-1,1).toLocaleString(undefined,{month:'long'});
    months.push({num:m,name});
  }
  monthSelect.innerHTML = `<option value="All">All Months</option>` + months.map(m=>`<option value="${m.num}">${String(m.num).padStart(2,'0')} — ${m.name}</option>`).join('');

  // Charts
  let barChart=null, lineChart=null;

  function getLastDayOfMonth(year, month){ return new Date(year, month, 0).getDate(); }
  function monthDayLabels(year, month){
    const last = getLastDayOfMonth(year, month);
    const dayLabels = [], isoDates=[];
    for(let d=1;d<=last;d++){
      dayLabels.push(String(d));
      const mm = String(month).padStart(2,'0');
      const dd = String(d).padStart(2,'0');
      isoDates.push(`${year}-${mm}-${dd}`);
    }
    return {dayLabels, isoDates};
  }

  function monthLabelsYear(year){
    const labels=[]; const iso=[];
    for(let m=1;m<=12;m++){
      const name=new Date(year,m-1,1).toLocaleString(undefined,{month:'short'});
      labels.push(name);
      iso.push(`${year}-${String(m).padStart(2,'0')}`);
    }
    return {labels, iso};
  }

  function createCharts(teamFilter, year, month){
    year = Number(year);
    const monthName = (month !== 'All') ? new Date(year, month-1, 1).toLocaleString(undefined,{month:'long'}) : null;

    // LINE CHART DATA
    let lineLabels, lineValues;
    if(month === 'All'){
      const {labels, iso} = monthLabelsYear(year);
      lineLabels = labels;
      lineValues = iso.map(isoMonth=>{
        const totalForMonth = dashboardData.reduce((acc, teamObj)=>{
          const sum = teamObj.projects.reduce((s,p)=> p.date.startsWith(isoMonth) ? s + p.count : s, 0);
          return acc + (teamFilter==='All Teams' ? sum : (teamObj.team===teamFilter? sum:0));
        },0);
        return totalForMonth;
      });
    } else {
      const {dayLabels, isoDates} = monthDayLabels(year, Number(month));
      lineLabels = dayLabels;
      lineValues = isoDates.map(dateStr => {
        if(teamFilter==='All Teams') return dashboardData.reduce((s,teamObj)=>s+(teamObj.projects.find(p=>p.date===dateStr)?.count||0),0);
        const teamObj = dashboardData.find(t=>t.team===teamFilter);
        return teamObj?.projects.find(p=>p.date===dateStr)?.count||0;
      });
    }

    // BAR CHART DATA
    let barLabels = dashboardData.map(t=>t.team);
    let barValues;
    if(month === 'All'){
      barValues = dashboardData.map(teamObj => {
        return teamObj.projects.reduce((s,p)=> p.date.startsWith(String(year)) ? s + p.count : s, 0);
      });
    } else {
      const {dayLabels, isoDates} = monthDayLabels(year, Number(month));
      barValues = dashboardData.map(teamObj => teamObj.projects.filter(p=> isoDates.includes(p.date)).reduce((s,p)=>s+p.count,0));
    }

    const displayBarValues = teamFilter==='All Teams' ? barValues : barLabels.map((lbl,i)=> lbl===teamFilter ? barValues[i] : 0);

    const totalProjects = lineValues.reduce((s,v)=>s+v,0);
    document.getElementById("totalProjects").textContent = totalProjects;

    // Titles
    const yearSpanBlue = `<span style="color:#007bff">${year}</span>`;
    if(month === 'All'){
      document.getElementById("barChartTitle").innerHTML = `Project Distribution to IT Teams in ${yearSpanBlue}`;
      document.getElementById("lineChartTitle").innerHTML = `Project Allocation by Month — <span style="color:#ff4d4d">${year}</span>`; // line chart red
    } else {
      document.getElementById("barChartTitle").innerHTML = `Project Distribution to IT Teams — <span style="color:#007bff">${monthName}</span>`;
      document.getElementById("lineChartTitle").innerHTML = `Project Allocation by Date — <span style="color:#ff4d4d">${monthName}</span>`; // line chart red
    }

    if(barChart){ barChart.destroy(); barChart=null; }
    if(lineChart){ lineChart.destroy(); lineChart=null; }

    // BAR CHART
    const ctxBar = document.getElementById("barChart").getContext("2d");
    barChart = new Chart(ctxBar,{
      type:'bar',
      data:{ 
        labels:barLabels, 
        datasets:[{ 
          label:'Projects', 
          data:displayBarValues, 
          backgroundColor:'#007bff', // all bars blue
          borderRadius:6, 
          barPercentage:0.6, 
          categoryPercentage:0.7 
        }]
      },
      options:{ 
        responsive:true, 
        maintainAspectRatio:false, 
        scales:{ 
          y:{ beginAtZero:true, ticks:{ precision:0 } }, 
          x:{ grid:{ display:false } } 
        }, 
        plugins:{ legend:{ display:false } } 
      }
    });

    // LINE CHART
    const ctxLine = document.getElementById("lineChart").getContext("2d");
    lineChart = new Chart(ctxLine,{
      type:'line',
      data:{ 
        labels:lineLabels, 
        datasets:[{ 
          label: month==='All' ? 'Monthly projects' : 'Daily projects', 
          data:lineValues, 
          borderColor:'#ff4d4d', 
          backgroundColor:'rgba(255,77,77,0.16)', 
          fill:true, 
          tension:0.25, 
          pointRadius:3, 
          pointBackgroundColor:'#ff4d4d' 
        }] 
      },
      options:{ 
        responsive:true, 
        maintainAspectRatio:false, 
        scales:{ 
          y:{ beginAtZero:true, ticks:{ precision:0 } }, 
          x:{ grid:{ display:false } } 
        }, 
        plugins:{ legend:{ display:false } } 
      }
    });
  }

  // Initial render
  yearSelect.value = today.getFullYear();
  monthSelect.value = today.getMonth()+1;
  createCharts('All Teams', yearSelect.value, monthSelect.value);

  // Filter buttons
  document.getElementById("applyFilter").addEventListener("click",()=>{
    createCharts(teamSelect.value, yearSelect.value, monthSelect.value);
  });

  document.getElementById("resetFilter").addEventListener("click",()=>{
    yearSelect.value = today.getFullYear();
    monthSelect.value = today.getMonth()+1;
    teamSelect.value = 'All Teams';
    createCharts('All Teams', yearSelect.value, monthSelect.value);
  });

  // Admin dropdown
  const adminToggle = document.getElementById("adminToggle");
  const adminMenu = document.getElementById("adminMenu");
  adminToggle.addEventListener("click",(ev)=>{ ev.stopPropagation(); adminMenu.classList.toggle('show'); });
  document.addEventListener("click",(ev)=>{ if(!adminMenu.contains(ev.target)&&!adminToggle.contains(ev.target)) adminMenu.classList.remove('show'); });

  // Sidebar menu highlight
  document.querySelectorAll(".sidebar-menu li").forEach(item=>{ item.addEventListener("click",()=>{ document.querySelectorAll(".sidebar-menu li").forEach(i=>i.classList.remove("active")); item.classList.add("active"); }); });

  window.addEventListener("resize",debounce(()=>{
    if(barChart) barChart.resize();
    if(lineChart) lineChart.resize();
  },180));

  function debounce(fn,wait){ let t; return function(...a){ clearTimeout(t); t=setTimeout(()=>fn.apply(this,a),wait); }; }

  // Auto-update on change
  [yearSelect, monthSelect, teamSelect].forEach(el => {
    el.addEventListener("change", ()=>{
      createCharts(teamSelect.value, yearSelect.value, monthSelect.value);
    });
  });
});

// Highlight the active sidebar link
const sidebarLinks = document.querySelectorAll('#sidebar .nav-link');

sidebarLinks.forEach(link => {
  // Check if the link's href matches the current page URL
  if (link.href === window.location.href) {
    link.classList.add('active');
  }
});

// Function to animate numbers
function animateNumber(id, endValue, duration = 1500) {
  const element = document.getElementById(id);
  let start = 0;
  const increment = endValue / (duration / 16); // approx 60fps

  function updateNumber() {
    start += increment;
    if (start >= endValue) {
      element.textContent = endValue;
    } else {
      element.textContent = Math.floor(start);
      requestAnimationFrame(updateNumber);
    }
  }

  requestAnimationFrame(updateNumber);
}

// Example usage: replace with actual dynamic values
animateNumber("totalTeams", 12);      // animate to 12
animateNumber("totalProjects", 48);   // animate to 48

//profile section
document.addEventListener("DOMContentLoaded", () => {
  const userData = JSON.parse(localStorage.getItem("user"));

  if (userData) {
    const { name, email } = userData;
    const initial = name.charAt(0).toUpperCase();

    const profileInitial = document.getElementById("profileInitial");
    const profileInitialBig = document.getElementById("profileInitialBig");
    const bgColor = stringToColor(name);

    if (profileInitial) {
      profileInitial.textContent = initial;
      profileInitial.style.backgroundColor = bgColor;
      document.getElementById("profileName").textContent = name;
      document.getElementById("profileEmail").textContent = email;
    }

    if (profileInitialBig) {
      profileInitialBig.textContent = initial;
      profileInitialBig.style.backgroundColor = bgColor;
      document.getElementById("userName").textContent = name;
      document.getElementById("userEmail").textContent = email;
    }
  }
});

function stringToColor(str) {
  let hash = 0;
  for (let i = 0; i < str.length; i++) {
    hash = str.charCodeAt(i) + ((hash << 5) - hash);
  }
  let color = "#";
  for (let i = 0; i < 3; i++) {
    const value = (hash >> (i * 8)) & 0xff;
    color += ("00" + value.toString(16)).substr(-2);
  }
  return color;
}

document.querySelectorAll(".logout-btn").forEach(btn => {
  btn.addEventListener("click", (e) => {
    e.preventDefault();
    localStorage.removeItem("user");
    window.location.href = "userlogin.html";
  });
});

document.addEventListener("DOMContentLoaded", () => {
  const sidebar = document.getElementById("sidebar");
  const pageContent = document.getElementById("page-content");
  const toggle = document.getElementById("sidebarToggle");

  const toggleSidebar = () => {
    if (sidebar && pageContent) {
      sidebar.classList.toggle("show");
      pageContent.classList.toggle("sidebar-open");
    }
  };

  if (toggle && sidebar && pageContent) {
    toggle.addEventListener("click", toggleSidebar);
    pageContent.addEventListener("click", (e) => {
      // Only close sidebar if the click is on the content area/overlay on small screens
      if (pageContent.classList.contains("sidebar-open") && window.innerWidth < 992 && e.target === pageContent) {
        toggleSidebar();
      }
    });
    document.querySelectorAll("#sidebar .nav-link").forEach((link) => {
      link.addEventListener("click", () => {
        // Close sidebar after navigating on mobile
        if (window.innerWidth < 992 && sidebar.classList.contains("show")) {
          setTimeout(toggleSidebar, 100);
        }
      });
    });
  }
});

// ---------------------- Dynamic User Info (Navbar Profile) ----------------------
document.addEventListener("DOMContentLoaded", () => {
  const userData = JSON.parse(localStorage.getItem("user"));

  if (userData) {
    const { name, email } = userData;
    const initial = name.charAt(0).toUpperCase();

    // Create a consistent color for user icon
    const bgColor = stringToColor(name);

    // Update small profile section in navbar
    const profileInitial = document.getElementById("profileInitial");
    profileInitial.textContent = initial;
    profileInitial.style.backgroundColor = bgColor;
    profileInitial.style.color = "#fff";

    // Update visible name & email in navbar
    document.getElementById("navbarProfileName").textContent = name;
    document.getElementById("navbarProfileEmail").textContent = email;

    // Update dropdown large profile
    const profileInitialBig = document.getElementById("profileInitialBig");
    profileInitialBig.textContent = initial;
    profileInitialBig.style.backgroundColor = bgColor;
    profileInitialBig.style.color = "#fff";

    document.getElementById("userName").textContent = name;
    document.getElementById("userEmail").textContent = email;
  } else {
    // If no user data, redirect back to login
    window.location.href = "userlogin.html";
  }
});

// Helper: Convert name → consistent color
function stringToColor(str) {
  let hash = 0;
  for (let i = 0; i < str.length; i++) {
    hash = str.charCodeAt(i) + ((hash << 5) - hash);
  }
  let color = '#';
  for (let i = 0; i < 3; i++) {
    const value = (hash >> (i * 8)) & 0xFF;
    color += ('00' + value.toString(16)).substr(-2);
  }
  return color;
}

// ----------------- Resource Allocation Form -----------------
document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("resource_allocation_form");
  if (!form) return;

  form.addEventListener("submit", (e) => {
    e.preventDefault();

    const allocationId = document.getElementById("allocation_id").value.trim();
    const projectId    = document.getElementById("project_id").value.trim();
    const itTeam       = document.getElementById("it_team").value.trim();
    const startDate    = document.getElementById("start_date").value;
    const endDate      = document.getElementById("end_date").value;

    if (!allocationId || !projectId || !itTeam || !startDate || !endDate) {
      alert("Please fill all fields.");
      return;
    }

    // TODO: if you want to send to backend later, call fetch() here.
    // For now just show success.
    alert("Resource allocation saved (frontend).");
    form.reset();
  });
});
