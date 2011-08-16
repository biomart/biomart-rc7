package org.biomart.api.rest;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.lang.StringEscapeUtils;
import org.biomart.common.constants.OutputConstants;

/**
 *
 * @author jhsu
 */
public class IframeOutputStream extends FilterOutputStream implements OutputConstants {
    private final byte[][] HTML;

    public IframeOutputStream(String uuid, OutputStream out, String scope) throws IOException {
        super(out);

		// Helper HTML code
        HTML = new byte[5][];
        HTML[0] = "<!doctype html><html><head><title></title></head><body>".getBytes();
        HTML[1] = ("<script>parent." + scope + ".write('" + uuid + "','").getBytes();
        HTML[2] = ("')</script>\n<script>parent." + scope + ".write('" + uuid + "','").getBytes();
        HTML[3] = ("')</script>\n<script>parent." + scope + ".done('" + uuid + "')</script></body></html>").getBytes();
        HTML[4] = "<span></span>".getBytes();

        out.write(HTML[0]);
        out.write(HTML[1]);
    }

	// Buffer the results so we don't stream line by line to the iframe's 
	// callback function
	private static final int WRITE_LIMIT = 50;
	private StringBuilder sb = new StringBuilder();
	private int pos = 0;

	/*
	 * Only write to the callback function when we've reached WRITE_LIMIT.
	 * This buffering will prevent performance degradation on the client-side
	 * JavaScript engine.
	 */
    @Override
    public void write(byte[] bytes) throws IOException {
		String line = StringEscapeUtils.escapeJavaScript(new String(bytes));

		sb.append(line);

		if (pos == WRITE_LIMIT-1) {
			out.write(sb.toString().getBytes());
			out.write(HTML[2]);
			pos = 0;
			sb = new StringBuilder();
		} else {
			pos++;
		}
    }

    @Override
    public void close() throws IOException {
		out.write(sb.toString().getBytes());
        out.write(HTML[3]);
        super.close();
    }
}
