/*
 * Copyright 2014 Red Hat, Inc. and others.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package org.eclipse.hono.adapter.http;

import java.util.Base64;

import org.eclipse.hono.service.auth.device.HonoAuthHandler;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.web.RoutingContext;


/**
 * A Hono specific version of vert.x web's standard {@code BasicAuthHandlerImpl}
 * that does not swallow the root exception which caused an authentication failure.
 * <p>
 * This class is a combination of
 * {@code io.vertx.ext.web.handler.impl.BasicAuthHandlerImpl} and
 * {@code io.vertx.ext.web.handler.impl.AuthorizationAuthHandler}.
 *
 */
public class HonoBasicAuthHandler extends HonoAuthHandler {

    /**
     * Creates a new handler for an authentication provider and a security realm.
     * 
     * @param authProvider The provider to use for verifying credentials.
     * @param realm The security realm.
     */
    public HonoBasicAuthHandler(final AuthProvider authProvider, final String realm) {
        super(authProvider, realm);
    }

    @Override
    public void parseCredentials(final RoutingContext context, final Handler<AsyncResult<JsonObject>> handler) {

      parseAuthorization(context, false, parseAuthorization -> {
        if (parseAuthorization.failed()) {
          handler.handle(Future.failedFuture(parseAuthorization.cause()));
          return;
        }

        final String suser;
        final String spass;

        try {
          // decode the payload
            final String decoded = new String(Base64.getDecoder().decode(parseAuthorization.result()));

            final int colonIdx = decoded.indexOf(":");
          if (colonIdx != -1) {
            suser = decoded.substring(0, colonIdx);
            spass = decoded.substring(colonIdx + 1);
          } else {
            suser = decoded;
            spass = null;
          }
        } catch (RuntimeException e) {
          // IllegalArgumentException includes PatternSyntaxException
          context.fail(e);
          return;
        }

        handler.handle(Future.succeededFuture(new JsonObject().put("username", suser).put("password", spass)));
      });
    }

    @Override
    protected String authenticateHeader(final RoutingContext context) {
      return "Basic realm=\"" + realm + "\"";
    }

    /**
     * Extracts authentication information from the <em>Authorization</em>
     * header of an HTTP request.
     * 
     * @param ctx The routing context that contains the HTTP request.
     * @param optional Indicates whether the authorization header is mandatory.
     * @param handler The handler to invoke with the authentication info.
     */
    protected final void parseAuthorization(final RoutingContext ctx, final boolean optional,
            final Handler<AsyncResult<String>> handler) {

        final HttpServerRequest request = ctx.request();
        final String authorization = request.headers().get(HttpHeaders.AUTHORIZATION);

        if (authorization == null) {
          if (optional) {
            // this is allowed
            handler.handle(Future.succeededFuture());
          } else {
            handler.handle(Future.failedFuture(UNAUTHORIZED));
          }
          return;
        }

        try {
          final int idx = authorization.indexOf(' ');

          if (idx <= 0) {
            handler.handle(Future.failedFuture(BAD_REQUEST));
            return;
          }

          if (!"Basic".equalsIgnoreCase(authorization.substring(0, idx))) {
            handler.handle(Future.failedFuture(UNAUTHORIZED));
            return;
          }

          handler.handle(Future.succeededFuture(authorization.substring(idx + 1)));
        } catch (RuntimeException e) {
          handler.handle(Future.failedFuture(e));
        }
      }
}
