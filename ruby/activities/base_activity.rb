# frozen_string_literal: true

require 'temporalio/activity'

module Activities
  class BaseActivity < Temporalio::Activity::Definition
    RETRY_POLICY = Temporalio::RetryPolicy.new(
      initial_interval: 1,
      backoff_coefficient: 1.2
    )

    private

    def simulate_external_operation(ms)
      sleep(ms / 1000.0)
    end

    def simulate_external_operation_with_error(ms, workflow_type, attempt)
      simulate_external_operation(ms / attempt)
      attempt < 5 ? workflow_type : 'NoError'
    end

    def logger
      # Do not memoize logger or other contextual objects per Activity attempt.
      # Stateful Activities (like those with DB clients) that are instantiated before injecting to the Workers
      # reuse the same Activity instance, hence memoization will leak state across attempts.
      Temporalio::Activity::Context.current.logger
    end
  end
end
