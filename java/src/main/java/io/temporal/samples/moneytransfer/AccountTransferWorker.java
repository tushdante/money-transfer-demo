package io.temporal.samples.moneytransfer;

import io.temporal.samples.moneytransfer.helper.ServerInfo;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;

public class AccountTransferWorker {

    @SuppressWarnings("CatchAndPrintStackTrace")
    public static void main(String[] args) throws Exception {
        final String TASK_QUEUE = ServerInfo.getTaskqueue();

        // worker factory that can be used to create workers for specific task queues
        WorkerFactory factory = WorkerFactory.newInstance(TemporalClient.get());
        Worker workerForCommonTaskQueue = factory.newWorker(TASK_QUEUE);
        workerForCommonTaskQueue.registerWorkflowImplementationTypes(
                AccountTransferWorkflowImpl.class
        );
        workerForCommonTaskQueue.registerWorkflowImplementationTypes(
                AccountTransferWorkflowScenarios.class
        );
        AccountTransferActivities accountTransferActivities =
                new AccountTransferActivitiesImpl();
        workerForCommonTaskQueue.registerActivitiesImplementations(
                accountTransferActivities
        );
        // Start all workers created by this factory.
        factory.start();
        System.out.println("Worker started for task queue: " + TASK_QUEUE);
    }
}
