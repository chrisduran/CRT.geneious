package co.chrisduran.CrisprRecognitionToolWrapper;

import com.biomatters.geneious.publicapi.plugin.*;
import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotation;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotationInterval;
import com.biomatters.geneious.publicapi.utilities.FileUtilities;
import com.biomatters.geneious.publicapi.utilities.SequenceUtilities;

import com.room220.crt.CRISPRFinder;
import jebl.util.ProgressListener;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This plugin shows how to create a simple SequenceAnnotationGenerator by providing a simple implementation of a Motif finder.
 * 
 * @author Matt Kearse
 * @version $Id$
 */

public class CrisprRecognitionTool extends SequenceAnnotationGenerator {

    public static String TYPE_CRISPR = "CRISPR";
    public static String TYPE_CRISPR_REPEAT_UNIT = "CRISPR Repeat Unit";
    public static String TYPE_CRISPR_SPACER = "CRISPR Spacer";

    private static Pattern totalRangePattern = Pattern.compile("Range: (\\d+) - (\\d+)");
    private static Pattern intervalPattern = Pattern.compile("^(\\d+)\\t*([GATC]+)\\t*([GATC]+)");

    public GeneiousActionOptions getActionOptions() {
        return new GeneiousActionOptions("Find CRISPR loci...",
                "Finds a non-ambiguous motif and creates annotations covering each occurrence").
                setMainMenuLocation(GeneiousActionOptions.MainMenu.AnnotateAndPredict);
    }

    public String getHelp() {
        return "This plugin shows how to create a simple SequenceAnnotationGenerator by providing a simple implementation of a Motif finder.";
    }

    public Options getOptions(AnnotatedPluginDocument[] documents, SelectionRange selectionRange) throws DocumentOperationException {
        return new CrisprRecognitionToolOptions(); // Provides all the options we display to the user. See CrisprRecognitionToolOptions below for details.
    }

    public DocumentSelectionSignature[] getSelectionSignatures() {
        return new DocumentSelectionSignature[] {
                new DocumentSelectionSignature(NucleotideSequenceDocument.class,1,1)
                // This indicates this annotation generator will accept a single nucleotide sequence as input  
        };
    }

    public List<List<SequenceAnnotation>> generateAnnotations(AnnotatedPluginDocument[] annotatedPluginDocuments, SelectionRange selectionRange, ProgressListener progressListener, Options _options) throws DocumentOperationException {
        NucleotideSequenceDocument sequence= (NucleotideSequenceDocument) annotatedPluginDocuments[0].getDocument();
        // We can safely access the first element from the array since our selection signature specifies we accept 1 and only 1 document.
        // And we can safely cast it to a NucleotideSequenceDocument as the selection signature specified that too.

        CrisprRecognitionToolOptions options = (CrisprRecognitionToolOptions) _options;
        // We can safely cast this to CrisprRecognitionToolOptions because that is all we ever return from getOptions()

        String sequenceString=sequence.getSequenceString();
        //System.out.println(sequenceString);

        List<SequenceAnnotation> results = new ArrayList<SequenceAnnotation>();

        File outputFile;
        String outputFilePath;
        try {
            outputFile = FileUtilities.createTempFile("crf.out", true);
            outputFilePath = outputFile.getCanonicalPath();
        } catch (IOException e) {
            throw new DocumentOperationException("Cannot create temporary working directory");
        }

        CRISPRFinder crisprFinder = new CRISPRFinder("input.fasta",outputFilePath,0,
                options.getMinNR(),options.getMinRL(),options.getMaxRL(),options.getMinSL(),options.getMaxSL(),options.getSearchWL());

        crisprFinder.insertSequenceAndFindRepeats(sequence.getName(), sequenceString);

        parseOutputIntoAnnotations(outputFile, results);

        return Arrays.asList(results); // Put the results in a single element array since we only operate on a single sequence hence there is only 1 set of results.
    }

