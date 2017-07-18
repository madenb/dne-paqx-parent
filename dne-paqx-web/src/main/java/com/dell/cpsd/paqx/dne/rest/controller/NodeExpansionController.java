/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 */
package com.dell.cpsd.paqx.dne.rest.controller;

import com.dell.cpsd.paqx.dne.domain.Job;
import com.dell.cpsd.paqx.dne.rest.exception.WorkflowNotFoundException;
import com.dell.cpsd.paqx.dne.rest.model.AboutInfo;
import com.dell.cpsd.paqx.dne.rest.model.ClusterInfo;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.WorkflowService;
import com.dell.cpsd.paqx.dne.service.addNode.IAddNodeService;
import com.dell.cpsd.paqx.dne.service.model.*;
import com.dell.cpsd.paqx.dne.service.orchestration.IOrchestrationService;
import com.dell.cpsd.paqx.dne.service.preProcess.IPreProcessService;
import com.dell.cpsd.service.common.client.exception.ServiceExecutionException;
import com.dell.cpsd.service.common.client.exception.ServiceTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 *
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 *
 * Created by fuvce1 on 3/14/2017.
 */
@EnableAsync
@RestController
@RequestMapping("/dne")
public class NodeExpansionController
{
    /*
     * The logger for this class
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeExpansionController.class);

    
    /*
     * The service controlling the workflow.
     */
    @Autowired
    IPreProcessService preProcessService;

    @Autowired
    IAddNodeService addNodeService;

    @Autowired
    IOrchestrationService orchestrationService;

    public NodeService getNodeService() {
        return nodeService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    @Autowired
    private NodeService nodeService;
    /**
     * Send request to get about info for node expansion PAQX.
     * @return Returns about information for node expansion PAQX.
     */
    @CrossOrigin
    @RequestMapping(path="/about", method = RequestMethod.GET, produces = "application/json")
    public AboutInfo about(HttpServletResponse httpServletResponse) {
        return new AboutInfo("Node Expansion API v0.1");
    }

    /**
     * List discovered nodes
     *
     * @param servletRequest
     * @return
     * @throws ServiceTimeoutException
     * @throws ServiceExecutionException
     */
    @CrossOrigin
    @RequestMapping(path = "/nodes", method = RequestMethod.GET, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public List<NodeInfo> listDiscoveredNodes(HttpServletRequest servletRequest) throws ServiceTimeoutException, ServiceExecutionException 
    {
        List<DiscoveredNode> discoveredNodes = nodeService.listDiscoveredNodes();
        if (discoveredNodes != null)
        {
            return discoveredNodes.stream().map(n -> new NodeInfo(n.getConvergedUuid(), n.getNodeId(), NodeStatus.valueOf(n.getNodeStatus().toString()))).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    @CrossOrigin
    @RequestMapping(path = "/preprocess", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public ResponseEntity<NodeExpansionResponse> preProcessOrchestration(@RequestBody NodeExpansionRequest params,
                                                                         HttpServletRequest servletRequest) throws InterruptedException, ExecutionException
    {
        Job job = preProcessService.createWorkflow("preProcessWorkflow", "startPreProcessWorkflow",
                "PreProcess request has been Submitted successfully");
        job.setInputParams(params);
        orchestrationService.orchestrateWorkflow(job, preProcessService );

        return new ResponseEntity<>(preProcessService.makeNodeExpansionResponse(job), HttpStatus.OK);
    }

    @CrossOrigin
    @RequestMapping(path = "/preprocess/step/{stepName}", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public ResponseEntity<NodeExpansionResponse> preProcessStep(@PathVariable String stepName,
                                                                        @RequestBody NodeExpansionRequest params,
                                                                        HttpServletRequest servletRequest) throws InterruptedException, ExecutionException
    {
        Job job = preProcessService.createWorkflow("preprocess", stepName,
                "Preprocess request has been Submitted successfully");

        orchestrationService.orchestrateWorkflow(job, preProcessService);

        return new ResponseEntity<>(preProcessService.makeNodeExpansionResponse(job), HttpStatus.OK);
    }

    /**
     * This starts the add node process.
     * 
     * @param   params          The request parameters
     * @param   servletRequest  The servlet request.
     * 
     * @return   The node expansion response entity.
     * 
     * @since   1.0
     */
    @CrossOrigin
    @RequestMapping(path = "/nodes", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public ResponseEntity<NodeExpansionResponse> addNode(@RequestBody NodeExpansionRequest params, 
            HttpServletRequest servletRequest) throws InterruptedException, ExecutionException 
    {
        Job job = addNodeService.createWorkflow("addNode", "startAddNodeWorkflow",
                "AddNode request has been Submitted successfully");
        job.setInputParams(params);
        orchestrationService.orchestrateWorkflow(job, addNodeService );

        return new ResponseEntity<>(addNodeService.makeNodeExpansionResponse(job), HttpStatus.OK);
    }


    @CrossOrigin
    @RequestMapping(path = "/nodes/step/{stepName}", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public ResponseEntity<NodeExpansionResponse> dontKnowWhatToCallThis(@PathVariable String stepName,
                                                                        @RequestBody NodeExpansionRequest params,
                                                                        HttpServletRequest servletRequest) throws InterruptedException, ExecutionException
    {
        Job job = addNodeService.createWorkflow("addNode", stepName,
                "AddNode request has been Submitted successfully");

        orchestrationService.orchestrateWorkflow(job, addNodeService);

        return new ResponseEntity<>(addNodeService.makeNodeExpansionResponse(job), HttpStatus.OK);
    }

    /**
     * This returns the workflow job information for the specified job identifier.
     * 
     * @param   jobId           The job identifier.
     * @param   servletRequest  The servlet request.
     * 
     * @return  The node expansion response entity.
     * 
     * @since   1.0
     */
    @CrossOrigin
    @RequestMapping(path = "/nodes/{jobId}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<NodeExpansionResponse> getAddNodeJob(
            final @PathVariable UUID jobId, final HttpServletRequest servletRequest)
        throws WorkflowNotFoundException
    {
        final Job job = addNodeService.findJob(jobId);

        return new ResponseEntity<>(addNodeService.makeNodeExpansionResponse(job), HttpStatus.OK);
    }

    @CrossOrigin
    @RequestMapping(path = "/preprocess/{jobId}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<NodeExpansionResponse> getPreProcessJob(
            final @PathVariable UUID jobId, final HttpServletRequest servletRequest)
            throws WorkflowNotFoundException
    {
        final Job job = preProcessService.findJob(jobId);

        return new ResponseEntity<>(preProcessService.makeNodeExpansionResponse(job), HttpStatus.OK);
    }

    public void setWorkflowService(WorkflowService workflowService){
        addNodeService.setWorkflowService(workflowService);
    }

    @RequestMapping(path = "/clusters", method = RequestMethod.GET, produces = "application/json")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public List<ClusterInfo> listVirtualizationClusters(HttpServletRequest servletRequest) throws ServiceTimeoutException, ServiceExecutionException {

        List<VirtualizationCluster> clusters = nodeService.listClusters();
        if (clusters != null )
        {
            LOGGER.info("Return " + clusters.size() + " clusters information.");
            return clusters.stream().map(c -> new ClusterInfo(c.getName(), c.getNumberOfHosts()))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}
