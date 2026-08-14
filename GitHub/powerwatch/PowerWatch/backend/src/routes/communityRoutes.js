'use strict';

const express = require('express');
const Joi = require('joi');
const rateLimit = require('express-rate-limit');
const { DISCO_CODES } = require('../config/discos');

/**
 * OPTIONAL feature: community-submitted power status reports.
 *
 * These are explicitly user-generated and are NEVER presented as
 * authoritative "power on/off" data from Sogo or a Disco — the Android
 * app must always label this section "Community reports (user-submitted)".
 *
 * This implementation uses a simple in-memory store to keep the demo
 * self-contained. For production, replace `store` with a real database
 * (Postgres/Redis) and add abuse protections (e.g. one report per
 * device/meter per time window, moderation, etc).
 */

const router = express.Router();

/** @type {Array<{id: string, discoCode: string, areaOrMeterHint: string, status: 'on'|'off', reportedAt: string}>} */
const store = [];

const reportSchema = Joi.object({
  discoCode: Joi.string().trim().uppercase().valid(...DISCO_CODES).required(),
  areaOrMeterHint: Joi.string().trim().max(80).required().messages({
    'string.empty': 'Please provide an area name (e.g. "Ikeja, Opebi Rd").',
  }),
  status: Joi.string().valid('on', 'off').required(),
});

const reportLimiter = rateLimit({
  windowMs: 60_000,
  max: 5,
  standardHeaders: true,
  legacyHeaders: false,
  message: {
    success: false,
    error: { code: 'RATE_LIMITED', message: 'Too many reports submitted. Please wait a moment.' },
  },
});

/** GET /api/community/reports?discoCode=IKEDC — most recent reports first, capped. */
router.get('/community/reports', (req, res) => {
  const { discoCode } = req.query;
  let results = store;
  if (discoCode) {
    results = results.filter((r) => r.discoCode === String(discoCode).toUpperCase());
  }
  const recent = [...results].sort((a, b) => (a.reportedAt < b.reportedAt ? 1 : -1)).slice(0, 20);
  res.json({ success: true, data: recent, disclaimer: 'User-submitted reports, not verified by Sogo or any Disco.' });
});

/** POST /api/community/reports — submit a report. */
router.post('/community/reports', reportLimiter, (req, res) => {
  const { error, value } = reportSchema.validate(req.body);
  if (error) {
    return res.status(400).json({
      success: false,
      error: { code: 'VALIDATION_ERROR', message: error.details[0].message },
    });
  }

  const report = {
    id: `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
    ...value,
    reportedAt: new Date().toISOString(),
  };
  store.push(report);

  res.status(201).json({ success: true, data: report });
});

module.exports = router;
