# frozen_string_literal: true

require 'active_support/inflector'
require_relative 'deposit_response'

module Models
  class TransferOutput
    attr_accessor :deposit_response

    def initialize(deposit_response:)
      @deposit_response = deposit_response
    end

    def to_h
      { deposit_response: deposit_response }.transform_keys { |key| key.to_s.camelize(:lower) }
    end

    def self.from_h(hash)
      hash = hash.transform_keys { |key| key.underscore.to_sym }
      new(deposit_response: hash.fetch(:deposit_response))
    end
  end
end
