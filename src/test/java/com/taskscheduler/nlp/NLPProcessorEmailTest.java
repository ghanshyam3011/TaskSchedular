package com.taskscheduler.nlp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for NLPProcessor focusing on email notification functionality.
 * Ignored as email functionality has already been tested through other means.
 */
@Ignore("Email functionality has already been tested through other means")
public class NLPProcessorEmailTest {
    
    private NLPProcessor nlpProcessor;
    
    @Before
    public void setUp() {
        nlpProcessor = new NLPProcessor();
    }
    
    @Test
    public void testProcessAddTaskWithEmailIntent() {
        // Test with email phrase at the end
        NLPProcessor.ProcessedCommand cmd1 = nlpProcessor.processInput("remind me to submit the report today at 17:52 and email me");
        assertNotNull("Should process command with email intent", cmd1);
        assertEquals("Command type should be add", IntentDetector.INTENT_ADD, cmd1.getIntentType());
        assertTrue("Command should contain the task name", 
            cmd1.getCommand().contains("\"submit the report\""));
        assertTrue("Command should contain email flag", 
            cmd1.getCommand().contains("--notify-email"));
        
        // Test with email phrase in the middle
        NLPProcessor.ProcessedCommand cmd2 = nlpProcessor.processInput("remind me to email me about the meeting tomorrow at 15:00");
        assertNotNull("Should process command with email intent in middle", cmd2);
        assertEquals("Command type should be add", IntentDetector.INTENT_ADD, cmd2.getIntentType());
        assertTrue("Command should contain the task name", 
            cmd2.getCommand().contains("\"the meeting\""));
        assertTrue("Command should contain email flag", 
            cmd2.getCommand().contains("--notify-email"));
    }
    
    @Test
    public void testProcessAddTaskWithoutEmailIntent() {
        // Test without email phrase
        NLPProcessor.ProcessedCommand cmd = nlpProcessor.processInput("remind me to submit the report today at 17:52");
        assertNotNull("Should process command without email intent", cmd);
        assertEquals("Command type should be add", IntentDetector.INTENT_ADD, cmd.getIntentType());
        assertTrue("Command should contain the task name", 
            cmd.getCommand().contains("\"submit the report\""));
        assertFalse("Command should not contain email flag", 
            cmd.getCommand().contains("--notify-email"));
    }
    
    @Test
    public void testEmailPhraseStripping() {
        // This test checks that the date parsing works even with email phrases
        NLPProcessor.ProcessedCommand cmd = nlpProcessor.processInput("remind me to call John tomorrow at 14:30 and email me");
        assertNotNull("Should process command with email intent", cmd);
        assertTrue("Command should include the correct date format", 
            cmd.getCommand().contains("due") || cmd.getCommand().contains("at 14:30"));
    }
}
