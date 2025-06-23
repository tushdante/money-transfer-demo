# frozen_string_literal: true

require 'json/add/struct'
require_relative 'serialization'

module Models
  TransferInput = Struct.new(
    :amount,
    :from_account,
    :to_account
  )
end
