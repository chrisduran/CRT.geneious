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
        return "Crispr Recognition Tool Wrapper";
    }

    public String getHelp() {
        return "Visit http://www.room220.com/crt/ for further information related to the detection algorithm";
    }

    public String getDescription() {
        String description = "A Geneious plugin wrapper for Bland C, Ramsey TL, Sabree F, Lowe M, Brown K, Kyrpides NC, Hugenholtz P: " +
                "CRISPR Recognition Tool (CRT): a tool for automatic detection of clustered regularly interspaced palindromic repeats. " +
                "BMC Bioinformatics. 2007 Jun 18;8(1):209." +
                "\n\n" +
                "This plugin wraps CRT version 1.2, which is Public Domain Software." +
                "\n\n" +
                "http://www.room220.com/crt/" +
                "\n\n" +
                "To the extent possible under law, Chris Duran has waived all copyright and related or neighboring rights to CRISPR Recognition Tool Wrapper.";
        return description;
    }



    public String getAuthors() {
        return "Bland et al (algorithm), Chris Duran (wrapper)";
    }

    public String getVersion() {
        return "1.0";
    }

    public String getMinimumApiVersion() {
        return "4.700";
    }

    public int getMaximumApiVersion() {
        return 4;
    }
}
