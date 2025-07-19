# frozen_string_literal: true

require 'spec_helper'
require_relative '../../models/transfer_input'

RSpec.describe Models::TransferInput do
  describe '.from_h' do
    it 'creates a TransferInput from a hash with camelCase keys' do
      hash = {
        'amount' => 100.50,
        'fromAccount' => '123-456',
        'toAccount' => '789-012'
      }

      result = described_class.from_h(hash)

      expect(result).to have_attributes(
        amount: 100.50,
        from_account: '123-456',
        to_account: '789-012'
      )
    end

    it 'creates a TransferInput from a hash with snake_case keys' do
      hash = {
        'amount' => 200.75,
        'from_account' => '111-222',
        'to_account' => '333-444'
      }

      result = described_class.from_h(hash)

      expect(result).to have_attributes(
        amount: 200.75,
        from_account: '111-222',
        to_account: '333-444'
      )
    end
  end
end
