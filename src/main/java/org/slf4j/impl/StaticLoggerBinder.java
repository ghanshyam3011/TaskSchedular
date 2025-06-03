package org.slf4j.impl;

import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

public class StaticLoggerBinder implements LoggerFactoryBinder {
    private static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder();
    
    public static StaticLoggerBinder getSingleton() {
        return SINGLETON;
    }
    
    private StaticLoggerBinder() {
        // Private constructor to ensure singleton
    }
    
    @Override
    public ILoggerFactory getLoggerFactory() {
        return new NoOpLoggerFactory();
    }
    
    @Override
    public String getLoggerFactoryClassStr() {
        return NoOpLoggerFactory.class.getName();
    }
}
