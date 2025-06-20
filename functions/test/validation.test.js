const assert = require('assert');
const { generatePuzzles } = require('../generate-puzzles/index');
const { getPuzzle } = require('../get-puzzle/index');

function createMockRes() {
  return {
    statusCode: null,
    body: null,
    headers: {},
    status(code) { this.statusCode = code; return this; },
    json(payload) { this.body = payload; return this; },
    set(key, value) { this.headers[key] = value; return this; },
    send(payload) { this.body = payload; return this; }
  };
}

(async () => {
  // Test invalid days values
  {
    const req = { query: { days: '0' } };
    const res = createMockRes();
    await generatePuzzles(req, res);
    assert.strictEqual(res.statusCode, 400, 'days=0 should return 400');
  }

  {
    const req = { query: { days: '31' } };
    const res = createMockRes();
    await generatePuzzles(req, res);
    assert.strictEqual(res.statusCode, 400, 'days=31 should return 400');
  }

  {
    const req = { query: { days: 'abc' } };
    const res = createMockRes();
    await generatePuzzles(req, res);
    assert.strictEqual(res.statusCode, 400, 'days=abc should return 400');
  }

  // Test invalid date formats
  {
    const req = { method: 'GET', query: { date: '20210101' } };
    const res = createMockRes();
    await getPuzzle(req, res);
    assert.strictEqual(res.statusCode, 400, 'date without dashes should return 400');
  }

  {
    const req = { method: 'GET', query: { date: '2021-1-01' } };
    const res = createMockRes();
    await getPuzzle(req, res);
    assert.strictEqual(res.statusCode, 400, 'date missing zero padding should return 400');
  }

  console.log('All validation tests passed.');
})();
