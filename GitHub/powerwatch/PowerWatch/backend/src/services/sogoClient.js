'use strict';

const axios = require('axios');
const config = require('../config/env');
const logger = require('../utils/logger');

/**
 * ============================================================================
 * Sogo API client — meter verification adapter
 * ============================================================================
 *
 * ⚠️  IMPORTANT — READ BEFORE DEPLOYING
 * As of this writing, Sogo's public bill-payments developer API is listed
 * as "in the works" / not yet publicly documented. The request shape,
 * auth header name, and response field names below are written as a
 * best-guess placeholder following common conventions used by Nigerian
 * bill-payment aggregators (Paystack/Interswitch/VTpass-style APIs), so
 * that the rest of this backend (routes, validation, error handling,
 * security) is fully wired and ready to go.
 *
 * Before going live you MUST:
 *   1. Get Sogo's real API docs / OpenAPI spec from their team.
 *   2. Update SOGO_API_BASE_URL in your .env.
 *   3. Update `buildRequest()` below to match their exact request format.
 *   4. Update `normalizeResponse()` below to match their exact response
 *      field names.
 *
 * Everything outside this file (routes/middleware/Android app) talks to
 * the normalized shape returned by `verifyMeter()`, so once you fix this
 * one file the rest of the app keeps working unchanged.
 * ============================================================================
 */

const sogoHttp = axios.create({
  baseURL: config.sogo.baseUrl,
  timeout: config.sogo.timeoutMs,
  headers: {
    'Content-Type': 'application/json',
    // TODO: Confirm the real auth header name/format with Sogo's docs.
    // Common patterns: "Authorization: Bearer <key>" or "x-api-key: <key>".
    Authorization: `Bearer ${config.sogo.secretKey}`,
    ...(config.sogo.publicKey ? { 'x-public-key': config.sogo.publicKey } : {}),
  },
});

/**
 * Builds the outgoing request payload for Sogo's meter-verification
 * endpoint. TODO: adjust field names to match Sogo's real spec.
 */
function buildRequest({ meterNumber, discoCode }) {
  return {
    meter_number: meterNumber,
    disco: discoCode,
  };
}

/**
 * Normalizes Sogo's raw response into a stable internal shape.
 * TODO: adjust field extraction to match Sogo's real response body.
 *
 * We deliberately do NOT invent fields that Sogo doesn't return —
 * anything not present comes back as `null` rather than a fake default.
 */
function normalizeResponse(raw) {
  const data = raw?.data ?? raw ?? {};

  return {
    verified: Boolean(data.verified ?? data.status === 'success'),
    customerName: data.customer_name ?? data.customerName ?? null,
    meterNumberMasked: maskMeterNumber(data.meter_number ?? data.meterNumber ?? null),
    meterType: data.meter_type ?? data.meterType ?? null,
    discoCode: data.disco ?? data.discoCode ?? null,
    address: data.address ?? null,
    // Only present if Sogo (or another legitimate, disclosed source)
    // actually returns it. We never fabricate this.
    outageStatus: data.outage_status ?? null,
  };
}

/**
 * Masks a meter number for display, e.g. "04512345678" -> "0451****678".
 * Keeps the first 4 and last 3 digits visible.
 */
function maskMeterNumber(meterNumber) {
  if (!meterNumber || typeof meterNumber !== 'string') return null;
  if (meterNumber.length <= 7) return meterNumber;
  const start = meterNumber.slice(0, 4);
  const end = meterNumber.slice(-3);
  const middle = '*'.repeat(meterNumber.length - 7);
  return `${start}${middle}${end}`;
}

/**
 * Calls Sogo's meter verification endpoint.
 *
 * @param {{meterNumber: string, discoCode: string}} params
 * @returns {Promise<{verified: boolean, customerName: string|null,
 *   meterNumberMasked: string|null, meterType: string|null,
 *   discoCode: string|null, address: string|null, outageStatus: string|null}>}
 */
async function verifyMeter({ meterNumber, discoCode }) {
  const payload = buildRequest({ meterNumber, discoCode });

  try {
    // TODO: confirm the real endpoint path with Sogo's docs.
    const response = await sogoHttp.post('/meters/verify', payload);
    return normalizeResponse(response.data);
  } catch (err) {
    if (err.response) {
      // Sogo responded with an error status.
      logger.warn(
        { status: err.response.status, body: err.response.data },
        'Sogo API returned an error response'
      );
      const message =
        err.response.data?.message ||
        err.response.data?.error ||
        'The meter could not be verified with the provider.';
      const providerError = new Error(message);
      providerError.name = 'SogoApiError';
      providerError.statusCode = err.response.status === 404 ? 404 : 502;
      throw providerError;
    }

    if (err.code === 'ECONNABORTED') {
      logger.error('Sogo API request timed out');
      const timeoutError = new Error('The verification provider timed out. Please try again.');
      timeoutError.name = 'SogoTimeoutError';
      timeoutError.statusCode = 504;
      throw timeoutError;
    }

    logger.error({ err }, 'Unexpected error calling Sogo API');
    const genericError = new Error('Could not reach the verification provider.');
    genericError.name = 'SogoConnectionError';
    genericError.statusCode = 502;
    throw genericError;
  }
}

module.exports = { verifyMeter, maskMeterNumber };
