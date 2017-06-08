/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 */
package com.dell.cpsd.paqx.dne.service.amqp.adapter;

import com.dell.converged.capabilities.compute.discovered.nodes.api.NodesListed;
import com.dell.cpsd.service.common.client.callback.IServiceCallback;
import com.dell.cpsd.service.common.client.callback.ServiceResponse;
import com.dell.cpsd.service.common.client.rpc.ServiceCallbackAdapter;
import com.dell.cpsd.service.common.client.rpc.ServiceCallbackRegistry;

public class NodesListedResponseAdapter implements ServiceCallbackAdapter<NodesListed, ServiceResponse<NodesListed>>
{
    private ServiceCallbackRegistry serviceCallbackRegistry;

    public NodesListedResponseAdapter(ServiceCallbackRegistry serviceCallbackRegistry)
    {
        this.serviceCallbackRegistry = serviceCallbackRegistry;
    }

    @Override
    public ServiceResponse<NodesListed> transform(NodesListed nodesListed)
    {
        return new ServiceResponse<>(nodesListed.getMessageProperties().getCorrelationId(), nodesListed, null);
    }

    @Override
    public void consume(IServiceCallback callback, ServiceResponse<NodesListed> nodesListedServiceResponse)
    {
        callback.handleServiceResponse(nodesListedServiceResponse);
    }

    @Override
    public IServiceCallback take(NodesListed nodesListed)
    {
        return serviceCallbackRegistry.removeServiceCallback(nodesListed.getMessageProperties().getCorrelationId());
    }

    @Override
    public Class<NodesListed> getSourceClass()
    {
        return NodesListed.class;
    }
}
