package org.biomart.api.rest;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.biomart.common.constants.OutputConstants;

/**
 *
 * @author jhsu
 */
public class IframeOutputStream extends FilterOutputStream implements OutputConstants {
    private final byte[][] HTML;

    public IframeOutputStream(String uuid, OutputStream out, String scope) throws IOException {
        super(out);

        HTML = new byte[5][];
        HTML[0] = "<!doctype html><html><head><title></title></head><body>".getBytes();
        HTML[1] = ("<script>parent." + scope + ".write('" + uuid + "','").getBytes();
        HTML[2] = ("')</script>\n<script>parent." + scope + ".write('" + uuid + "','").getBytes();
        HTML[3] = ("')</script>\n<script>parent." + scope + ".done('" + uuid + "')</script></body></html>").getBytes();
        HTML[4] = "<span></span>".getBytes();

        out.write(HTML[0]);
        // Pad some junk HTML so WebKit will start streaming
        for (int i=0; i<100; i++) {
            out.write(HTML[4]);
        }
        out.write(HTML[1]);
    }

    @Override
    public void write(int b) throws IOException {
        if (b == NEWLINE) {
            out.write(HTML[2]);
        } else {
            if (b == QUOTE) {
                out.write(BACK_SLASH);
            }
            out.write(b);
        }
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        for (byte b : bytes) {
            this.write(b);
        }
    }

    @Override
    public void close() throws IOException {
        out.write(HTML[3]);
        super.close();
    }
}
