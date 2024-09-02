package io.temporal.samples.moneytransfer;

import static io.temporal.samples.moneytransfer.TransferRequester.runApproveSignal;

public class TransferApprover {

    public static void main(String[] args) {
        String workflowId = args[0];
        System.out.println("Signaling workflow: " + workflowId);
        runApproveSignal(workflowId);
    }
}
