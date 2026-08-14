'use strict';

/**
 * Supported Nigerian Electricity Distribution Companies (Discos).
 *
 * `code` is whatever identifier Sogo's API expects for that Disco.
 * These are placeholder codes based on common convention in the Nigerian
 * bill-payment industry (Interswitch/Quickteller/Paystack style short
 * codes) — confirm the exact codes Sogo expects against their real docs
 * and adjust here. This is the ONLY file you should need to touch to
 * add/rename a Disco.
 */
const DISCOS = [
  { code: 'IKEDC', name: 'Ikeja Electric' },
  { code: 'EKEDC', name: 'Eko Electricity' },
  { code: 'AEDC', name: 'Abuja Electricity' },
  { code: 'PHED', name: 'Port Harcourt Electricity' },
  { code: 'KEDCO', name: 'Kano Electricity' },
  { code: 'JED', name: 'Jos Electricity' },
  { code: 'IBEDC', name: 'Ibadan Electricity' },
  { code: 'KAEDCO', name: 'Kaduna Electricity' },
  { code: 'EEDC', name: 'Enugu Electricity' },
  { code: 'BEDC', name: 'Benin Electricity' },
  { code: 'YEDC', name: 'Yola Electricity' },
];

const DISCO_CODES = new Set(DISCOS.map((d) => d.code));

module.exports = { DISCOS, DISCO_CODES };
