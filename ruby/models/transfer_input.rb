# frozen_string_literal: true
require 'active_support/inflector'

module Models
  class TransferInput
    attr_accessor :amount, :from_account, :to_account

    def initialize(amount:, from_account:, to_account:)
      @amount = amount
      @from_account = from_account
      @to_account = to_account
    end

    def to_h
      {
        amount: amount,
        from_account: from_account,
        to_account: to_account
      }.transform_keys { |key| key.to_s.camelize(:lower) }
    end

    def self.from_h(hash)
      hash = hash.transform_keys { |key| key.underscore.to_sym }
      new(**symbolized.slice(:amount, :from_account, :to_account))
    end
  end
end
