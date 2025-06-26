require 'spec_helper'
require_relative '../../models/serialization'

TestModel = Struct.new(:name, :amount, :nested_object) do
  include Models::Serialization
end

NestedModel = Struct.new(:id, :status, :more_nested_object) do
  include Models::Serialization
end

MoreNestedModel = Struct.new(:id, :status) do
  include Models::Serialization
end

RSpec.describe Models::Serialization do
  let(:more_nested) { MoreNestedModel.new(id: 456, status: 'inactive') }
  let(:nested) { NestedModel.new(id: 123, status: 'active', more_nested_object: more_nested) }
  let(:model) { TestModel.new(name: 'test', amount: 100, nested_object: nested) }

  describe '#deep_camelize_keys' do
    it 'camelizes keys recursively' do
      result = model.deep_camelize_keys
      expect(result).to include('name', 'amount', 'nestedObject')
      expect(result['nestedObject']).to include('id', 'status', 'moreNestedObject')
      expect(result['nestedObject']['moreNestedObject']).to include('id', 'status')
    end
  end
end
