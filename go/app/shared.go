package app

type TransferInput struct {
	Amount      int    `json:"amount"s`
	FromAccount string `json:"fromAccount""`
	ToAccount   string `json:"toAccount""`
}

type ChargeResponseObj struct {
	ChargeId string `json:"chargeId""`
}

type TransferOutput struct {
	ChargeResponseObj ChargeResponseObj `json:"chargeResponseObj""`
}

type TransferState struct {
	ApprovalTime       int               `json:"approvalTime""`
	ProgressPercentage int               `json:"progressPercentage""`
	TransferState      string            `json:"transferState""`
	WorkflowStatus     string            `json:"workflowStatus""`
	ChargeResult       ChargeResponseObj `json:"chargeResult""`
}
