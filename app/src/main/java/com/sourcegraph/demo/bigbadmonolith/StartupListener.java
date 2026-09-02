package com.sourcegraph.demo.bigbadmonolith;

import com.sourcegraph.demo.bigbadmonolith.common.ConnectionManager;
import com.sourcegraph.demo.bigbadmonolith.common.LibertyConnectionManager;
import com.sourcegraph.demo.bigbadmonolith.service.DataInitializationService;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.logging.Level;
import java.util.logging.Logger;

@WebListener
public class StartupListener implements ServletContextListener {

    private static final Logger LOGGER = Logger.getLogger(StartupListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            // Initialize database schema for Liberty if needed
            if (LibertyConnectionManager.isLibertyDataSourceAvailable()) {
                LOGGER.info("Running on WebSphere Liberty - using managed DataSource");
                LibertyConnectionManager.initializeDatabaseSchema();
            } else {
                LOGGER.info("Running in embedded mode - using embedded Derby");
            }

            // Initialize sample data
            DataInitializationService dataService = new DataInitializationService();
            dataService.initializeSampleData();
            LOGGER.info("Sample data initialized successfully");
        } catch (RuntimeException | java.sql.SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize application", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            // Only shutdown embedded Derby, Liberty manages its own DataSource
            if (!LibertyConnectionManager.isLibertyDataSourceAvailable()) {
                ConnectionManager.shutdown();
                LOGGER.info("Embedded Derby database shutdown successfully");
            } else {
                LOGGER.info("Liberty DataSource will be managed by server");
            }
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Failed to shutdown database", e);
        }
    }
}
