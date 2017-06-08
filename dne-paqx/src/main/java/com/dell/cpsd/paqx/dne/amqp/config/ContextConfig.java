/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.amqp.config;

import com.dell.cpsd.service.common.client.context.ConsumerContextConfig;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ContextConfig extends ConsumerContextConfig {
    private static final String CONSUMER_NAME = "dne-paqx";

    /**
     * ContextConfig constructor.
     *
     * @since 1.0
     */
    public ContextConfig() {
        super(CONSUMER_NAME, false);
    }

}
