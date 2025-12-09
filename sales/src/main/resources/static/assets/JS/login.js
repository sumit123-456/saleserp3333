// login.js
// login.js
// This script performs a real backend login request to /api/v1/auth/login
// Stores returned token and user data in localStorage if successful.

document.addEventListener('DOMContentLoaded', function () {
  const form = document.getElementById('loginForm');
  if (!form) return;

  form.addEventListener('submit', async (e) => {
    e.preventDefault();

    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value.trim();
    if (!email || !password) {
      alert('Provide email and password');
      return;
    }

    try {
      const API_BASE = 'http://localhost:9090';
      const res = await fetch(API_BASE + '/api/v1/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password })
      });

      const body = await res.json();
      console.log('Login response:', body); // DEBUG

      if (!res.ok) {
        // backend returns LoginResponse with success=false
        alert(body.message || 'Login failed');
        return;
      }

      // The backend wraps the payload under `data` (CommonUtil.createBuildResponse)
      // Response format: { status: "OK", message: "success", data: { userId, fullName, email, role, token, ... } }
      const payload = body?.data;
      console.log('Extracted payload:', payload); // DEBUG

      if (!payload) {
        alert('Invalid login response from server');
        return;
      }

      // Store authToken (required for protected endpoints)
      if (payload.token) {
        localStorage.setItem('authToken', payload.token);
        console.log('Token stored:', payload.token.substring(0, 20) + '...'); // DEBUG
      } else {
        alert('No token received from server');
        return;
      }

      // Store user info for display
      try {
        const shortUser = { 
          name: payload.fullName || payload.email?.split('@')[0] || 'User', 
          email: payload.email 
        };
        localStorage.setItem('user', JSON.stringify(shortUser));
      } catch (e) {
        console.error('Error storing user:', e);
      }

      // Store complete user data
      const userData = {
        userId: payload.userId || null,
        fullName: payload.fullName || null,
        email: payload.email || null,
        role: payload.role || null,
        teamAllocation: payload.teamAllocation || null,
        callTarget: payload.callTarget || null,
        monthlyTarget: payload.monthlyTarget || null
      };

      localStorage.setItem('userData', JSON.stringify(userData));
      console.log('User data stored:', userData); // DEBUG

      // redirect to main page
      setTimeout(() => {
        window.location.href = 'index.html';
      }, 500);
    } catch (err) {
      console.error('Login error', err);
      alert('Unable to login — check server and network');
    }
  });
});
