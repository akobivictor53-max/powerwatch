'use strict';

/**
 * Centralized, validated environment configuration.
 *
 * This is the ONLY place the process should read from `process.env`.
 * Every other module imports the parsed `config` object from here instead
 * of touching `process.env` directly. That keeps secret-handling auditable
 * in one spot, and lets us fail fast (and loudly) at boot time if something
 * required — like the Sogo secret key — is missing.
 */

require('dotenv').config();

function required(name) {
  const value = process.env[name];
  if (!value || !value.trim()) {
    throw new Error(
      `[config] Missing required environment variable: ${name}. ` +
        `Copy .env.example to .env and fill it in.`
    );
  }
  return value.trim();
}

function optional(name, fallback) {
  const value = process.env[name];
  return value && value.trim() ? value.trim() : fallback;
}

function requiredInt(name, fallback) {
  const raw = process.env[name];
  if (!raw) return fallback;
  const n = parseInt(raw, 10);
  if (Number.isNaN(n)) {
    throw new Error(`[config] Environment variable ${name} must be an integer.`);
  }
  return n;
}

const config = {
  env: optional('NODE_ENV', 'development'),
  port: requiredInt('PORT', 8080),
  allowedOrigins: optional('ALLOWED_ORIGINS', 'http://localhost:8080')
    .split(',')
    .map((o) => o.trim())
    .filter(Boolean),

  sogo: {
    // These two are the ones that must NEVER end up in the Android app.
    baseUrl: required('SOGO_API_BASE_URL'),
    secretKey: required('SOGO_API_SECRET_KEY'),
    publicKey: optional('SOGO_API_PUBLIC_KEY', ''),
    timeoutMs: requiredInt('SOGO_REQUEST_TIMEOUT_MS', 15000),
  },

  appClientKey: required('APP_CLIENT_KEY'),

  rateLimit: {
    windowMs: requiredInt('RATE_LIMIT_WINDOW_MS', 60000),
    max: requiredInt('RATE_LIMIT_MAX_REQUESTS', 20),
  },
};

// Extra safety net: refuse to boot if a placeholder value made it into a
// production environment. This has caught real incidents in other projects.
const placeholderPattern = /REPLACE_WITH/i;
if (config.env === 'production') {
  if (placeholderPattern.test(config.sogo.secretKey)) {
    throw new Error(
      '[config] SOGO_API_SECRET_KEY still looks like a placeholder. Refusing to start in production.'
    );
  }
  if (placeholderPattern.test(config.appClientKey)) {
    throw new Error(
      '[config] APP_CLIENT_KEY still looks like a placeholder. Refusing to start in production.'
    );
  }
}

module.exports = config;
