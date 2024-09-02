package io.temporal.samples.moneytransfer;

import com.google.common.base.Splitter;
import com.google.protobuf.Timestamp;
import io.temporal.api.workflow.v1.WorkflowExecutionInfo;
import io.temporal.api.workflowservice.v1.ListWorkflowExecutionsRequest;
import io.temporal.api.workflowservice.v1.ListWorkflowExecutionsResponse;
import io.temporal.samples.moneytransfer.helper.ServerInfo;
import io.temporal.samples.moneytransfer.model.WorkflowStatus;
import io.temporal.serviceclient.WorkflowServiceStubs;

import javax.net.ssl.SSLException;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static io.temporal.samples.moneytransfer.TemporalClient.getWorkflowServiceStubs;

public class TransferLister {

    public static List<WorkflowStatus> listWorkflows() throws FileNotFoundException, SSLException {
        WorkflowServiceStubs service = getWorkflowServiceStubs();

        ListWorkflowExecutionsResponse responseOpen = service
                .blockingStub()
                .listWorkflowExecutions(
                        ListWorkflowExecutionsRequest.newBuilder()
                                .setNamespace(ServerInfo.getNamespace())
                                .setQuery(
                                        "ExecutionStatus = 'Running'" +
                                                "AND WorkflowType STARTS_WITH 'AccountTransferWorkflow'" +
                                                "AND StartTime BETWEEN '" +
                                                timeStampToString(getOneHourAgo()) +
                                                "'" +
                                                " AND '" +
                                                timeStampToString(getNow()) +
                                                "'"
                                )
                                .build()
                );

        ListWorkflowExecutionsResponse responseClosed = service
                .blockingStub()
                .listWorkflowExecutions(
                        ListWorkflowExecutionsRequest.newBuilder()
                                .setNamespace(ServerInfo.getNamespace())
                                .setQuery(
                                        "ExecutionStatus != 'Running'" +
                                                "AND WorkflowType STARTS_WITH 'AccountTransferWorkflow'" +
                                                "AND StartTime BETWEEN '" +
                                                timeStampToString(getOneHourAgo()) +
                                                "'" +
                                                " AND '" +
                                                timeStampToString(getNow()) +
                                                "'"
                                )
                                .build()
                );

        // array of WorkflowStatus
        List<WorkflowStatus> workflowStatusList = new ArrayList<>();

        for (WorkflowExecutionInfo workflowExecutionInfo : responseOpen.getExecutionsList()) {
            WorkflowStatus workflowStatusOpen = new WorkflowStatus();
            workflowStatusOpen.setWorkflowId(workflowExecutionInfo.getExecution().getWorkflowId());
            workflowStatusOpen.setWorkflowStatus(getWorkflowStatus(workflowExecutionInfo.getStatus().toString()));
            workflowStatusOpen.setUrl(getWorkflowUrl(workflowExecutionInfo.getExecution().getWorkflowId()));
            workflowStatusList.add(workflowStatusOpen);
        }

        for (WorkflowExecutionInfo workflowExecutionInfo : responseClosed.getExecutionsList()) {
            WorkflowStatus workflowStatusClosed = new WorkflowStatus();
            workflowStatusClosed.setWorkflowId(workflowExecutionInfo.getExecution().getWorkflowId());
            workflowStatusClosed.setWorkflowStatus(getWorkflowStatus(workflowExecutionInfo.getStatus().toString()));
            workflowStatusClosed.setUrl(getWorkflowUrl(workflowExecutionInfo.getExecution().getWorkflowId()));
            workflowStatusList.add(workflowStatusClosed);
        }

        return workflowStatusList;
    }

    // in the format the UI expects
    private static String getWorkflowStatus(String input) {
        if (input == null || input.isEmpty()) {
            return ""; // Return empty string if input is null or empty
        }

        List<String> parts = Splitter.on('_').splitToList(input);

        return parts.get(parts.size() - 1); // Return the last part
    }

    private static String timeStampToString(Timestamp aTime) {
        java.sql.Timestamp javaTimestamp = new java.sql.Timestamp(aTime.getSeconds() * 1000);
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(javaTimestamp);
    }

    private static Timestamp getNow() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
        Instant instant = now.atZone(ZoneId.of("UTC")).toInstant();
        Timestamp timestamp = Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();

        return timestamp;
    }

    private static Timestamp getOneHourAgo() {
        // Get current date-time in UTC
        LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));

        // Subtract one hour
        LocalDateTime oneHourAgo = now.minusHours(1);

        // Convert LocalDateTime to Instant
        Instant instant = oneHourAgo.atZone(ZoneId.of("UTC")).toInstant();

        // Convert Instant to Timestamp
        Timestamp timestamp = Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();

        return timestamp;
    }

    private static String getWorkflowUrl(String workflowId) {
        String url = "";
        if (ServerInfo.getAddress().endsWith(".tmprl.cloud:7233")) {
            url = "https://cloud.temporal.io/namespaces/" + ServerInfo.getNamespace() + "/workflows/" + workflowId;
        }
        return url;
    }

    public static void main(String[] args) throws FileNotFoundException, SSLException {
        List<WorkflowStatus> workflowStatusList = listWorkflows();
        for (WorkflowStatus workflowStatus : workflowStatusList) {
            System.out.println(
                    workflowStatus.getWorkflowId() +
                            " " +
                            workflowStatus.getWorkflowStatus() +
                            " " +
                            workflowStatus.getUrl()
            );
        }
    }
}
