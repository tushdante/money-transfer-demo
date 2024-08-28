package main

import (
	"crypto/tls"
	"go.temporal.io/sdk/client"
	tlog "go.temporal.io/sdk/log"
	"go.temporal.io/sdk/worker"
	"go.temporal.io/sdk/workflow"
	"log"
	"log/slog"
	"money-transfer-worker/activities"
	"money-transfer-worker/workflows"
	"os"
)

func main() {
	c, err := client.Dial(getClientOptions())
	if err != nil {
		log.Fatalln("Unable to create client", err)
	}
	defer c.Close()

	w := worker.New(c, getEnv("TEMPORAL_MONEYTRANSFER_TASKQUEUE", "MoneyTransfer"), worker.Options{})

	// workflows
	w.RegisterWorkflowWithOptions(workflows.MoneyTransferWorkflow, workflow.RegisterOptions{
		Name: "AccountTransferWorkflow",
	})

	// activities
	w.RegisterActivity(activities.Deposit)
	w.RegisterActivity(activities.Withdraw)
	w.RegisterActivity(activities.UndoWithdraw)

	/*
		w.RegisterWorkflowWithOptions(workflows.OrderWorkflow, workflow.RegisterOptions{
			Name: "OrderWorkflowHappyPath",
		})
		w.RegisterWorkflowWithOptions(workflows.OrderWorkflowScenarios, workflow.RegisterOptions{
			Name: "OrderWorkflowAPIFailure",
		})
		w.RegisterWorkflowWithOptions(workflows.OrderWorkflowScenarios, workflow.RegisterOptions{
			Name: "OrderWorkflowRecoverableFailure",
		})
		w.RegisterWorkflowWithOptions(workflows.OrderWorkflowScenarios, workflow.RegisterOptions{
			Name: "OrderWorkflowNonRecoverableFailure",
		})
		w.RegisterWorkflowWithOptions(workflows.OrderWorkflowScenarios, workflow.RegisterOptions{
			Name: "OrderWorkflowChildWorkflow",
		})
		w.RegisterWorkflow(workflows.ShippingChildWorkflow)
		w.RegisterWorkflowWithOptions(workflows.OrderWorkflowScenarios, workflow.RegisterOptions{
			Name: "OrderWorkflowAdvancedVisibility",
		})
		w.RegisterWorkflowWithOptions(workflows.OrderWorkflowScenarios, workflow.RegisterOptions{
			Name: "OrderWorkflowHumanInLoopSignal",
		})
		w.RegisterWorkflowWithOptions(workflows.OrderWorkflowScenarios, workflow.RegisterOptions{
			Name: "OrderWorkflowHumanInLoopUpdate",
		})

		// activities
		w.RegisterActivity(activities.GetItems)
		w.RegisterActivity(activities.CheckFraud)
		w.RegisterActivity(activities.PrepareShipment)
		w.RegisterActivity(activities.UndoPrepareShipment)
		w.RegisterActivity(activities.ChargeCustomer)
		w.RegisterActivity(activities.UndoChargeCustomer)
		w.RegisterActivity(activities.ShipOrder)
	*/

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

	return clientOptions
}

func getEnv(key, fallback string) string {
	if value, ok := os.LookupEnv(key); ok {
		return value
	}
	return fallback
}
