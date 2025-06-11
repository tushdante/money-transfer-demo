# frozen_string_literal: true

require 'active_support/inflector'
require_relative 'deposit_response'

module Models
  class TransferStatus
    attr_accessor :progress_percentage, :transfer_state, :workflow_status, :charge_result, :approval_time

    def initialize(progress_percentage:, transfer_state:, workflow_status:, charge_result:, approval_time:)
      @progress_percentage = progress_percentage
      @transfer_state = transfer_state
      @workflow_status = workflow_status
      @charge_result = charge_result
      @approval_time = approval_time
    end

    def to_h
      {
        progress_percentage: progress_percentage,
        transfer_state: transfer_state,
        workflow_status: workflow_status,
        charge_result: charge_result.to_h,
        approval_time: approval_time
      }.transform_keys { |key| key.to_s.camelize(:lower) }
    end

    def self.from_h(hash)
      hash = hash.transform_keys { |key| key.underscore.to_sym }
      charge_result = DepositResponse.from_h(hash[:charge_result] || {})
      new(
        progress_percentage: hash[:progress_percentage] || 0,
        transfer_state: hash[:transfer_state] || '',
        workflow_status: hash[:workflow_status] || '',
        charge_result: charge_result,
        approval_time: hash[:approval_time] || 0
      )
    end
  end
end
