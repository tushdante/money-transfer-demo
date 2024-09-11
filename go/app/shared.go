package app

type TransferInput struct {
	Amount      int    `json:"amount"`
	FromAccount string `json:"fromAccount"`
	ToAccount   string `json:"toAccount"`
}

type DepositResponse struct {
	DepositId string `json:"chargeId"`
}

type TransferOutput struct {
	DepositResponse DepositResponse `json:"depositResponse"`
}

type TransferStatus struct {
	ProgressPercentage int             `json:"progressPercentage"`
	TransferState      string          `json:"transferState"`
	WorkflowStatus     string          `json:"workflowStatus"`
	DepositResponse    DepositResponse `json:"chargeResult"`
	ApprovalTime       int             `json:"approvalTime"`
}
