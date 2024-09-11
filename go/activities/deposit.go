package activities

import (
	"context"
	"errors"
	"money-transfer-worker/app"

	"go.temporal.io/sdk/activity"
	"go.temporal.io/sdk/temporal"
)

const (
	INVALID_ACCOUNT = "AccountTransferWorkflowInvalidAccount"
)

func Deposit(ctx context.Context, idempotencyKey string, amount float32, name string) (app.DepositResponse, error) {
	logger := activity.GetLogger(ctx)
	logger.Info("Deposit activity started", "amount", amount)
	attempt := activity.GetInfo(ctx).Attempt

	// simulate external API call
	error := simulateExternalOperationWithError(1000, name, attempt)
	logger.Info("Deposit call complete", "name", name, "error", error)

	if INVALID_ACCOUNT == error {
		// a business error, which cannot be retried
		return app.DepositResponse{},
			temporal.NewNonRetryableApplicationError("deposity activity failed, account is invalid", "activityFailure", errors.New("account invalid"))
	}

	response := app.DepositResponse{
		DepositId: "example-transfer-id",
	}

	return response, nil
}
