/*******************************************************************************
 * Copyright (c) 2016, 2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.hono.tests.http;

import java.util.function.Consumer;

import org.apache.qpid.proton.message.Message;
import org.eclipse.hono.client.MessageConsumer;
import org.eclipse.hono.util.EventConstants;
import org.junit.runner.RunWith;

import io.vertx.core.Future;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;


/**
 * Integration tests for uploading events to the HTTP adapter.
 *
 */
@RunWith(VertxUnitRunner.class)
public class EventHttpIT extends HttpTestBase {

    private static final String URI = String.format("/%s", EventConstants.EVENT_ENDPOINT);

    @Override
    protected String getEndpointUri() {
        return URI;
    }

    @Override
    protected Future<MessageConsumer> createConsumer(final String tenantId, final Consumer<Message> messageConsumer) {

        return helper.honoClient.createEventConsumer(tenantId, messageConsumer, remoteClose -> {});
    }

    @Override
    protected void assertAdditionalMessageProperties(final TestContext ctx, final Message msg) {
        // assert that events are marked as "durable"
        ctx.assertTrue(msg.isDurable());
    }
}
