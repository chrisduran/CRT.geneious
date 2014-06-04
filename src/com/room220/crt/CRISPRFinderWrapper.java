package com.room220.crt;


/**
 * Created by chris on 4/6/14.
 */
public class CRISPRFinderWrapper extends CRISPRFinder {

    public CRISPRFinderWrapper(String _name, String _sequence, int _screenDisplay, int _minNumRepeats, int _minRepeatLength, int _maxRepeatLength, int _minSpacerLength, int _maxSpacerLength, int _searchWindowLength)
    {
        super("input.fasta","crispr.out",_screenDisplay,_minNumRepeats,_minRepeatLength,_maxRepeatLength,_minSpacerLength,_maxSpacerLength,_searchWindowLength);
        sequence = new DNASequence(_sequence, _name);
    }
}
