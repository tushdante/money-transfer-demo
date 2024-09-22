import { NativeConnection, Runtime, Worker } from '@temporalio/worker';
import * as activities from './activities/index';
import { getWorkflowOptions, getConnectionOptions, getTelemetryOptions, namespace, taskQueue } from './env';
import { getDataConverter } from './security/data-converter';
import { encryptPayloads } from './env';

async function main() {
  const telemetryOptions = getTelemetryOptions();

  if (telemetryOptions) {
    Runtime.install(telemetryOptions);
  }

  const connectionOptions = await getConnectionOptions();
  const connection = await NativeConnection.connect(connectionOptions);

  if(encryptPayloads === 'true') {
    console.info('🤖: Data Converter Set.');
  } else {
    console.info(`🤖: Data Converter Not Set.`);
  }

  const worker = await Worker.create({
    ...getWorkflowOptions(),
    connection,
    namespace,
    taskQueue,
    activities: { ...activities },
    ...(encryptPayloads === 'true' && {
      dataConverter: await getDataConverter()
    })
  });

  console.info('🤖: Temporal Worker Online! Beep Boop Beep!');
  await worker.run();
}

main().then(
  () => void process.exit(0),
  (err) => {
    console.error(err);
    process.exit(1);
  },
);
