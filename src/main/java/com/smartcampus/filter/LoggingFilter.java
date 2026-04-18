package com.smartcampus.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

@Provider
public class LoggingFilter
        implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOG =
            Logger.getLogger(LoggingFilter.class.getName());

    @Override
    public void filter(ContainerRequestContext request)
            throws IOException {
        LOG.info("REQUEST --> "
                + request.getMethod()
                + " "
                + request.getUriInfo().getRequestUri());
    }

    @Override
    public void filter(ContainerRequestContext request,
                       ContainerResponseContext response)
            throws IOException {
        LOG.info("RESPONSE --> Status: "
                + response.getStatus());
    }
}