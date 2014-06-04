package co.chrisduran.CrisprRecognitionToolWrapper;

import com.biomatters.geneious.publicapi.plugin.*;
import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotation;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotationInterval;
import com.biomatters.geneious.publicapi.utilities.SequenceUtilities;

import com.room220.crt.CRISPRFinderWrapper;
import jebl.util.ProgressListener;

import java.util.*;

/**
 * This plugin shows how to create a simple SequenceAnnotationGenerator by providing a simple implementation of a Motif finder.
 * 
 * @author Matt Kearse
 * @version $Id$
 */

public class CrisprRecognitionTool extends SequenceAnnotationGenerator {
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

        String sequenceString=sequence.getSequenceString().toUpperCase();

        List<SequenceAnnotation> results = new ArrayList<SequenceAnnotation>();
        String basesToFind=options.getBasesToFind();
        if (basesToFind.length()==0) {
            throw new DocumentOperationException("Must specify at least 1 base to find");
        }

        CRISPRFinderWrapper cfw = new CRISPRFinderWrapper(sequence.getName(),sequence.getSequenceString().toUpperCase(),0,3,19,28,19,48,8);

        return Arrays.asList(results); // Put the results in a single element array since we only operate on a single sequence hence there is only 1 set of results.
    }

    private static class CrisprRecognitionToolOptions extends Options {
        private final BooleanOption alsoFindReverse;
        private final StringOption basesToFind;
        private final StringOption annotationType;
        private final StringOption annotationName;
        private CrisprRecognitionToolOptions() {
            basesToFind = addStringOption("basesToFind","Bases to find","");
            alsoFindReverse = addBooleanOption("alsoFindReverse", "Also find on reverse complement", true);

            annotationType = addStringOption("annotationType","Annotation type", SequenceAnnotation.TYPE_MOTIF);
            annotationType.setAdvanced(true);
            annotationType.setDescription("The type of annotations that will be created on each matching occurrence");

            annotationName = addStringOption("annotationName","Annotation name", "");
            annotationName.setAdvanced(true);
            annotationName.setDescription("The name of the annotations that will be created on each matching occurrence");
        }

        public boolean isAlsoFindReverse() {
            return alsoFindReverse.getValue();
        }

        public String getBasesToFind() {
            return basesToFind.getValue().toUpperCase();
        }

        public String getAnnotationType() {
            return annotationType.getValue();
        }

        public String getAnnotationName() {
            return annotationName.getValue();
        }

    }
}
