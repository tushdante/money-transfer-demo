import asyncio
import logging
import os

from temporalio.client import Client, TLSConfig
from temporalio.worker import Worker

from account_transfer_workflow import AccountTransferWorkflow
from account_transfer_workflow_scenarios import AccountTransferWorkflowScenarios
from activities import AccountTransferActivities

async def main():
    logging.basicConfig(level=logging.INFO,
                        format="%(asctime)s | %(levelname)s | %(filename)s:%(lineno)s | %(message)s")
    
    address = os.getenv("TEMPORAL_ADDRESS","127.0.0.1:7233")
    namespace = os.getenv("TEMPORAL_NAMESPACE","default")
    tlsCertPath = os.getenv("TEMPORAL_CERT_PATH","")
    tlsKeyPath = os.getenv("TEMPORAL_KEY_PATH","")
    tls = None

    if tlsCertPath and tlsKeyPath:
        with open(tlsCertPath,"rb") as f:
            cert = f.read()
        with open(tlsKeyPath,"rb") as f:
            key = f.read()
                    
        tls = TLSConfig(client_cert=cert,
                        client_private_key=key)

    client = await Client.connect(
        target_host=address, 
        namespace=namespace,
        tls=tls
    )

    activities = AccountTransferActivities()

    worker = Worker(
        client,
        task_queue="MoneyTransfer",   
        workflows=[AccountTransferWorkflow,
                   AccountTransferWorkflowScenarios],
        activities=[activities.validate, 
                    activities.withdraw, 
                    activities.deposit, 
                    activities.sendNotification,
                    activities.undoWithdraw],
    )

    await worker.run()

if __name__ == "__main__":
    asyncio.run(main())