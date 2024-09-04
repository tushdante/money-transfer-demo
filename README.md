# Money Transfer 
Demonstrates a simple Money Transfer in Temporal using different languages. 

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

Now start a worker. You can choose to use the Java or Go Worker below

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
go run worker/main.go
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
export TEMPORAL_ADDRESS=<namespace>.<accountID>.tmprl.cloud:7233
export TEMPORAL_NAMESPACE=<namespace>.<accountID>
export TEMPORAL_CERT_PATH="/path/to/cert.pem"
export TEMPORAL_KEY_PATH="/path/to/key.key"
```

### Start the UX 
Next, start the UX which is written using the Java SDK

```bash
cd ui
./startcloudwebui.sh
```

### Start the Worker - Java
In a new terminal, start the Java Worker
```bash
cd java
./startcloudworker.sh
```

### Start the Worker - Golang
In a new terminal, start the Golang Worker

```bash
cd go
go run worker/main.go
```