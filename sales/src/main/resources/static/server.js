const express = require('express');
const cors = require('cors');

const app = express();
const PORT = 5000;

// Middleware
app.use(cors());
app.use(express.json());

// Routes
app.use('/api/sales', require('./routes/sales'));
app.use('/api/performance', require('./routes/performance'));

// Default route
app.get('/', (req, res) => {
  res.send('Sales ERP Backend Running...');
});

// Start server
app.listen(PORT, () => {
  console.log(`Server running at http://localhost:${PORT}`);
});