    private void parseOutputIntoAnnotations(File outputFile, List<SequenceAnnotation> results) {
        BufferedReader reader;
        String currentCrispr = "";
        try {
            reader = new BufferedReader(new FileReader(outputFile));
            String line;
            Matcher interval;
            int innerCount = 1;
            List<SequenceAnnotationInterval> sequenceAnnotationIntervals =  new LinkedList<SequenceAnnotationInterval>();
            while ((line=reader.readLine())!=null) {
                if (line.startsWith("CRISPR")) {
                    String[] names = line.split(" ",3);
                    currentCrispr = names[0]+" "+names[1];
                    if (!sequenceAnnotationIntervals.isEmpty()) {
                        results.add(new SequenceAnnotation(currentCrispr+" Repeat Units",TYPE_CRISPR_REPEAT_UNIT,sequenceAnnotationIntervals.toArray(new SequenceAnnotationInterval[0])));
                        sequenceAnnotationIntervals.clear();
                    }
                    results.add(getOverallCrisprAnnotation(currentCrispr, line));
                    innerCount = 1;
                } else if ((interval = intervalPattern.matcher(line)).find()) {
                    int start = Integer.parseInt(interval.group(1));

                    if (interval.group(3).length()>1) { //dodgy regex will show one character in the third group when the CRISPR has a trailing repeat unit
                        sequenceAnnotationIntervals.add(new SequenceAnnotationInterval(start,interval.group(2).length()+start-1));
                        SequenceAnnotationInterval spacerInterval =
                                new SequenceAnnotationInterval(interval.group(2).length()+start,interval.group(3).length()+interval.group(2).length()+start-1);
                        results.add(new SequenceAnnotation(currentCrispr+"; Spacer "+innerCount,TYPE_CRISPR_SPACER,spacerInterval));
                    } else {
                        sequenceAnnotationIntervals.add(new SequenceAnnotationInterval(start,interval.group(2).length()+start));
                    }
                    innerCount++;

                }

            }
            if (!sequenceAnnotationIntervals.isEmpty()) { //clean up final annotations
                results.add(new SequenceAnnotation(currentCrispr+" Repeat Units",TYPE_CRISPR_REPEAT_UNIT,sequenceAnnotationIntervals.toArray(new SequenceAnnotationInterval[0])));
                sequenceAnnotationIntervals.clear();
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private SequenceAnnotation getOverallCrisprAnnotation(String name, String sourceStr) {
        Matcher matcher = totalRangePattern.matcher(sourceStr);

        if (matcher.find()) {
            //System.out.println(matcher.group(1)+" <><><><><> "+matcher.group(2)); //DEBUG
            SequenceAnnotationInterval interval = new SequenceAnnotationInterval(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)));
            return new SequenceAnnotation(name,TYPE_CRISPR,interval);
        }
        return null;
    }


    private static class CrisprRecognitionToolOptions extends Options {



        private final IntegerOption minNR;
        private final IntegerOption minRL;
        private final IntegerOption maxRL;
        private final IntegerOption minSL;
        private final IntegerOption maxSL;
        private final IntegerOption searchWL;
        private CrisprRecognitionToolOptions() {
            minNR= addIntegerOption("minNR","Minimum number of repeats a CRISPR must contain",3);
            minRL = addIntegerOption("minRL", "Minimum length of a CRISPR's repeated region", 19);
            minRL.setAdvanced(true);
            maxRL = addIntegerOption("maxRL","Maximum length of a CRISPR's repeated region",38);
            maxRL.setAdvanced(true);
            minSL = addIntegerOption("minSL","Minimum length of a CRISPR's non-repeated region (or spacer region)",19);
            minSL.setAdvanced(true);
            maxSL = addIntegerOption("maxSL","Maximum length of a CRISPR's non-repeated region (or spacer region)",48);
            maxSL.setAdvanced(true);
            searchWL = addIntegerOption("searchWL","Length of search window used to discover CRISPRs",8,6,9);
            searchWL.setAdvanced(true);

        }

        public int getMinNR() {
            return minNR.getValue();
        }

        public int getMinRL() {
            return minRL.getValue();
        }

        public int getMaxRL() {
            return maxRL.getValue();
        }

        public int getMinSL() {
            return minSL.getValue();
        }

        public int getMaxSL() {
            return maxSL.getValue();
        }

        public int getSearchWL() {
            return searchWL.getValue();
        }

    }
}
