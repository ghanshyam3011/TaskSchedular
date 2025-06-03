package org.slf4j.impl;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.helpers.NOPLogger;

public class NoOpLoggerFactory implements ILoggerFactory {
    @Override
    public Logger getLogger(String name) {
        return NOPLogger.NOP_LOGGER;
    }
}
