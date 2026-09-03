package com.sourcegraph.demo.bigbadmonolith.app.rest;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * JAX-RS application entry point. The empty body lets CDI discover the annotated resource classes
 * and providers automatically. All REST endpoints are served under {@code /api}.
 */
@ApplicationPath("/api")
public class RestApplication extends Application {
}
