package org.biomart.dino.dinos.enrichment;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;

public class GuiResponseCompiler {


    public static void
    compile(File tpl, OutputStream out, Map<String, Object> binding) throws IOException {
        FileReader in = null;
        BufferedWriter writer = null;
        try {
            in = new FileReader(tpl);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            MustacheFactory mf = new DefaultMustacheFactory();
            Mustache mustache = mf.compile(in, "enrichment.html", "[", "]");
            mustache.execute(writer, binding);
            writer.flush();
        } finally {
            if (in != null) {
                in.close();
            }

            if (writer != null) {
                writer.close();
            }
        }
    }
}
