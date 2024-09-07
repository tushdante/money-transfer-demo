package io.temporal.samples.moneytransfer.workflows;

import io.temporal.samples.moneytransfer.model.TransferStatus;
import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.UpdateMethod;
import io.temporal.workflow.UpdateValidatorMethod;

public interface AccountTransferMessages {
    @QueryMethod(name = "transferStatus")
    TransferStatus queryTransferStatus();

    @SignalMethod(name = "approveTransfer")
    void approveTransferSignal();

    @UpdateMethod
    String approveTransferUpdate();

    @UpdateValidatorMethod(updateName = "approveTransferUpdate")
    void approveTransferUpdateValidator();
}
