package activities

import (
	"context"
	"go.temporal.io/sdk/activity"
)

func UndoWithdraw(ctx context.Context, amountDollars float32) (bool, error) {
	logger := activity.GetLogger(ctx)
	logger.Info("API /undoWithdraw", "amount = ", amountDollars)

	return true, nil
}
