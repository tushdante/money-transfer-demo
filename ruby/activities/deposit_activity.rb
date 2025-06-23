# frozen_string_literal: true

require_relative 'base_activity'
require_relative '../models/deposit_response'

module Activities
  class DepositActivity < BaseActivity
    INVALID_ACCOUNT = 'AccountTransferWorkflowInvalidAccount'

    def execute(idempotency_key, amount)
      attempt = Temporalio::Activity::Context.current.info.attempt
      workflow_type = Temporalio::Activity::Context.current.info.workflow_type
      logger.info("Deposit activity started. Amount: #{amount}, workflow type: #{workflow_type}, attempt: #{attempt}")

      error = simulate_external_operation_with_error(1000, workflow_type, attempt)
      logger.info("Deposit activity complete. Type: #{workflow_type}, error: #{error}")

      raise ArgumentError, 'Deposit activity failed, account is invalid' if error == INVALID_ACCOUNT

      Models::DepositResponse.new(charge_id: 'example-transfer-id')
    end
  end
end
