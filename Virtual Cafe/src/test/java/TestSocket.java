import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TestSocket extends Socket {
    private final ByteArrayInputStream inputStream;
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private boolean closed = false;

    public TestSocket(String input) {
        this.inputStream = new ByteArrayInputStream(input.getBytes());
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public OutputStream getOutputStream() {
        return outputStream;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close() throws IOException {
        closed = true;
        inputStream.close();
        outputStream.close();
    }

    public String getOutput() {
        return outputStream.toString();
    }
}
