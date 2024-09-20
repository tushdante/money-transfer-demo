import { ApplicationFailure, Context, log } from '@temporalio/activity';
import type { TransferInput } from '../types';

export const API_DOWNTIME = 'AccountTransferWorkflowAPIDowntime';
export const INVALID_ACCOUNT = 'AccountTransferWorkflowInvalidAccount';
export const NO_ERROR = 'NoError';
export const SUCCESS = 'SUCCESS';

async function simulateExternalOperationAsync(ms: number) {
  await new Promise((resolve) => setTimeout(resolve, ms));
  return SUCCESS;
}

async function simulateExternalOperationAsyncComplex(ms: number, type: string, attempt: number) {
  await simulateExternalOperationAsync(ms / attempt);
  return attempt < 5 ? type : NO_ERROR
}

export async function validateAsync(input: TransferInput) {
  log.info(`Validate activity started, input = ${input}`);

  await simulateExternalOperationAsync(1000);

  return SUCCESS;
}

export async function withdrawAsync(idempotencyKey: string, amount: number, type: string) {
  log.info(`Withdraw activity started, amount = ${amount}`);

  const context = Context.current();
  const { attempt } = context.info;

  // Simulate External API Call
  const error = await simulateExternalOperationAsyncComplex(1000, type, attempt);
  log.info(`Withdraw call complete, type = ${type}, error = ${error}`);
  
  if(error == API_DOWNTIME) {
    log.error(`Withdraw API unavailable, attempt = ${attempt}`);

    throw ApplicationFailure.create({
      message: 'Withdraw activity failed, API unavailable',
    });
  }

  return SUCCESS;
}

export async function depositAsync(idempotencyKey: string, amount: number, type: string) {
  log.info(`Deposit activity started, amount = ${amount}`);

  const context = Context.current();
  const { attempt } = context.info;

  const error = await simulateExternalOperationAsyncComplex(1000, type, attempt);
  log.info(`Deposit call complete, type = ${type}, error = ${error}`);

  if(error == INVALID_ACCOUNT) {
    throw ApplicationFailure.create({
      nonRetryable: true,
      message: 'Deposit activity failed, account is invalid',
    });
  }
  return null;
}

export async function sendNotificationAsync(input: TransferInput) {
  log.info(`Send notification activity started, input = ${input}`);

  await simulateExternalOperationAsync(1000);

  return SUCCESS;
}

export async function undoWithdrawAsync(amount: number) {
  log.info(`Undo withdraw activity started, amount = ${amount}`);

  await simulateExternalOperationAsync(1000);

  return true;
}
