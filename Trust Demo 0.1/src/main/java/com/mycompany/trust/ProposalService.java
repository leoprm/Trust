package com.mycompany.trust;

import java.sql.SQLException;

public class ProposalService {
    
    public static void createLevelProposal(String proposer, double xpIncrease, double xpThreshold) throws SQLException {
        // Use the specific DB method
        int proposalId = DatabaseManager.createLevelProposal(proposer, xpIncrease, xpThreshold);

        if (proposalId != -1) {
            LevelProposal proposal = new LevelProposal(proposer, xpIncrease, xpThreshold);
            proposal.setId(proposalId);
            // Update the in-memory map
            TrustSystem.levelProposals.put(proposalId, proposal);
        } else {
            throw new SQLException("Failed to create level proposal");
        }
    }

    public static void createBerryEarningProposal(String proposer, int initialEarning) throws SQLException {
        // Use the specific DB method
        int proposalId = DatabaseManager.createBerryEarningProposal(proposer, initialEarning);

        if (proposalId != -1) {
            BerryEarningProposal proposal = new BerryEarningProposal(proposer, initialEarning);
            proposal.setId(proposalId);
            // Update the in-memory map
            TrustSystem.berryEarningProposals.put(proposalId, proposal);
        } else {
            throw new SQLException("Failed to create berry earning proposal");
        }
    }

    public static void createBerryValidityProposal(String proposer, int months) throws SQLException {
        // Use the specific DB method
        int proposalId = DatabaseManager.createBerryValidityProposal(proposer, months);

        if (proposalId != -1) {
            BerryValidityProposal proposal = new BerryValidityProposal(proposer, months);
            proposal.setId(proposalId);
            // Update the in-memory map
            TrustSystem.berryValidityProposals.put(proposalId, proposal);
        } else {
            throw new SQLException("Failed to create berry validity proposal");
        }
    }

    public static void createBerryConversionProposal(String proposer, double conversionPercentage, int conversionPeriod) throws SQLException {
        // Use the specific DB method
        int proposalId = DatabaseManager.createBerryConversionProposal(proposer, conversionPercentage, conversionPeriod);

        if (proposalId != -1) {
            // Use appropriate constructor
            BerryConversionProposal proposal = new BerryConversionProposal(proposer, conversionPercentage, conversionPeriod);
            proposal.setId(proposalId);
            // Update the in-memory map
            TrustSystem.berryConversionProposals.put(proposalId, proposal);
        } else {
            throw new SQLException("Failed to create berry conversion proposal");
        }
    }
    
    public static void createNeedThresholdProposal(String proposer, double globalThresholdPercent, 
                                                  double personalThresholdPercent, int timeLimit) throws SQLException {
        // Use the DB method to create the proposal
        int proposalId = DatabaseManager.createNeedThresholdProposal(proposer, globalThresholdPercent, 
                                                                    personalThresholdPercent, timeLimit);

        if (proposalId != -1) {
            // Create the in-memory proposal object
            NeedThresholdProposal proposal = new NeedThresholdProposal(proposer, globalThresholdPercent, 
                                                                       personalThresholdPercent, timeLimit);
            proposal.setId(proposalId);
            // Update the in-memory map
            TrustSystem.needThresholdProposals.put(proposalId, proposal);
        } else {
            throw new SQLException("Failed to create need threshold proposal");
        }
    }

