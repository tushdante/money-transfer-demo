# frozen_string_literal: true

require 'active_support/inflector'
require 'active_support/core_ext/hash/keys'

module Models
  TransferInput = Struct.new(:amount, :from_account, :to_account) do
    def self.from_h(hash)
      symbolized = hash.deep_transform_keys { |key| key.to_s.underscore.to_sym }
      new(**symbolized.slice(:amount, :from_account, :to_account))
    end
  end
end
