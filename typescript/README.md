# Temporal Money Transfer - Typescript

This is an alternate implementation of the Temporal Money Transfer Demo backend
using the [Typescript SDK](https://typescript.temporal.io/)

All of the scenarios suppported in the Go backend, and outlined in the main README are implemented in this Typescript version. This version is also fully compatible with the Java UI. See the main README for instructions on how to run the UI, and the instructions below for running the Typescript backend.

## Run Worker

1. `npm install` to install dependencies.
1. `npm run start.watch` to start the Worker.

## (Optional) Run Worker in Productionize Build

1. `npm run build` to build out the worker and activites.
1. `NODE_ENV=production node lib/worker.js` to run the production Worker.

## (Optional) Using VSCode Debugger

1. Install the [VSCode Debugger](https://temporal.io/blog/temporal-for-vs-code)
1. `cd typescript/`
1. `Command + Shift + P` Select `Temporal: Open Panel`