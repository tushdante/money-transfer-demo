import { MockActivityEnvironment } from '@temporalio/testing';
import { describe, it } from 'mocha';
import * as activities from '../src/activities/index';
import type { TransferInput } from '../src/types';
import assert from 'assert';

const transferInput:TransferInput = {
  amount: 100,
  fromAccount: 'account1',
  toAccount: 'account2'
}

describe('validateAsync activity', async () => {
  it('successfully Validate Transfer Input', async () => {
    const env = new MockActivityEnvironment();
    const result = await env.run(activities.validateAsync, transferInput);
    assert.equal(result, activities.SUCCESS);
  });
});

/*
describe('withdrawAsync activity', async () => {
  it('successfully Validate Transfer Input', async () => {
    const env = new MockActivityEnvironment();
    const result = await env.run(activities.withdrawAsync, transferInputTest);
    assert.equal(result, activities.SUCCESS);
  });
});

describe('depositAsync activity', async () => {
  it('successfully Validate Transfer Input', async () => {
    const env = new MockActivityEnvironment();
    const result = await env.run(activities.depositAsync, transferInputTest);
    assert.equal(result, activities.SUCCESS);
  });
});
*/

describe('sendNotificationAsync activity', async () => {
  it('successfully Send Notification', async () => {
    const env = new MockActivityEnvironment();
    const result = await env.run(activities.sendNotificationAsync, transferInput);
    assert.equal(result, activities.SUCCESS);
  });
});

describe('undoWithdrawAsync activity', async () => {
  it('successfully Undo Withdraw', async () => {
    const env = new MockActivityEnvironment();
    const amount = 100;
    const result = await env.run(activities.undoWithdrawAsync, amount);
    assert.equal(result, true);
  });
});