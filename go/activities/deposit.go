package activities

import (
	"context"
	"go.temporal.io/sdk/activity"
	"go.temporal.io/sdk/temporal"
	"money-transfer-worker/app"
)

func Deposit(ctx context.Context, idempotencyKey string, amountDollars float32, invalidAccount bool) (app.ChargeResponseObj, error) {
	logger := activity.GetLogger(ctx)
	logger.Info("API /deposit", "amount = ", amountDollars)

	if invalidAccount {
		return app.ChargeResponseObj{},
			temporal.NewNonRetryableApplicationError("invalid account", "invalid_account", nil)
	}

	response := app.ChargeResponseObj{
		ChargeId: "example-charge-id",
	}

	return response, nil
}
