package org.biomart.dino.tests;

import org.biomart.dino.dinos.BedDino;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

public class BedDinoTest {

    
    BedDino dino;
    final String b1 = "chrChr-_R\t123\t456", e1 = "Chr-_R:123:456", 
            b2 = "chrX\t1\t2", e2 = "X:1:2",
            b3 = "10\t13\t25", e3 = "10:13:25",
            b4 = "chr3\t123\t456\tname\t34\t-", e4 = "3:123:456:-",
            b5 = "chr3\t123\t456\tname\t34\t+", e5 = "3:123:456:+",
            b6 = "chr3\t123\t456\t\t\t+", e6 = "3:123:456:+",
            b7 = "chr3\t123\t456\t2\t\t+", e7 = "3:123:456:+";;
    
    
    @Before
    public void setUp() throws Exception {
        dino = new BedDino();
    }

    @Test
    public void singleLineTest() {
        assertEquals(e1, dino.getEnsemblFormat(b1));
        assertEquals(e2, dino.getEnsemblFormat(b2));
        assertEquals(e3, dino.getEnsemblFormat(b3));
        assertEquals(e4, dino.getEnsemblFormat(b4));
        assertEquals(e5, dino.getEnsemblFormat(b5));
        assertEquals(e6, dino.getEnsemblFormat(b6));
        assertEquals(e7, dino.getEnsemblFormat(b7));
    }
    
    
    @Test
    public void multiLineTest() {
        assertEquals(e1+","+e2+","+e3+","+e4+","+e5+","+e6+","+e7, 
                dino.getEnsemblFormat(b1+"\n"+b2+"\n"+b3+"\n"+b4+"\n"+b5+"\n"+b6+"\n"+b7));
    }
    
    
    @Test
    public void singleLineFailTest() {
        assertEquals("", dino.getEnsemblFormat("\t123\t456"));
        assertEquals("", dino.getEnsemblFormat("chrChr-_R\t\t456"));
        assertEquals("", dino.getEnsemblFormat("chrChr-_R\t123\t"));
        assertEquals("", dino.getEnsemblFormat("chrChr-_R123\t456"));
        assertEquals("", dino.getEnsemblFormat("chrChr-_R\t123456"));
        assertEquals("", dino.getEnsemblFormat("chrChr-_R123456"));
        assertEquals("", dino.getEnsemblFormat(""));
        assertEquals("", dino.getEnsemblFormat("chr3\t123\t456\t\t34\t"));
        assertEquals("", dino.getEnsemblFormat("chr3\t123\t456\tname\t\t"));
        assertEquals("", dino.getEnsemblFormat("chr3\t123\t456\tname\t34"));
        assertEquals("", dino.getEnsemblFormat("chr3\t123\t456\tname\t34\t"));
    }

}
