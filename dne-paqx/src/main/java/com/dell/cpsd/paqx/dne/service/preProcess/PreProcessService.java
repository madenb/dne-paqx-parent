/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.preProcess;

import com.dell.cpsd.paqx.dne.domain.Job;
import com.dell.cpsd.paqx.dne.domain.WorkflowTask;
import com.dell.cpsd.paqx.dne.service.BaseService;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.WorkflowService;
import com.dell.cpsd.paqx.dne.service.model.NodeExpansionResponse;
import com.dell.cpsd.paqx.dne.service.task.handler.preprocess.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class PreProcessService extends BaseService implements IPreProcessService {

    @Value("#{PropertySplitter.map('${preprocess.map.step.to.map}')}")
    private Map<String, String> propertyAsMap;

    @Autowired
    @Qualifier("preProcessWorkflowService")
    private WorkflowService workflowService;

    @Autowired
    private NodeService nodeService;

    @Bean("findVClusterTask")
    public WorkflowTask createVClusterTask(){
        return createTask("Find VCluster", new FindVClusterTaskHandler(nodeService));
    }

    @Bean("findProtectionDomainTask")
    public WorkflowTask findProtectionDomainTask(){
        return createTask("Find ProtectionDomain", new FindProtectionDomainTaskHandler(workflowService));
    }

    @Bean("findSystemDataTask")
    public WorkflowTask findSystemDataTask(){
        return createTask("Find SystemData", new FindSystemDataTaskHandler(workflowService));
    }

    @Bean("assignDefaultHostNameTask")
    public WorkflowTask assignDefaultHostNameTask(){
        return createTask("Assign Default HostName", new AssignDefaultHostNameTaskHandler(workflowService));
    }

    @Bean("assignDefaultCredentialsTask")
    public WorkflowTask assignDefaultCredentialsTask(){
        return createTask("Assign Default Credentials", new AssignDefaultCredentialsTaskHandler(workflowService));
    }

    @Bean("preProcessWorkflowTasks")
    public Map<String, WorkflowTask> preProcessWorkflowTasks(){
        final Map<String, WorkflowTask> workflowTasks = new HashMap<>();

        workflowTasks.put("findVCluster", createVClusterTask());
        workflowTasks.put("findProtectionDomain", findProtectionDomainTask());
        workflowTasks.put("findSystemData", findSystemDataTask());
        workflowTasks.put("assignDefaultHostName", assignDefaultHostNameTask());
        workflowTasks.put("assignDefaultCredentials", assignDefaultCredentialsTask());
        return workflowTasks;
    }

    @Override
    public Job createWorkflow(final String workflowType, final String startingStep,
                              final String currentStatus){

        Job job = workflowService.createWorkflow(workflowType, startingStep, currentStatus, preProcessWorkflowTasks());
        return job;
    }


    ///////////////////////////////////////////////
    public Job findJob(UUID jobId){
        final Job job = workflowService.findJob(jobId);

        return job;
    }

    public  NodeExpansionResponse makeNodeExpansionResponse(final Job job){
        return makeNodeExpansionResponse(job, workflowService);
    }

    ////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////
    //

    public String findPathFromStep(final String step)
    {
        return propertyAsMap.get(step);
    }

    public WorkflowService getWorkflowService() {
        return workflowService;
    }
    public void setWorkflowService (WorkflowService workflowService){
        this.workflowService = workflowService;
    }
}
