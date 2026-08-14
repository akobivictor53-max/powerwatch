'use strict';

const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const pinoHttp = require('pino-http');

const config = require('./config/env');
const logger = require('./utils/logger');
const { requireAppKey } = require('./middleware/auth');
const { errorHandler, notFoundHandler } = require('./middleware/errorHandler');
const meterRoutes = require('./routes/meterRoutes');
const communityRoutes = require('./routes/communityRoutes');

const app = express();

app.use(helmet());
app.use(
  cors({
    origin: config.allowedOrigins,
  })
);
app.use(express.json({ limit: '10kb' }));
app.use(pinoHttp({ logger, autoLogging: { ignore: (req) => req.url === '/health' } }));

// Health check — no auth required, useful for uptime monitors / load balancers.
app.get('/health', (req, res) => {
  res.json({ success: true, status: 'ok', env: config.env });
});

// Everything under /api requires the app client key.
app.use('/api', requireAppKey, meterRoutes);
app.use('/api', requireAppKey, communityRoutes);

app.use(notFoundHandler);
app.use(errorHandler);

app.listen(config.port, () => {
  logger.info(`PowerWatch backend listening on port ${config.port} (${config.env})`);
});

module.exports = app;
