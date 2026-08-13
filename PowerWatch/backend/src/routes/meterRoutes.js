'use strict';

const express = require('express');
const rateLimit = require('express-rate-limit');
const config = require('../config/env');
const { verifyMeterSchema } = require('../utils/validators');
const sogoClient = require('../services/sogoClient');
const { DISCOS } = require('../config/discos');
const logger = require('../utils/logger');

const router = express.Router();

const verifyLimiter = rateLimit({
  windowMs: config.rateLimit.windowMs,
  max: config.rateLimit.max,
  standardHeaders: true,
  legacyHeaders: false,
  message: {
    success: false,
    error: { code: 'RATE_LIMITED', message: 'Too many requests. Please slow down.' },
  },
});

/**
 * GET /api/discos
 * Returns the list of supported Discos for the app's selector UI.
 */
router.get('/discos', (req, res) => {
  res.json({ success: true, data: DISCOS });
});

/**
 * POST /api/meters/verify
 * Body: { meterNumber: string, discoCode: string }
 *
 * Validates input, calls Sogo, and returns only what Sogo actually
 * reports. No status is invented here.
 */
router.post('/meters/verify', verifyLimiter, async (req, res, next) => {
  const { error, value } = verifyMeterSchema.validate(req.body);

  if (error) {
    return res.status(400).json({
      success: false,
      error: { code: 'VALIDATION_ERROR', message: error.details[0].message },
    });
  }

  try {
    logger.info({ discoCode: value.discoCode }, 'Verifying meter');
    const result = await sogoClient.verifyMeter({
      meterNumber: value.meterNumber,
      discoCode: value.discoCode,
    });

    if (!result.verified) {
      return res.status(404).json({
        success: false,
        error: {
          code: 'METER_NOT_VERIFIED',
          message: 'This meter number could not be verified. Double-check the number and Disco.',
        },
      });
    }

    return res.json({ success: true, data: result });
  } catch (err) {
    next(err);
  }
});

module.exports = router;
