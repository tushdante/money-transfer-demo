import asyncio
import dataclasses
import logging
import os

import temporalio.converter
from temporalio.client import Client, TLSConfig
from temporalio.converter import DataConverter
from temporalio.worker import Worker

from account_transfer_workflow import AccountTransferWorkflow
from account_transfer_workflow_scenarios import AccountTransferWorkflowScenarios
from activities import AccountTransferActivities

from codec import EncryptionCodec

async def main():
    logging.basicConfig(level=logging.INFO,
                        format="%(asctime)s | %(levelname)s | %(filename)s:%(lineno)s | %(message)s")
    
    address = os.getenv("TEMPORAL_ADDRESS","127.0.0.1:7233")
    namespace = os.getenv("TEMPORAL_NAMESPACE","default")
    tlsCertPath = os.getenv("TEMPORAL_CERT_PATH","")
    tlsKeyPath = os.getenv("TEMPORAL_KEY_PATH","")
    encryptPayloads = True if os.getenv("ENCRYPT_PAYLOADS", "").lower() == "true" else False
    tls = None

    if tlsCertPath and tlsKeyPath:
        with open(tlsCertPath,"rb") as f:
            cert = f.read()
        with open(tlsKeyPath,"rb") as f:
            key = f.read()
                    
        tls = TLSConfig(client_cert=cert,
                        client_private_key=key)

    dataConverter = DataConverter.default
    if encryptPayloads:
        print("Encrypting payloads")
        dataConverter = dataclasses.replace(temporalio.converter.default(), payload_codec=EncryptionCodec())


    client = await Client.connect(
        data_converter=dataConverter,
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
    print("Python money transfer worker starting...")
    await worker.run()

if __name__ == "__main__":
    asyncio.run(main())