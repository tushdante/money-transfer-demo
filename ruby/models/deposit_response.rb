# frozen_string_literal: true
require 'active_support/inflector'

module Models
  class DepositResponse
    attr_accessor :charge_id

    def initialize(charge_id:)
      @charge_id = charge_id
    end

    def to_h
      { charge_id: charge_id }.transform_keys { |key| key.to_s.camelize(:lower) }
    end

    def self.from_h(hash)
      hash = hash.transform_keys { |key| key.underscore.to_sym }
      new(charge_id: hash[:charge_id])
    end
  end
end