    public static void voteForProposal(String proposalType, int proposalId, User voter) throws SQLException {
        String username = voter.getUsername();
        switch (proposalType.toUpperCase()) {
            case "LEVEL":
                if (DatabaseManager.hasLevelProposalVote(proposalId, username)) {
                    DialogFactory.showError("You have already voted for this proposal.");
                    return;
                }
                DatabaseManager.addLevelProposalVote(proposalId, username);
                if (TrustSystem.levelProposals.containsKey(proposalId)) {
                    TrustSystem.levelProposals.get(proposalId).addVote(username);
                    TrustSystem.levelProposals.get(proposalId).setVotes(TrustSystem.levelProposals.get(proposalId).getVotes() + 1);
                }
                break;
            case "BERRY_EARNING":
                if (DatabaseManager.hasBerryEarningProposalVote(proposalId, username)) {
                    DialogFactory.showError("You have already voted for this proposal.");
                    return;
                }
                DatabaseManager.addBerryEarningProposalVote(proposalId, username);
                if (TrustSystem.berryEarningProposals.containsKey(proposalId)) {
                    TrustSystem.berryEarningProposals.get(proposalId).addVote(username);
                    TrustSystem.berryEarningProposals.get(proposalId).setVotes(TrustSystem.berryEarningProposals.get(proposalId).getVotes() + 1);
                }
                break;
            case "BERRY_VALIDITY":
                if (DatabaseManager.hasBerryValidityProposalVote(proposalId, username)) {
                    DialogFactory.showError("You have already voted for this proposal.");
                    return;
                }
                DatabaseManager.addBerryValidityProposalVote(proposalId, username);
                if (TrustSystem.berryValidityProposals.containsKey(proposalId)) {
                    TrustSystem.berryValidityProposals.get(proposalId).addVote(username);
                    TrustSystem.berryValidityProposals.get(proposalId).setVotes(TrustSystem.berryValidityProposals.get(proposalId).getVotes() + 1);
                }
                break;
            case "BERRY_CONVERSION":
                if (DatabaseManager.hasBerryConversionProposalVote(proposalId, username)) {
                    DialogFactory.showError("You have already voted for this proposal.");
                    return;
                }
                DatabaseManager.addBerryConversionProposalVote(proposalId, username);
                if (TrustSystem.berryConversionProposals.containsKey(proposalId)) {
                    TrustSystem.berryConversionProposals.get(proposalId).addVote(username);
                    TrustSystem.berryConversionProposals.get(proposalId).setVotes(TrustSystem.berryConversionProposals.get(proposalId).getVotes() + 1);
                }
                break;
            case "NEED_THRESHOLD":
                if (DatabaseManager.hasNeedThresholdProposalVote(proposalId, username)) {
                    DialogFactory.showError("You have already voted for this proposal.");
                    return;
                }
                DatabaseManager.addNeedThresholdProposalVote(proposalId, username);
                if (TrustSystem.needThresholdProposals.containsKey(proposalId)) {
                    TrustSystem.needThresholdProposals.get(proposalId).addVote(username);
                    TrustSystem.needThresholdProposals.get(proposalId).setVotes(TrustSystem.needThresholdProposals.get(proposalId).getVotes() + 1);
                }
                break;
            default:
                throw new SQLException("Unknown proposal type: " + proposalType);
        }
        DialogFactory.showInfo("Vote Recorded", "Your vote has been recorded.");
    }
    
    // Generic method to create proposals based on category
    public static void createProposal(String title, String description, String category, String proposer) throws SQLException {
        switch (category.toUpperCase()) {
            case "LEVEL":
                // Use default values for level proposals
                createLevelProposal(proposer, 30.0, 40.0);
                break;
            case "BERRY_EARNING":
                // Use default value for berry earning proposals
                createBerryEarningProposal(proposer, 10);
                break;
            case "BERRY_VALIDITY":
                // Use default value for berry validity proposals
                createBerryValidityProposal(proposer, 12);
                break;
            case "BERRY_CONVERSION":
                // Use default values for berry conversion proposals
                createBerryConversionProposal(proposer, 80.0, 6);
                break;
            case "NEED_THRESHOLD":
                // Use default values for need threshold proposals
                createNeedThresholdProposal(proposer, 60.0, 40.0, 12);
                break;
            default:
                throw new SQLException("Unknown proposal category: " + category);
        }
    }    // Method to vote on proposal with boolean support parameter
    public static void voteOnProposal(int proposalId, String username, boolean support) throws SQLException {
        User voter = DatabaseManager.getUser(username);
        if (voter == null) {
            throw new SQLException("User not found: " + username);
        }
        
        // Determine proposal type by checking which map contains the proposal
        String proposalType = null;
        if (TrustSystem.levelProposals.containsKey(proposalId)) {
            proposalType = "LEVEL";
        } else if (TrustSystem.berryEarningProposals.containsKey(proposalId)) {
            proposalType = "BERRY_EARNING";
        } else if (TrustSystem.berryValidityProposals.containsKey(proposalId)) {
            proposalType = "BERRY_VALIDITY";
        } else if (TrustSystem.berryConversionProposals.containsKey(proposalId)) {
            proposalType = "BERRY_CONVERSION";
        } else if (TrustSystem.needThresholdProposals.containsKey(proposalId)) {
            proposalType = "NEED_THRESHOLD";
        } else {
            throw new SQLException("Proposal not found: " + proposalId);
        }
        
        // Only count support votes for now (could be extended to handle opposition)
        if (support) {
            voteForProposal(proposalType, proposalId, voter);
        }
    }
}
