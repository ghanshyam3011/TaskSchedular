package com.taskscheduler;

import java.io.File;
import java.util.logging.Logger;

/**
 * Helper class to detect execution environment and adapt behavior accordingly
 */
public class EnvironmentDetector {
    private static final Logger logger = Logger.getLogger(EnvironmentDetector.class.getName());
    private static Boolean isDockerEnvironment = null;
    
    /**
     * Detects if the application is running inside a Docker container
     * 
     * @return true if running in Docker, false otherwise
     */
    public static boolean isRunningInDocker() {
        if (isDockerEnvironment != null) {
            return isDockerEnvironment;
        }

        // Check for Docker environment variable
        String dockerEnv = System.getenv("RUNNING_IN_DOCKER");
        if (dockerEnv != null && dockerEnv.equalsIgnoreCase("true")) {
            logger.info("Detected Docker environment via environment variable");
            isDockerEnvironment = true;
            return true;
        }

        // Check for Docker detection file
        File dockerFile = new File("/app/running_in_docker");
        if (dockerFile.exists()) {
            logger.info("Detected Docker environment via detection file");
            isDockerEnvironment = true;
            return true;
        }

        // Check for .dockerenv file
        File dockerEnvFile = new File("/.dockerenv");
        if (dockerEnvFile.exists()) {
            logger.info("Detected Docker environment via .dockerenv file");
            isDockerEnvironment = true;
            return true;
        }

        isDockerEnvironment = false;
        return false;
    }
    
    /**
     * Gets the appropriate command to execute shell commands based on the environment
     * 
     * @return array of command and parameter to execute shell commands
     */
    public static String[] getShellExecutor() {
        if (isRunningInDocker()) {
            logger.info("Using bash for command execution in Docker");
            return new String[]{"bash", "-c"};
        } else {
            // Windows environment
            logger.info("Using cmd.exe for command execution in Windows");
            return new String[]{"cmd.exe", "/c"};
        }
    }
}
