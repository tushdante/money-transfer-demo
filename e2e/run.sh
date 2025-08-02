#!/bin/bash

echo "Setting up and running e2e tests..."

# Install npm dependencies
npm install

# Install Playwright browsers
npx playwright install

# Run tests
headed=false
verbose=false
only=""

while [[ $# -gt 0 ]]; do
  case $1 in
    --headed)
      headed=true
      shift
      ;;
    --verbose)
      verbose=true
      shift
      ;;
    --only)
      only="$2"
      shift 2
      ;;
    *)
      shift
      ;;
  esac
done

if [ "$verbose" = true ]; then
  export VERBOSE=true
fi

if [ -n "$only" ]; then
  export ONLY_WORKER="$only"
fi

if [ "$headed" = true ]; then
  npm run test:headed
else
  npm test
fi
