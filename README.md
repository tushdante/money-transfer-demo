# Money Transfer 
Demonstrates a simple Money Transfer in Temporal implemented in different languages. 

The workflow is designed so that the "Happy Path" is one workflow and all other scenarios are implemented using a Dyanmic Workflow.

Scenarios currently implemented include
* Happy Path                - everything works as intended
* Advanced Visibility       - updates a Search Attribute (Step) as it progesses through the workflow
* Require Human in the Loop - Shows how to use a signal with timeouts if not approved in time
* API Downtime              - Demonstrates an unreliable API (recovers after the 5th attempt)
* Bug in Workflow           - Purposefully throws/raises an error (fix and redeploy the worker)
* Invalid Account           - How to exit a workflow for business purposes (fail the workflow)

## Running the Demo locally
Start Temporal Locally

```bash
temporal server start-dev
```

Create a Search Attribute 
```bash
temporal operator search-attribute create --name Step --type Keyword
```

### Start the UX 
Next, start the UX which is written using the Java SDK

```bash
cd ui
./startlocalwebui.sh
```

Navigate to http://localhost:7000 in a web browser to interact with the UX

### Start a worker

Now start a worker. You can choose to use the Java, Go or .NET Worker below

#### Java Worker
In a new terminal, start the Java Worker 
```bash
cd java
./startlocalworker.sh
```

#### Golang Worker
In a new terminal, start the Golang Worker

```bash
cd go
./startlocalworker.sh
```

#### .NET Worker
In a new terminal, start the .NET Worker

```bash
cd dotnet
./startlocalworker.sh
```

#### Python Worker
In a new terminal, start the Python Worker

```bash
cd python
# If you haven't done this yet, install the dependencies
poetry install
./startlocalworker.sh
```

#### TypeScript Worker
In a new terminal, start the TypeScript Worker

```bash
cd typescript
# If you haven't done this yet, install the dependencies
npm install
./startlocalworker.sh
```

## Running the Demo on Temporal Cloud
Set up a search attribute in your namespace using the following command

```bash
tcld login
tcld namespace search-attributes add --namespace <namespace>.<accountId> --search-attribute "Step=Keyword"
```

Copy the setcloundenv.example to setcloudenv.sh 
```bash
cp setcloudenv.example setcloudenv.sh
```

Edit setcloudenv.sh to match your Temporal Cloud account:
```bash
# either using mTLS
export TEMPORAL_ADDRESS=<namespace>.<accountID>.tmprl.cloud:7233
export TEMPORAL_NAMESPACE=<namespace>.<accountID>
export TEMPORAL_CERT_PATH="/path/to/cert.pem"
export TEMPORAL_KEY_PATH="/path/to/key.key"

# or API keys
export TEMPORAL_ADDRESS=<region>.<cloud_provider>.api.temporal.io:7233
export TEMPORAL_NAMESPACE=<namespace>.<accountID>
export TEMPORAL_API_KEY=<api_key>
```

### Start the UX 
Next, start the UX which is written using the Java SDK

```bash
cd ui
./startcloudwebui.sh
```

### Start a Worker

Now start a worker. You can choose to use the Java, Go or .NET Worker below

#### Java Worker
In a new terminal, start the Java Worker
```bash
cd java
./startcloudworker.sh
```

#### Golang Worker
In a new terminal, start the Golang Worker

```bash
cd go
./startcloudworker.sh
```

#### .NET Worker
In a new terminal, start the .NET Worker

```bash
cd dotnet
./startcloudworker.sh
```

#### Python Worker
In a new terminal, start the Python Worker

```bash
cd python
# If you haven't done this yet, install the dependencies
poetry install
./startcloudworker.sh
```

#### TypeScript Worker
In a new terminal, start the Python Worker

```bash
cd python
# If you haven't done this yet, install the dependencies
npm install
./startcloudworker.sh
```

## Using Encryption
This demo supports encyrption by setting the environment variable ENCRYPT_PAYLOADS to true. To make this easier, each of the start*.sh files will use the first parameter to set this variable. If you want to use encryption, be sure to set this for the UI and the worker that you choose. For example:

### Start the UX 
Next, start the UX which is written using the Java SDK

```bash
cd ui
./startcloudwebui.sh true
```

### Java Worker Example -- works for other SDKs as well
In a new terminal, start the Java Worker
```bash
cd java
./startcloudworker.sh true
```

To view the decrypted data in your browser, you can set the Codec Server to use [https://codec.tmprl-demo.cloud](https://codec.tmprl-demo.cloud). Be sure to enable the "Pass the user access token" slider. This will work as long as you do not change the encryption key. 
