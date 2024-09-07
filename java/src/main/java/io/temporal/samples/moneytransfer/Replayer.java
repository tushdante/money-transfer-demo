package io.temporal.samples.moneytransfer;

import io.temporal.samples.moneytransfer.workflows.AccountTransferWorkflowImpl;
import io.temporal.testing.WorkflowReplayer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Replayer {

    public static void main(String[] args) throws IOException {
        // get command line argument for string WorkflowId
        String historyFile = args[0];

        Path historyFilePath = Paths.get(historyFile);

        System.out.println("Reading from file: " + historyFilePath.toAbsolutePath());
        String historyFileString = new String(Files.readAllBytes(historyFilePath), StandardCharsets.UTF_8);

        System.out.println("History file string length: " + historyFileString.length());

        try {
            // read history file to string

            System.out.println("Replaying workflow: " + historyFile);

            WorkflowReplayer.replayWorkflowExecution(historyFileString, AccountTransferWorkflowImpl.class);

            System.out.println("Replay completed successfully");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Replay failed, check above output for io.temporal.worker.NonDeterministicException");
        }
    }
}
