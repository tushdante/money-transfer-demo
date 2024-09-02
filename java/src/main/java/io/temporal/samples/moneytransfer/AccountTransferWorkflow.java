package io.temporal.samples.moneytransfer;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.temporal.samples.moneytransfer.model.TransferInput;
import io.temporal.samples.moneytransfer.model.TransferOutput;
import io.temporal.samples.moneytransfer.model.TransferStatus;
import io.temporal.workflow.*;

@WorkflowInterface
public interface AccountTransferWorkflow {
    @WorkflowMethod(name = "moneyTransferWorkflow")
    TransferOutput transfer(TransferInput params);

    @QueryMethod(name = "transferStatus")
    TransferStatus getStateQuery() throws JsonProcessingException;

    @SignalMethod(name = "approveTransfer")
    void approveTransfer();

    @UpdateMethod
    String approveTransferUpdate();

    @UpdateValidatorMethod(updateName = "approveTransferUpdate")
    void approveTransferUpdateValidator();
}
