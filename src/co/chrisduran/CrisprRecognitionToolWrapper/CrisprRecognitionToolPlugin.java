package co.chrisduran.CrisprRecognitionToolWrapper;

import com.biomatters.geneious.publicapi.plugin.GeneiousPlugin;
import com.biomatters.geneious.publicapi.plugin.SequenceAnnotationGenerator;

/**
 * A Geneious plugin wrapper for the CRT application by Bland et al (http://www.room220.com/crt/).
 * 
 * This class just provides the framework to hook the {@link co.chrisduran.CrisprRecognitionToolWrapper.CrisprRecognitionTool}
 * into Geneious. All of the real work happens in {@link co.chrisduran.CrisprRecognitionToolWrapper.CrisprRecognitionTool}.
 */
public class CrisprRecognitionToolPlugin extends GeneiousPlugin {
    public SequenceAnnotationGenerator[] getSequenceAnnotationGenerators() {
        return new SequenceAnnotationGenerator[]{
                new CrisprRecognitionTool()
        };
    }

    public String getName() {
        return "CrisprRecognitionToolPlugin";
    }

    public String getHelp() {
        return "CrisprRecognitionToolPlugin";
    }

    public String getDescription() {
        return "A Geneious plugin wrapper for Bland C, Ramsey TL, Sabree F, Lowe M, Brown K, Kyrpides NC, Hugenholtz P: CRISPR Recognition Tool (CRT): a tool for automatic detection of clustered regularly interspaced palindromic repeats. BMC Bioinformatics. 2007 Jun 18;8(1):209. This plugin wraps CRT version 1.2";
    }



    public String getAuthors() {
        return "Bland et al (algorithm), Chris Duran (wrapper)";
    }

    public String getVersion() {
        return "0.1";
    }

    public String getMinimumApiVersion() {
        return "4.1";
    }

    public int getMaximumApiVersion() {
        return 4;
    }
}
