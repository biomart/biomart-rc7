package org.biomart.processors;

import com.google.common.io.Closeables;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import org.biomart.common.constants.OutputConstants;
import org.biomart.common.resources.Log;
import org.biomart.processors.annotations.ContentType;
import org.jdom.Comment;

/**
 *
 * @author Joachim Baran
 *
 */
@ContentType("application/sparql-results+xml")
public class SPARQLXML extends ProcessorImpl {
    @Override
    public void setOutputStream(OutputStream out) {
        String prelude = null;
        String[] variableNames = new String[0];
        String exception = null;
        for (Object content : query.queryXMLobject.getContent()) {
            if (content instanceof Comment) {
                Comment comment = (Comment)content;

                // Variable names starting with a "?" are used only internally
                // and their values are not returned.

                if (comment.getText().startsWith(" VARIABLES:")) {
                    variableNames = comment.getText().substring(11).trim().split(" ");
                } else if (comment.getText().startsWith(" BioMart XML-Query:")) {
                    prelude = "<!--" + comment.getText() + "-->\n";
                } else if (comment.getText().startsWith(" SPARQL-Exception:")) {
                    exception = comment.getText().substring(18);
                }
            }
        }

        this.out = new XMLOutputStream(out, variableNames, prelude, exception);
    }

    @Override
    public void done() {
        Closeables.closeQuietly(out);
    }

    private class XMLOutputStream extends FilterOutputStream implements OutputConstants {
        protected boolean startOfLine = true;
        protected boolean exception = false;

        private int column = 0;
        private boolean[] visibleColumn;
        private int[] nextVisibleColumn;
        private int lastVisibleColumn;

        protected String[] variableNames;

        public XMLOutputStream(OutputStream out, String[] variableNames, String prelude, String exception) {
            super(out);
            try {

                if (exception != null) {
                    out.write(("<exception>" + exception + "</exception>").getBytes());
                    this.exception = true;
                    return;
                }

                out.write("<?xml version=\"1.0\"?>\n".getBytes());

                if (prelude != null)
                    out.write(prelude.getBytes());

                this.variableNames = variableNames;
                this.visibleColumn = new boolean[variableNames.length];
                this.nextVisibleColumn = new int[variableNames.length];

                Arrays.fill(nextVisibleColumn, -1);

                out.write("<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\">\n\t<head>\n".getBytes());

                for (int i = 0, j = 0; i < variableNames.length; i++) {
                    String variableName = variableNames[i];

                    if (!variableName.startsWith("?") &&  // Un-projected query variables
                        !variableName.startsWith("!")) {  // Virtual variables for filters
                        out.write(("\t\t<variable name=\"" + variableName + "\"/>\n").getBytes());
                        visibleColumn[i] = true;
                        nextVisibleColumn[j] = i;
                        j = i;
                        lastVisibleColumn = i;
                    }
                }

                out.write("\t</head>\n\t<results>\n".getBytes());
            } catch (IOException e) {
                Log.error("IOException in SPARQLXML.XMLOutputStream constructor", e);
            }
        }
 
        @Override
        public void write(int b) throws IOException {
            if (exception) return;

            b &= 0xff; // force argument to one byte

            if (startOfLine) {
                int nextColumn = 0;

                if (!visibleColumn[column])
                    nextColumn = nextVisibleColumn[0];

                out.write(("\t\t<result>\n\t\t\t<binding name=\"" + variableNames[nextColumn] + "\">\n\t\t\t\t<literal>").getBytes());

                startOfLine = false;
            }

            switch(b) {
                case NEWLINE:
                    out.write("</literal>\n\t\t\t</binding>\n\t\t</result>\n".getBytes());

                    startOfLine = true;
                    column = 0;
                    break;
                case TAB:
                    if (!visibleColumn[column] || column == lastVisibleColumn) {
                        column++;
                    } else {
                        int nextColumn = nextVisibleColumn[column++];

                        if (nextColumn == -1)
                            throw new RuntimeException("Internal data format corrupted.");

                        out.write(("</literal>\n\t\t\t</binding>\n\t\t\t<binding name=\"" + variableNames[nextColumn] + "\">\n\t\t\t\t<literal>").getBytes());
                    }
                    break;
                default:
                    if (visibleColumn[column])
                        out.write(b);
                    
                    break;
            }
        }

        @Override
        public void close() throws IOException {
            if (exception) return;

            out.write("\t</results>\n</sparql>\n".getBytes());
            super.close();
        }
    }
}
