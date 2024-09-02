package io.temporal.samples.moneytransfer.model;

public enum ExecutionScenario {
    HAPPY_PATH("AccountTransferWorkflow"),
    ADVANCED_VISIBILITY("AccountTransferWorkflowAdvancedVisibility"),
    HUMAN_IN_LOOP("AccountTransferWorkflowHumanInLoop"),
    API_DOWNTIME("AccountTransferWorkflowAPIDowntime"),
    BUG_IN_WORKFLOW("AccountTransferWorkflowRecoverableFailure"),
    INVALID_ACCOUNT("AccountTransferWorkflowInvalidAccount");

    private final String workflowType;

    ExecutionScenario(String workflowType) {
        this.workflowType = workflowType;
    }

    public String getWorkflowType() {
        return this.workflowType;
    }
}
