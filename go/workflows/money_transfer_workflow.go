package workflows

import (
	"fmt"
	"github.com/google/uuid"
	"go.temporal.io/sdk/workflow"
	"money-transfer-worker/activities"
	"money-transfer-worker/app"
	"money-transfer-worker/messages"
	"time"
)

func MoneyTransferWorkflow(ctx workflow.Context, input app.TransferInput) (output *app.TransferOutput, err error) {
	// name := workflow.GetInfo(ctx).WorkflowType.Name
	logger := workflow.GetLogger(ctx)
	logger.Info("Processing workflow started", "fromAccount", input.FromAccount, "toAccount", input.ToAccount, "Amount", input.Amount)

	// expose progress as a query
	workflowState, err := messages.SetQueryHandlerForStatus(ctx)
	if err != nil {
		return nil, err
	}

	sleep(ctx, 1, &workflowState.ProgressPercentage, 60)

	workflowState.TransferState = "running"

	activityOptions := workflow.ActivityOptions{
		StartToCloseTimeout: 5 * time.Second,
	}

	ctx = workflow.WithActivityOptions(ctx, activityOptions)

	// call Withdraw
	withdrawResult := ""

	logger.Info("Withdrawing money..")

	err = workflow.ExecuteActivity(ctx, activities.Withdraw, input.Amount, false).Get(ctx, &withdrawResult)
	if err != nil {
		logger.Info("Error running withdraw", err)
		return nil, err
	}

	logger.Info("Withdraw returned ", withdrawResult)

	// pause for dramatic effect
	sleep(ctx, 2, &workflowState.ProgressPercentage, 65)

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
	err = workflow.ExecuteActivity(ctx, activities.Deposit, idempotencyKey, input.Amount, false).Get(ctx, &depositResult)
	if err != nil {
		logger.Info("Deposit failed unrecoverably. Reverting withdraw")

		var undoResult = false
		workflow.ExecuteActivity(ctx, activities.UndoWithdraw, input.Amount).Get(ctx, &undoResult)

		return nil, fmt.Errorf("deposit failed %w", err)
	}

	logger.Info("Deposit ", "result", depositResult)

	workflowState.ProgressPercentage = 80

	sleep(ctx, 6, &workflowState.ProgressPercentage, 100)

	workflowState.TransferState = "finished"
	workflowState.ChargeResult = depositResult

	output = &app.TransferOutput{
		ChargeResponseObj: app.ChargeResponseObj{
			ChargeId: depositResult.ChargeId,
		},
	}

	logger.Info("workflow returning", "output", output)

	return output, nil
}

func sleep(ctx workflow.Context, seconds int, progress *int, value int) {
	if seconds > 0 {
		duration := time.Duration(seconds) * time.Second
		workflow.Sleep(ctx, duration)
	}
	*progress = value
}
