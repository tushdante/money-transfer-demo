package main

import (
	"crypto/tls"
	"log"
	"log/slog"
	"money-transfer-worker/activities"
	"money-transfer-worker/encryption"
	"money-transfer-worker/workflows"
	"os"

	"go.temporal.io/sdk/client"
	"go.temporal.io/sdk/converter"
	tlog "go.temporal.io/sdk/log"
	"go.temporal.io/sdk/worker"
	"go.temporal.io/sdk/workflow"
)

func main() {
	c, err := client.Dial(getClientOptions())
	if err != nil {
		log.Fatalln("Unable to create client", err)
	}
	defer c.Close()

	w := worker.New(c, getEnv("TEMPORAL_MONEYTRANSFER_TASKQUEUE", "MoneyTransfer"), worker.Options{})

	// workflows
	w.RegisterWorkflowWithOptions(workflows.AccountTransferWorkflow, workflow.RegisterOptions{
		Name: "AccountTransferWorkflow",
	})
	w.RegisterWorkflowWithOptions(workflows.AccountTransferWorkflowScenarios, workflow.RegisterOptions{
		Name: "AccountTransferWorkflowAdvancedVisibility",
	})
	w.RegisterWorkflowWithOptions(workflows.AccountTransferWorkflowScenarios, workflow.RegisterOptions{
		Name: "AccountTransferWorkflowHumanInLoop",
	})
	w.RegisterWorkflowWithOptions(workflows.AccountTransferWorkflowScenarios, workflow.RegisterOptions{
		Name: "AccountTransferWorkflowAPIDowntime",
	})
	w.RegisterWorkflowWithOptions(workflows.AccountTransferWorkflowScenarios, workflow.RegisterOptions{
		Name: "AccountTransferWorkflowRecoverableFailure",
	})
	w.RegisterWorkflowWithOptions(workflows.AccountTransferWorkflowScenarios, workflow.RegisterOptions{
		Name: "AccountTransferWorkflowInvalidAccount",
	})

	// activities
	w.RegisterActivity(activities.Validate)
	w.RegisterActivity(activities.Deposit)
	w.RegisterActivity(activities.Withdraw)
	w.RegisterActivity(activities.UndoWithdraw)
	w.RegisterActivity(activities.SendNotification)

	err = w.Run(worker.InterruptCh())
	if err != nil {
		log.Fatalln("Unable to start worker", err)
	}
}

func getClientOptions() client.Options {
	logger := slog.New(slog.NewTextHandler(os.Stdout, &slog.HandlerOptions{
		Level: slog.LevelInfo,
	}))
	slog.SetDefault(logger)

	address := getEnv("TEMPORAL_ADDRESS", "localhost:7233")
	namespace := getEnv("TEMPORAL_NAMESPACE", "default")
	clientOptions := client.Options{
		HostPort:  address,
		Namespace: namespace,
		Logger:    tlog.NewStructuredLogger(logger),
	}

	tlsCertPath := getEnv("TEMPORAL_CERT_PATH", "")
	tlsKeyPath := getEnv("TEMPORAL_KEY_PATH", "")
	if tlsCertPath != "" && tlsKeyPath != "" {
		cert, err := tls.LoadX509KeyPair(tlsCertPath, tlsKeyPath)
		if err != nil {
			log.Fatalln("Unable to load cert and key pair", err)
		}
		clientOptions.ConnectionOptions = client.ConnectionOptions{
			TLS: &tls.Config{
				Certificates: []tls.Certificate{cert},
			},
		}
	}

	encryptPayloads := getEnv("ENCRYPT_PAYLOADS", "false")
	if encryptPayloads == "true" {
		clientOptions.DataConverter = encryption.NewEncryptionDataConverter(
			converter.GetDefaultDataConverter(),
			encryption.DataConverterOptions{KeyID: "test", Compress: false},
		)
	}

	return clientOptions
}

func getEnv(key, fallback string) string {
	if value, ok := os.LookupEnv(key); ok {
		return value
	}
	return fallback
}
