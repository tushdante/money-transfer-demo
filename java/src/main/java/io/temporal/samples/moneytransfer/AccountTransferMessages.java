package io.temporal.samples.moneytransfer;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.temporal.samples.moneytransfer.model.TransferStatus;
import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.UpdateMethod;
import io.temporal.workflow.UpdateValidatorMethod;

public interface AccountTransferMessages {
    @QueryMethod(name = "transferStatus")
    TransferStatus getStateQuery() throws JsonProcessingException;

    @SignalMethod(name = "approveTransfer")
    void approveTransfer();

    @UpdateMethod
    String approveTransferUpdate();

    @UpdateValidatorMethod(updateName = "approveTransferUpdate")
    void approveTransferUpdateValidator();
}
