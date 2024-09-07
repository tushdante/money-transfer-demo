package io.temporal.samples.moneytransfer.workflows;

import io.temporal.samples.moneytransfer.model.TransferInput;
import io.temporal.samples.moneytransfer.model.TransferOutput;
import io.temporal.samples.moneytransfer.model.TransferStatus;
import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface AccountTransferWorkflow {
    @WorkflowMethod(name = "moneyTransferWorkflow")
    TransferOutput transfer(TransferInput input);

    @QueryMethod(name = "transferStatus")
    TransferStatus queryTransferStatus();
}
