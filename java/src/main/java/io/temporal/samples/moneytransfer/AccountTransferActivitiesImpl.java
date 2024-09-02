package io.temporal.samples.moneytransfer;

import io.temporal.activity.Activity;
import io.temporal.activity.ActivityExecutionContext;
import io.temporal.activity.ActivityInfo;
import io.temporal.samples.moneytransfer.helper.ServerInfo;
import io.temporal.samples.moneytransfer.model.ChargeResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class AccountTransferActivitiesImpl
        implements AccountTransferActivities {

    private static final Logger log = LoggerFactory.getLogger(
            AccountTransferActivitiesImpl.class
    );

    private static String simulateDelay(int seconds) {
        String url =
                ServerInfo.getWebServerURL() + "/simulateDelay?s=" + seconds;
        log.info("\n\n/API/simulateDelay URL: " + url + "\n");
        Request request = new Request.Builder().url(url).build();
        try (
                Response response = new OkHttpClient().newCall(request).execute()
        ) {
            return response.body().string();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String withdraw(float amountDollars, boolean simulateDelay) {
        log.info("\n\nAPI /withdraw amount = " + amountDollars + " \n");

        ActivityExecutionContext ctx = Activity.getExecutionContext();
        ActivityInfo info = ctx.getInfo();

        if (simulateDelay) {
            log.info("\n\n*** Simulating API Downtime\n");
            if (info.getAttempt() < 5) {
                log.info(
                        "\n*** Activity Attempt: #" + info.getAttempt() + "***\n"
                );
                int delaySeconds = 7;
                log.info(
                        "\n\n/API/simulateDelay Seconds" + delaySeconds + "\n"
                );
                simulateDelay(delaySeconds);
            }
        }

        return "SUCCESS";
    }

    @Override
    public ChargeResponse deposit(
            String idempotencyKey,
            float amountDollars,
            boolean invalidAccount
    ) {
        log.info("\n\nAPI /deposit amount = " + amountDollars + " \n");

        if (invalidAccount) {
            InvalidAccountException invalidAccountException =
                    new InvalidAccountException("Invalid Account");
            throw Activity.wrap(invalidAccountException);
        }

        ChargeResponse response = new ChargeResponse("example-charge-id");

        return response;
    }

    @Override
    public boolean undoWithdraw(float amountDollars) {
        log.info("\n\nAPI /undoWithdraw amount = " + amountDollars + " \n");

        return true;
    }

    // InvalidAccountException
    public static class InvalidAccountException extends RuntimeException {

        public InvalidAccountException(String message) {
            super(message);
        }
    }
}
