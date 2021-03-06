package com.github.dreamhead.moco.runner;

import com.github.dreamhead.moco.bootstrap.StartArgs;
import org.apache.http.conn.HttpHostConnectException;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;

import static com.github.dreamhead.moco.RemoteTestUtils.port;
import static com.github.dreamhead.moco.RemoteTestUtils.root;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class ShutdownMonitorRunnerTest extends AbstractRunnerTest {

    @Test(expected = HttpHostConnectException.class)
    public void should_shutdown_runner_by_socket() throws IOException {
        String shutdownMocoKey = "_SHUTDOWN_MOCO_KEY";
        int shutdownPort = 9527;

        RunnerFactory factory = new RunnerFactory(shutdownPort, shutdownMocoKey);
        runner = factory.createRunner(new StartArgs(port(), shutdownPort, "src/test/resources/foo.json", null));
        runner.run();

        try {
            assertThat(helper.get(root()), is("foo"));
        } catch (HttpHostConnectException e) {
            fail(e.getMessage());
        }

        shutdownMoco(shutdownPort, shutdownMocoKey);
        waitChangeHappens();
        helper.get(root());
    }

    @Test(expected = ConnectException.class)
    public void should_stop_runner_directly() throws IOException {
        String shutdownMocoKey = "_SHUTDOWN_MOCO_KEY";
        int shutdownPort = 9527;

        RunnerFactory factory = new RunnerFactory(shutdownPort, shutdownMocoKey);
        runner = factory.createRunner(new StartArgs(port(), shutdownPort, "src/test/resources/foo.json", null));
        runner.run();

        try {
            assertThat(helper.get(root()), is("foo"));
        } catch (HttpHostConnectException e) {
            fail(e.getMessage());
        }

        runner.stop();

        shutdownMoco(shutdownPort, shutdownMocoKey);
    }

    private void shutdownMoco(int shutdownPort, String shutdownMocoKey) throws IOException {
        Socket socket = new Socket(InetAddress.getByName("127.0.0.1"), shutdownPort);
        socket.setSoLinger(false, 0);

        OutputStream outputStream = socket.getOutputStream();
        outputStream.write((shutdownMocoKey + "\r\n").getBytes());

        outputStream.flush();
        socket.close();
    }
}
