package com.mycompany.trust;

import java.util.Date;

public class BerryConversionProposal extends Proposal {
    private double conversionPercentage;
    private int conversionPeriod;

    /**
     * Constructor for creating a berry conversion proposal with title, description, and proposer
     */
    public BerryConversionProposal(String title, String description, String proposer) {
        super(title, description, proposer);
        this.conversionPercentage = 10.0; // Default value
        this.conversionPeriod = 3; // Default value in months
    }
    
    /**
     * Constructor for creating a berry conversion proposal with specific parameters
     */
    public BerryConversionProposal(String proposer, double conversionPercentage, int conversionPeriod) {
        super("Berry Conversion Proposal", "Proposal for adjusting berry conversion rates and periods", proposer);
        this.conversionPercentage = conversionPercentage;
        this.conversionPeriod = conversionPeriod;
    }
    
    /**
     * Constructor with ID for database loading
     */
    public BerryConversionProposal(int id, String proposer, double conversionPercentage, int conversionPeriod, int votes) {
        super(proposer);
        setId(id);
        this.conversionPercentage = conversionPercentage;
        this.conversionPeriod = conversionPeriod;
        setVotes(votes);
    }

    /**
     * Constructor for loading from database with creation date
     */
    public BerryConversionProposal(String proposer, double conversionPercentage, int conversionPeriod, Date creationDate) {
        super("Berry Conversion Proposal", "Proposal for adjusting berry conversion rates and periods", proposer, creationDate);
        this.conversionPercentage = conversionPercentage;
        this.conversionPeriod = conversionPeriod;
    }

    // Getters and setters specific to BerryConversionProposal
    public double getConversionPercentage() {
        return conversionPercentage;
    }

    public void setConversionPercentage(double conversionPercentage) {
        this.conversionPercentage = conversionPercentage;
    }

    public int getConversionPeriod() {
        return conversionPeriod;
    }

    public void setConversionPeriod(int conversionPeriod) {
        this.conversionPeriod = conversionPeriod;
    }
    
    public void addVote() {
        setVotes(getVotes() + 1);
    }

    // Add these methods for UI compatibility
    public String getFromBerryType() {
        return "Standard";
    }
    public String getToBerryType() {
        return "Premium";
    }
    public double getConversionRate() {
        return conversionPercentage;
    }

    @Override
    public String toString() {
        return "Berry Conversion Proposal #" + getId() + 
               " | Conversion Rate: " + conversionPercentage + "%" +
               " | Conversion Period: " + conversionPeriod + " months" +
               " | Votes: " + getVotes() + 
               " | Proposed by: " + getProposer();
    }
}
