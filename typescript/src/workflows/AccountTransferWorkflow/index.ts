import {
  proxyActivities,
  proxyLocalActivities,
  sleep,
  workflowInfo,
  defineQuery,
  defineSignal,
  defineUpdate,
  upsertSearchAttributes,
  ActivityFailure,
  executeChild,
  setHandler,
  log,
  uuid4,
  condition,
  ParentClosePolicy,
  RetryPolicy,
} from '@temporalio/workflow';
import type * as activities from '../../activities/index';
import type { TransferInput, TransferOutput } from "../../types";

const DEFAULT_RETRY_POLICY: RetryPolicy = {
  initialInterval: '1s',
  maximumInterval: '30s',
  backoffCoefficient: 2,
};

const { validateAsync, withdrawAsync, depositAsync, sendNotificationAsync } =
  proxyActivities<typeof activities>({
    startToCloseTimeout: '5s',
    retry: DEFAULT_RETRY_POLICY,
  });

export async function AccountTransferWorkflow(input: TransferInput): Promise<TransferOutput> {
  const { workflowType } = workflowInfo();

  let progress:number, transferState:string;

  log.info(`Account Transfer workflow started, type = ${workflowType}`);
  const idempotencyKey = uuid4();

  // Validate
  await validateAsync(input);
  ({progress, transferState } = await updateProgressAsync(25, 1));

  // Withdraw
  await withdrawAsync(idempotencyKey, input.amount, workflowType);

  return null;
}

async function updateProgressAsync(progress: number, sleepDuration: number) {
  return updateProgressAsyncComplex(progress, sleepDuration, 'running');
}

async function updateProgressAsyncComplex(progress: number, sleepDuration: number, transferState: string) {
  if(sleepDuration > 0) {
    await sleep(sleepDuration);
  }

  return {transferState, progress};
}