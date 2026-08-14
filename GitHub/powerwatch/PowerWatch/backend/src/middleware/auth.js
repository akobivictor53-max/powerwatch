'use strict';

const config = require('../config/env');

/**
 * Simple shared-secret check between the Android app and this backend.
 *
 * This is NOT the Sogo secret key — it's a separate key that only proves
 * "this request came from our app," so random internet clients can't hit
 * your backend and burn your Sogo API quota. Rotate it independently of
 * the Sogo key.
 */
function requireAppKey(req, res, next) {
  const providedKey = req.header('x-app-key');

  if (!providedKey || providedKey !== config.appClientKey) {
    return res.status(401).json({
      success: false,
      error: {
        code: 'UNAUTHORIZED',
        message: 'Missing or invalid client key.',
      },
    });
  }

  next();
}

module.exports = { requireAppKey };
