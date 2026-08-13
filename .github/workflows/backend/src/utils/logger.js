'use strict';

const pino = require('pino');
const config = require('../config/env');

/**
 * App-wide structured logger.
 *
 * IMPORTANT: never log secrets (API keys, full meter numbers if you want to
 * be extra cautious, etc). Redact paths are set below for common spots
 * where a secret could accidentally leak into logs.
 */
const logger = pino({
  level: config.env === 'production' ? 'info' : 'debug',
  redact: {
    paths: [
      'req.headers.authorization',
      'req.headers["x-app-key"]',
      '*.secretKey',
      '*.apiKey',
      '*.SOGO_API_SECRET_KEY',
    ],
    censor: '[REDACTED]',
  },
});

module.exports = logger;
