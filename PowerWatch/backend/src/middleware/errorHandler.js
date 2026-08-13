'use strict';

const logger = require('../utils/logger');

/**
 * Catches anything thrown/next(err)'d in route handlers and turns it into
 * a consistent JSON error shape. Never leaks stack traces or internal
 * details (like the Sogo base URL) to the client.
 */
function errorHandler(err, req, res, _next) {
  const statusCode = err.statusCode || 500;

  logger.error({ err, path: req.path }, 'Request failed');

  res.status(statusCode).json({
    success: false,
    error: {
      code: err.name || 'INTERNAL_ERROR',
      message:
        statusCode >= 500
          ? 'Something went wrong on our end. Please try again shortly.'
          : err.message,
    },
  });
}

function notFoundHandler(req, res) {
  res.status(404).json({
    success: false,
    error: { code: 'NOT_FOUND', message: 'Route not found.' },
  });
}

module.exports = { errorHandler, notFoundHandler };
