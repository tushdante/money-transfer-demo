package activities

import (
	"context"
	"go.temporal.io/sdk/activity"
	"time"
)

func Withdraw(ctx context.Context, amountDollars float32, simulateDelay bool) (string, error) {
	logger := activity.GetLogger(ctx)
	logger.Info("API /withdraw", "amount = ", amountDollars)

	info := activity.GetInfo(ctx)
	if simulateDelay {
		logger.Info("*** Simulating API downtime")
		if info.Attempt < 5 {
			logger.Info("Activity", "attempt # ", info.Attempt)
			delaySeconds := 7
			logger.Info("simulateDelay", "Seconds", delaySeconds)
			time.Sleep(time.Duration(delaySeconds) * time.Second)
		}
	}

	logger.Info("Returning success fro Withdraw")

	return "SUCCESS", nil
}
