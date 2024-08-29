package activities

import (
	"context"
	"errors"
	"go.temporal.io/sdk/activity"
	"money-transfer-worker/app"
)

func Deposit(ctx context.Context, idempotencyKey string, amountDollars float32, invalidAccount bool) (app.ChargeResponseObj, error) {
	logger := activity.GetLogger(ctx)
	logger.Info("API /deposit", "amount = ", amountDollars)

	if invalidAccount {
		return app.ChargeResponseObj{}, errors.New("invalid account")
	}

	response := app.ChargeResponseObj{
		ChargeId: "example-charge-id",
	}

	return response, nil
}
