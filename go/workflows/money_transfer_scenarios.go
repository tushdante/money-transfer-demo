package workflows

import (
	"fmt"
	"github.com/google/uuid"
	"go.temporal.io/sdk/temporal"
	"go.temporal.io/sdk/workflow"
	"money-transfer-worker/activities"
	"money-transfer-worker/app"
	"money-transfer-worker/messages"
	"time"
)

const (
	ADVANCED_VISIBILTY = "AccountTransferWorkflowAdvancedVisibility"
	NEEDS_APPROVAL     = "AccountTransferWorkflowHumanInLoop"
	API_DOWNTIME       = "AccountTransferWorkflowAPIDowntime"
	BUG                = "AccountTransferWorkflowRecoverableFailure"
	INVALID_ACCOUNT    = "AccountTransferWorkflowInvalidAccount"
)

const (
	APPROVAL_TIME = 30
)

var transferStatusKey = temporal.NewSearchAttributeKeyKeyword("Step")

func MoneyTransferWorkflowScenarios(ctx workflow.Context, input app.TransferInput) (output *app.TransferOutput, err error) {
	name := workflow.GetInfo(ctx).WorkflowType.Name
	logger := workflow.GetLogger(ctx)
	logger.Info("Processing workflow started", "fromAccount", input.FromAccount, "toAccount", input.ToAccount, "Amount", input.Amount)

	// expose progress as a query
	workflowState, err := messages.SetQueryHandlerForStatus(ctx)
	if err != nil {
		return nil, err
	}

	updateProgress(ctx, workflowState, "starting", 3, 25)

	updateProgress(ctx, workflowState, "running", 2, 50)

	if NEEDS_APPROVAL == name {
		logger.Info("Waiting on 'approveTransfer' Signal or Update for workflow ID: ", workflow.GetInfo(ctx).WorkflowExecution.ID)
		updateProgress(ctx, workflowState, "waiting", 2, 55)
		c := messages.GetSignalChannelForApproval(ctx)
		ok, _ := c.ReceiveWithTimeout(ctx, time.Second*APPROVAL_TIME, nil)
		if !ok {
			return nil, fmt.Errorf("approval not received within %d seconds", APPROVAL_TIME)
		}
	}

	updateProgress(ctx, workflowState, "running", 2, 60)

	activityOptions := workflow.ActivityOptions{
		StartToCloseTimeout: 5 * time.Second,
	}

	ctx = workflow.WithActivityOptions(ctx, activityOptions)

	logger.Info("Withdrawing money..")
	withdrawResult := ""
	simulateDelay := API_DOWNTIME == name
	err = workflow.ExecuteActivity(ctx, activities.Withdraw, input.Amount, simulateDelay).Get(ctx, &withdrawResult)
	if err != nil {
		logger.Info("Error running withdraw", err)
		return nil, err
	}
	logger.Info("Withdraw returned ", withdrawResult)

	updateProgress(ctx, workflowState, "running", 1, 70)

	if BUG == name {
		// simulate a bug
		panic("Workflow bug!")
	}

	// pause for dramatic effect
	updateProgress(ctx, workflowState, "deposit", 2, 75)

	logger.Info("Calling side effect")

	result := workflow.SideEffect(ctx, func(ctx workflow.Context) interface{} {
		// generate a UUID as a side effect
		return uuid.New().String()
	})

	var idempotencyKey string
	result.Get(&idempotencyKey)

	logger.Info("Calling deposit", "idempotencyKey:", idempotencyKey)

	// do deposit
	depositResult := app.ChargeResponseObj{}
	invalidAccount := INVALID_ACCOUNT == name
	err = workflow.ExecuteActivity(ctx, activities.Deposit, idempotencyKey, input.Amount, invalidAccount).Get(ctx, &depositResult)
	if err != nil {
		logger.Info("Deposit failed unrecoverably. Reverting withdraw")

		var undoResult = false
		workflow.ExecuteActivity(ctx, activities.UndoWithdraw, input.Amount).Get(ctx, &undoResult)

		return nil, fmt.Errorf("deposit failed %w", err)
	}

	logger.Info("Deposit", "result", depositResult)

	updateProgress(ctx, workflowState, "deposited", 2, 80)

	updateProgress(ctx, workflowState, "finished", 2, 100)
	workflowState.ChargeResult = depositResult

	output = &app.TransferOutput{
		ChargeResponseObj: app.ChargeResponseObj{
			ChargeId: depositResult.ChargeId,
		},
	}

	logger.Info("workflow returning", "output", output)

	return output, nil
}

func updateProgress(ctx workflow.Context, workflowState *app.TransferState, transferStatus string, seconds int, value int) {
	sleep(ctx, seconds, &workflowState.ProgressPercentage, value)
	workflowState.TransferState = transferStatus
	if ADVANCED_VISIBILTY == workflow.GetInfo(ctx).WorkflowType.Name {
		workflow.UpsertTypedSearchAttributes(ctx, transferStatusKey.ValueSet(transferStatus))
	}
}
