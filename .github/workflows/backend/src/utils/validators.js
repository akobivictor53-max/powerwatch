'use strict';

const Joi = require('joi');
const { DISCO_CODES } = require('../config/discos');

/**
 * Validates a meter-verification request body.
 * Nigerian prepaid/postpaid meter numbers are numeric and typically
 * 11-13 digits. We keep the bound generous (10-13) to avoid rejecting
 * valid meters, while still blocking obvious garbage input.
 */
const verifyMeterSchema = Joi.object({
  meterNumber: Joi.string()
    .trim()
    .pattern(/^\d{10,13}$/)
    .required()
    .messages({
      'string.pattern.base': 'Meter number must be 10-13 digits.',
      'string.empty': 'Meter number is required.',
    }),
  discoCode: Joi.string()
    .trim()
    .uppercase()
    .valid(...DISCO_CODES)
    .required()
    .messages({
      'any.only': 'Unsupported or unknown Disco selected.',
      'string.empty': 'Disco selection is required.',
    }),
  meterType: Joi.string().valid('prepaid', 'postpaid').optional(),
});

module.exports = { verifyMeterSchema };
