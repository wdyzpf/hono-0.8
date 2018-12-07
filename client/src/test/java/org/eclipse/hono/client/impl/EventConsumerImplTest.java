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

package org.eclipse.hono.client.impl;

import static org.mockito.Mockito.*;

import java.util.function.BiConsumer;

import io.vertx.ext.unit.junit.Timeout;
import org.apache.qpid.proton.amqp.messaging.Released;
import org.apache.qpid.proton.amqp.transport.Source;
import org.apache.qpid.proton.message.Message;
import org.eclipse.hono.config.ClientConfigProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.proton.ProtonConnection;
import io.vertx.proton.ProtonDelivery;
import io.vertx.proton.ProtonHelper;
import io.vertx.proton.ProtonMessageHandler;
import io.vertx.proton.ProtonQoS;
import io.vertx.proton.ProtonReceiver;


/**
 * Test cases verifying the behavior of {@link EventConsumerImpl}.
 *
 */
@RunWith(VertxUnitRunner.class)
public class EventConsumerImplTest {

    /**
     * Timeout each test after 5 secs.
     */
    @Rule
    public Timeout timeout = Timeout.seconds(5);

    private Vertx vertx;
    private Context context;

    /**
     * Initializes fixture.
     */
    @Before
    public void setUp() {
        vertx = mock(Vertx.class);
        context = HonoClientUnitTestHelper.mockContext(vertx);
    }

    /**
     * Cleans up fixture.
     */
    @After
    public void shutDown() {
        vertx.close();
    }

    /**
     * Verifies that the message delivery for a received event is forwarded to the
     * registered event consumer.
     * 
     * @param ctx The test context.
     */
    @SuppressWarnings({ "unchecked" })
    @Test
    public void testCreateRegistersBiConsumerAsMessageHandler(final TestContext ctx) {

        // GIVEN an event consumer that releases all messages
        final BiConsumer<ProtonDelivery, Message> eventConsumer = (delivery, message) -> {
            ProtonHelper.released(delivery, true);
        };
        final Source source = mock(Source.class);
        when(source.getAddress()).thenReturn("event/tenant");
        final ProtonReceiver receiver = mock(ProtonReceiver.class);
        when(receiver.getSource()).thenReturn(source);
        when(receiver.getRemoteSource()).thenReturn(source);
        when(receiver.getRemoteQoS()).thenReturn(ProtonQoS.AT_LEAST_ONCE);

        final ProtonConnection con = mock(ProtonConnection.class);
        when(con.createReceiver(anyString())).thenReturn(receiver);

        final Async consumerCreation = ctx.async();
        EventConsumerImpl.create(
                context,
                new ClientConfigProperties(),
                con,
                "tenant",
                eventConsumer,
                ctx.asyncAssertSuccess(rec -> consumerCreation.complete()),
                remoteDetach -> {});

        final ArgumentCaptor<ProtonMessageHandler> messageHandler = ArgumentCaptor.forClass(ProtonMessageHandler.class);
        verify(receiver).handler(messageHandler.capture());
        // wait for peer's attach frame
        final ArgumentCaptor<Handler<AsyncResult<ProtonReceiver>>> openHandlerCaptor = ArgumentCaptor.forClass(Handler.class);
        verify(receiver).openHandler(openHandlerCaptor.capture());
        openHandlerCaptor.getValue().handle(Future.succeededFuture(receiver));
        consumerCreation.await();

        // WHEN an event is received
        final ProtonDelivery delivery = mock(ProtonDelivery.class);
        final Message msg = mock(Message.class);
        messageHandler.getValue().handle(delivery, msg);

        // THEN the message is released and settled
        verify(delivery).disposition(any(Released.class), eq(Boolean.TRUE));
    }

    /**
     * Verifies that the close handler on a consumer calls the registered close hook.
     *
     * @param ctx The test context.
     */
    @Test
    public void testCloseHandlerCallsCloseHook(final TestContext ctx) {
        testHandlerCallsCloseHook(ctx, (receiver, captor) -> verify(receiver).closeHandler(captor.capture()));
    }

    /**
     * Verifies that the detach handler on a consumer calls the registered close hook.
     *
     * @param ctx The test context.
     */
    @Test
    public void testDetachHandlerCallsCloseHook(final TestContext ctx) {
        testHandlerCallsCloseHook(ctx, (receiver, captor) -> verify(receiver).detachHandler(captor.capture()));
    }

    @SuppressWarnings({ "unchecked" })
    private void testHandlerCallsCloseHook(
            final TestContext ctx,
            final BiConsumer<ProtonReceiver, ArgumentCaptor<Handler<AsyncResult<ProtonReceiver>>>> handlerCaptor) {

        // GIVEN an open event consumer
        final BiConsumer<ProtonDelivery, Message> eventConsumer = mock(BiConsumer.class);
        final Source source = mock(Source.class);
        when(source.getAddress()).thenReturn("source/address");
        final ProtonReceiver receiver = mock(ProtonReceiver.class);
        when(receiver.isOpen()).thenReturn(Boolean.TRUE);
        when(receiver.getSource()).thenReturn(source);
        when(receiver.getRemoteSource()).thenReturn(source);

        final ProtonConnection con = mock(ProtonConnection.class);
        when(con.createReceiver(anyString())).thenReturn(receiver);

        final Handler<String> closeHook = mock(Handler.class);
        final ArgumentCaptor<Handler<AsyncResult<ProtonReceiver>>> captor = ArgumentCaptor.forClass(Handler.class);

        final Async consumerCreation = ctx.async();
        EventConsumerImpl.create(
                context,
                new ClientConfigProperties(),
                con,
                "source/address",
                eventConsumer,
                ctx.asyncAssertSuccess(rec -> consumerCreation.complete()),
                closeHook);

        // wait for peer's attach frame
        final ArgumentCaptor<Handler<AsyncResult<ProtonReceiver>>> openHandlerCaptor = ArgumentCaptor.forClass(Handler.class);
        verify(receiver).openHandler(openHandlerCaptor.capture());
        openHandlerCaptor.getValue().handle(Future.succeededFuture(receiver));
        consumerCreation.await();

        // WHEN the peer sends a detach frame
        handlerCaptor.accept(receiver, captor);
        captor.getValue().handle(Future.succeededFuture(receiver));

        // THEN the close hook is called
        verify(closeHook).handle(any());

        // and the receiver link is closed
        verify(receiver).close();
        verify(receiver).free();
    }
}
