package io.temporal.samples.moneytransfer;

import io.temporal.activity.ActivityInterface;
import io.temporal.samples.moneytransfer.model.ChargeResponse;

@ActivityInterface
public interface AccountTransferActivities {
    String withdraw(float amountDollars, boolean simulateDelay);

    ChargeResponse deposit(
            String idempotencyKey,
            float amountDollars,
            boolean invalidAccount
    );

    boolean undoWithdraw(float amountDollars);
}
