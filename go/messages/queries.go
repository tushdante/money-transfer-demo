package messages

import (
	"go.temporal.io/sdk/workflow"
	"money-transfer-worker/app"
)

func SetQueryHandlerForStatus(ctx workflow.Context) (*app.TransferState, error) {
	logger := workflow.GetLogger(ctx)

	// Initialize our state object
	var state = app.TransferState{}
	state.ProgressPercentage = 0
	state.TransferState = "starting"
	state.WorkflowStatus = ""
	state.ApprovalTime = 30
	state.ChargeResult = app.ChargeResponseObj{}

	err := workflow.SetQueryHandler(ctx, "transferStatus", func() (app.TransferState, error) {
		return state, nil
	})

	if err != nil {
		logger.Error("SetQueryHandler failed for status: " + err.Error())
		return nil, err
	}

	return &state, nil
}
