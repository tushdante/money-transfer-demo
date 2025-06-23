require 'json'
require 'active_support/inflector'
require 'active_support/core_ext/hash/keys'

module Models
  module Serialization
    def self.included(base)
      base.class_eval do
        def deep_camelize_keys
          deep_to_h.deep_transform_keys { |key| key.to_s.camelize(:lower) }
        end

        def deep_to_h
          hash = {}
          members.each do |member|
            value = send(member)
            hash[member] = if value.respond_to?(:deep_to_h)
                             value.deep_to_h
                           elsif value.respond_to?(:to_h)
                             value.to_h
                           else
                             value
                           end
          end
          hash
        end
      end
    end
  end
end
