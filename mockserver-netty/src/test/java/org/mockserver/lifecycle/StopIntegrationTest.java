package org.mockserver.lifecycle;

import org.junit.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.mockserver.MockServer;
import org.mockserver.socket.PortFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * @author jamesdbloom
 */
public class StopIntegrationTest {

    private final static int MOCK_SERVER_PORT = PortFactory.findFreePort();

    @Test
    public void canStartAndStopMultipleTimesViaClient() {
        // start server
        new MockServer(MOCK_SERVER_PORT);

        // start client
        MockServerClient mockServerClient = new MockServerClient("localhost", MOCK_SERVER_PORT);

        for (int i = 0; i < 2; i++) {
            // when
            mockServerClient.stop();

            // then
            assertFalse(mockServerClient.isRunning());
            new MockServer(MOCK_SERVER_PORT);
            assertTrue(mockServerClient.isRunning());
        }

        assertTrue(mockServerClient.isRunning());
        mockServerClient.stop();
        assertFalse(mockServerClient.isRunning());
    }

    @Test
    public void canStartAndStopMultipleTimes() {
        // start server
        MockServer mockServer = new MockServer(MOCK_SERVER_PORT);

        for (int i = 0; i < 2; i++) {
            // when
            mockServer.stop();

            // then
            assertFalse(mockServer.isRunning());
            mockServer = new MockServer(MOCK_SERVER_PORT);
            assertTrue(mockServer.isRunning());
        }

        assertTrue(mockServer.isRunning());
        mockServer.stop();
        assertFalse(mockServer.isRunning());
    }

    @Test
    public void closesSocketBeforeStopMethodReturns() {
        // start server
        MockServer mockServer = new MockServer(MOCK_SERVER_PORT);

        // when
        mockServer.stop();

        // then
        try {
            new Socket("localhost", MOCK_SERVER_PORT);
            fail("socket should be closed");
        } catch (IOException ioe) {
            assertThat(ioe.getMessage(), containsString("Connection refused"));
        }
    }

    @Test
    public void freesPortBeforeStopMethodReturns() {
        // start server
        MockServer mockServer = new MockServer(MOCK_SERVER_PORT);

        // when
        mockServer.stop();

        // then
        try {
            final ServerSocket serverSocket = new ServerSocket(MOCK_SERVER_PORT);
            assertThat(serverSocket.isBound(), is(true));
        } catch (IOException ioe) {
            fail("port should be freed");
        }
    }
}
