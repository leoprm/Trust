/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.trust;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.sql.SQLException;
import java.io.Serializable;

/**
 *
 * @author leo
 */
public class Branch implements Serializable {


    public enum Phase {
        GENERATION,
        INVESTIGATION,
        DEVELOPMENT,
        PRODUCTION,
        DISTRIBUTION,
        MAINTENANCE,
        RECYCLING,
        COMPLETED
    }
    private static final long serialVersionUID = 1L;
    private static int idCounter;
    private int id;
    private Phase currentPhase;
    private String description;
    private int ideaId;
    private Set<Integer> needs;
    private ArrayList<String> candidates;
    private int teamOpenings;
    // Map to track expertise requirements per opening: Key = expertise ID, Value = number of openings required for that expertise
    // A special key of -1 represents "no expertise requirement" (general openings)
    private Map<Integer, Integer> expertiseRequirements = new HashMap<>();
    private IdeaGenerationPhase generation;
    private InvestigationPhase investigation;
    private DevelopmentPhase development;
    private ProductionPhase production;
    private DistributionPhase distribution;
    private MaintenancePhase maintenance;
    private RecyclingPhase recycling;
    private CompletedPhase completed;
    private String name;
    private int parentId;
    private Set<Integer> children;
    private Map<Phase, ArrayList<String>> phaseTeams = new HashMap<>();
    
    public int getParentId() {
        return parentId;
    }
    public void setParentId(int parentId) {
        this.parentId = parentId;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getId()
    {
        return id;
    }
    public void setId(int id)
    {
        this.id = id;
    }
    public Phase getCurrentPhase()
    {
        return currentPhase;
    }
    public void setCurrentPhase(Phase phase)
    {
        this.currentPhase = phase;
    }
    public String getDescription()
    {
        return description;
    }
    public void setDescription(String description)
    {
        this.description = description;
    }
    public int getIdeaId()
    {
        return ideaId;
    }
    public void setIdeaId(int ideaId)
    {
        this.ideaId = ideaId;
    }
    public ArrayList<String> getCandidates() {
        return candidates;
    }
    public void setCandidates(ArrayList<String> aCandidates) {
        candidates = aCandidates;
    }
    public int getTeamOpenings() {
        return teamOpenings;
    }
    public void setTeamOpenings(int teamOpenings) throws SQLException {
        this.teamOpenings = teamOpenings;
        
        // Update in database
        DatabaseManager.updateBranchTeamOpenings(this.id, teamOpenings);
        
        // Update the current phase object if it exists
        PhaseBase currentPhaseObj = getCurrentPhaseObject();
        if (currentPhaseObj != null) {
            // Set the team openings on the phase object
            currentPhaseObj.setTeamOpenings(teamOpenings);
            // Save the updated phase object to the database
            this.saveCurrentPhase();
        }
    }
    public IdeaGenerationPhase getGeneration()
    {
        return generation;
    }
    public void setGeneration(IdeaGenerationPhase generation)
    {
        this.generation = generation;
    }
    public InvestigationPhase GetInvestigarion()
    {
        return investigation;
    }
    public void setInvestigation(InvestigationPhase investigation)
    {
        this.investigation = investigation;
    }
    public DevelopmentPhase GetDevelopment()
    {
        return development;
    }
    public void setDevelopment(DevelopmentPhase development)
    {
        this.development = development;
    }
    public ProductionPhase getProduction()
    {
        return production;
    }
    public void setProduction(ProductionPhase production)
    {
        this.production = production;
    }
    public DistributionPhase getDistribution()
    {
        return distribution;
    }
    public void setDistribution(DistributionPhase distribution)
    {
        this.distribution = distribution;
    }
    public MaintenancePhase getMaintenance()
    {
        return maintenance;
    }
    public void setMaintenace(MaintenancePhase maintenance)
    {
        this.maintenance = maintenance;
    }
    public RecyclingPhase getRecycling()
    {
        return recycling;
    }
    public void setRecycling(RecyclingPhase recycling)
    {
        this.recycling = recycling;
    }
    public CompletedPhase getCompleted() {
        return completed;
    }
    public void setCompleted(CompletedPhase completed) {
        this.completed = completed;
    }

    /**
     * @return the needs
     */
    public Set<Integer> getNeeds() {
        return needs;
    }

    /**
     * @param needs the needs to set
     */
    public void setNeeds(Set<Integer> needs) {
        this.needs = needs;
    }

    @Override
    public String toString() {
        if (name == null || name.trim().isEmpty()) {
            System.out.println("Warning: Branch #" + id + " has null or empty name");
            return "Branch #" + id;
        }
        return name;
    }
    
    public Branch()
    {
        this.id = ++idCounter;
        this.currentPhase = Phase.GENERATION;
        this.candidates = new ArrayList<>();
        this.needs = new HashSet<>();
        this.children = new HashSet<>();
        this.phaseTeams = new HashMap<>();
    }
    public Branch(String name, String description, int parentId) {
        this.id = ++idCounter;
        this.description = description;
        this.parentId = parentId;
        this.currentPhase = Phase.GENERATION;
        this.candidates = new ArrayList<>();
        this.needs = new HashSet<>();
        this.name = name;
        this.children = new HashSet<>();
        this.phaseTeams = new HashMap<>();
        System.out.println("New branch object created in memory: Name=\"" + this.name + "\" (ID not set yet)");
    }
    
    public Branch(int id, String name, String description, int parentId, int ideaId, Phase currentPhase) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.parentId = parentId;
        this.ideaId = ideaId;
        this.currentPhase = currentPhase;
        this.candidates = new ArrayList<>();
        this.needs = new HashSet<>();
        this.children = new HashSet<>();
        this.phaseTeams = new HashMap<>();
    }
    
    public void setPhaseTeams(Map<Phase, ArrayList<String>> loadedTeams) {
        this.phaseTeams = loadedTeams != null ? loadedTeams : new HashMap<>();
    }
    
    public void addSupporter(String supporter)
    {
        // This method is no longer used in the new implementation
    }
    
    public void addCandidate(String candidate) throws SQLException {
        if(teamOpening()) {
            if (this.candidates == null) this.candidates = new ArrayList<>();
            if (!this.candidates.contains(candidate)) {
                 this.candidates.add(candidate);
                 DatabaseManager.addBranchCandidate(this.id, candidate);
             } else {
                 System.out.println("Candidate " + candidate + " already applied.");
             }
        } else {
            System.out.println("Teams aren't open for applications in phase: " + this.currentPhase);
        }
    }
    
    public void advancePhase() throws SQLException {
        Phase completedPhaseEnum = this.currentPhase;
        ArrayList<String> teamToReward = new ArrayList<>(getTeam());

        if (completedPhaseEnum != Phase.GENERATION && completedPhaseEnum != Phase.COMPLETED) {
            if (!teamToReward.isEmpty()) {
                Idea associatedIdea = getIdea();
                if (associatedIdea != null) {
                    Set<Integer> associatedNeedIds = associatedIdea.getAssociatedNeedIds();
                    if (associatedNeedIds != null && !associatedNeedIds.isEmpty()) {
                        int totalNeedPoints = 0;
                        for (int needId : associatedNeedIds) {
                            Need need = TrustSystem.needs.get(needId);
                            if (need != null) {
                                totalNeedPoints += NeedService.calculateTotalPoints(need);
                            }
                        }
                        double baseXP = (double) totalNeedPoints / associatedNeedIds.size();
                        double averageRating = DatabaseManager.getAverageRatingForBranchPhase(this.id, completedPhaseEnum.name());
                        double ratingModifier = Math.max(0, averageRating / 100.0);
                        double xpPerMember = (baseXP * ratingModifier) / teamToReward.size();
                        int finalXpPerMember = (int) Math.round(xpPerMember);

                        if (finalXpPerMember > 0) {
                            System.out.println("[XP Award] Branch: " + this.id + ", Phase: " + completedPhaseEnum.name() + ", Points: " + totalNeedPoints + ", Rating: " + averageRating + "%, XP/Member: " + finalXpPerMember);
                            for (String username : teamToReward) {
                                User user = TrustSystem.users.get(username);
                                if (user != null) {
                                    try {
                                        TrustSystem.processUserXpGain(user, finalXpPerMember);
                                    } catch (SQLException e) {
                                        System.err.println("Error awarding XP to " + username + ": " + e.getMessage());
                                    }
                                } else {
                                     System.err.println("XP Award Error: User " + username + " not found.");
                                }
                            }
                        } else {
                             System.out.println("[XP Award] Branch: " + this.id + ", Phase: " + completedPhaseEnum.name() + " - No XP awarded (Calculated: " + finalXpPerMember + ")");
                        }
                    } else {
                         System.out.println("[XP Award] Branch: " + this.id + " - No associated needs found for idea " + associatedIdea.getId());
                    }
                } else {
                     System.out.println("[XP Award] Branch: " + this.id + " - No associated idea found.");
                }
            } else {
                System.out.println("[XP Award] Branch: " + this.id + ", Phase: " + completedPhaseEnum.name() + " - No team members to reward.");
            }
        }

        Phase nextPhase = null;
        Phase[] phases = Phase.values();
        for (int i = 0; i < phases.length - 1; i++) {
             if (phases[i] == this.currentPhase) {
                 nextPhase = phases[i+1];
                 break;
             }
         }

        if (nextPhase == null) {
            System.out.println("Branch " + this.id + " is already completed or cannot advance.");
            return;
        }

        this.currentPhase = nextPhase;
        System.out.println("Branch " + this.id + " advancing to phase: " + this.currentPhase);

        this.teamOpenings = 0;
        if (this.candidates != null) this.candidates.clear();

        DatabaseManager.updateBranch(this);
        DatabaseManager.deleteBranchCandidates(this.id);

        // --- BEGIN Notify Users with Required Expertise for NEW Phase ---
        Phase newPhase = this.currentPhase;
        if (newPhase != Phase.COMPLETED) { // No jobs needed for the completed state
            try {
                Set<Integer> requiredExpertiseIds = DatabaseManager.getExpertiseIdsForPhase(this.id, newPhase.name());
                System.out.println("Branch " + this.id + " entered phase " + newPhase.name() + ". Required expertise: " + requiredExpertiseIds);

                if (!requiredExpertiseIds.isEmpty()) {
                    // Iterate through all users to find matches
                    for (User user : TrustSystem.users.values()) {
                        Set<Integer> userCertifiedIds = user.getCertifiedExpertiseIds();
                        if (userCertifiedIds == null || userCertifiedIds.isEmpty()) {
                            continue; // Skip users with no certifications
                        }

                        // Find matching expertise IDs
                        Set<Integer> matchingIds = new HashSet<>(userCertifiedIds); // Copy user's set
                        matchingIds.retainAll(requiredExpertiseIds); // Keep only the intersection

                        if (!matchingIds.isEmpty()) {
                            // Create notification(s) for this user
                            for (int expertiseId : matchingIds) {
                                FieldOfExpertise foe = TrustSystem.fieldsOfExpertise.get(expertiseId);
                                String expertiseName = (foe != null) ? foe.getName() : "ID: " + expertiseId;
                                String message = "Job Alert: Expertise in '" + expertiseName + 
                                                 "' needed for Branch '" + this.getName() + 
                                                 "' - Phase: " + newPhase.name();
                                
                                Notification notification = new Notification(user.getUsername(), message, this.id);
                                try {
                                    DatabaseManager.saveNotification(notification);
                                    System.out.println("  -> Sent notification to " + user.getUsername() + " for expertise: " + expertiseName);
                                } catch (SQLException e) {
                                    System.err.println("Error saving notification for user " + user.getUsername() + ": " + e.getMessage());
                                }
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error retrieving required expertise or notifying users for Branch " + this.id + " Phase " + newPhase.name() + ": " + e.getMessage());
            } catch (Exception e) { // Catch other potential runtime exceptions
                 System.err.println("Unexpected error during expertise notification: " + e.getMessage());
                 e.printStackTrace();
            }
        }
        // --- END Notify Users ---

        // Perform actions specific to entering the new phase (if any)
        // e.g., reset team openings if needed for the new phase
        if (this.currentPhase == Phase.DEVELOPMENT) {
             // Maybe reset team openings?
             // setTeamOpenings(5); // Example
             // DatabaseManager.updateBranchTeamOpenings(this.id, 5);
        }
        
    }
    
    public void saveCurrentPhase() throws SQLException {
        PhaseBase phaseObj = getCurrentPhaseObject();
        if (phaseObj != null) {
            System.out.println("Saving phase object state for branch " + this.id + " phase " + this.currentPhase);
            DatabaseManager.savePhase(this.id, this.currentPhase, phaseObj);
        } else {
             System.out.println("No phase object found for branch " + this.id + " phase " + this.currentPhase + " to save.");
        }
    }
    
    public boolean teamOpening() {
       return this.currentPhase == Phase.INVESTIGATION ||
              this.currentPhase == Phase.DEVELOPMENT ||
              this.currentPhase == Phase.PRODUCTION ||
              this.currentPhase == Phase.DISTRIBUTION ||
              this.currentPhase == Phase.MAINTENANCE ||
              this.currentPhase == Phase.RECYCLING;
    }
    
    public void printState() {
        System.out.println(this.toString());
        PhaseBase phaseObj = getCurrentPhaseObject();
        if (phaseObj != null) {
             phaseObj.displayPhaseInfo();
        } else {
             System.out.println("No phase object details available for current phase: " + this.currentPhase);
        }
    }
    
    public void addTeamMember(User user) {
        if (user == null) return;
        String username = user.getUsername();

        ArrayList<String> currentPhaseTeam = this.phaseTeams.computeIfAbsent(this.currentPhase, k -> new ArrayList<>());

        if (!currentPhaseTeam.contains(username)) {
            currentPhaseTeam.add(username);
            System.out.println("User " + username + " added to phase " + this.currentPhase + " team map for branch " + this.id);

            try {
                DatabaseManager.addBranchTeamMember(this.id, username, this.currentPhase.name());
                System.out.println("User " + username + " added to branch_team table for phase " + this.currentPhase.name());
            } catch (SQLException e) {
                System.err.println("Error saving team member " + username + " for phase " + this.currentPhase + " to database: " + e.getMessage());
                currentPhaseTeam.remove(username);
            }
        } else {
             System.out.println("User " + username + " already in team map for phase " + this.currentPhase);
        }
    }

    public ArrayList<String> getTeam() {
        return this.phaseTeams.computeIfAbsent(this.currentPhase, k -> new ArrayList<>());
    }

    public void setTeam(ArrayList<String> team) {
         if (team == null) {
             team = new ArrayList<>();
         }
         this.phaseTeams.put(this.currentPhase, team);
    }
    
    public void addNeed(int need)
    {
        needs.add(need);
    }
    
    public void candidateSelector() throws SQLException {
        if (candidates == null || candidates.isEmpty()) {
             System.out.println("No candidates to select for branch " + id);
             return;
         }
        if (teamOpenings <= 0) {
             System.out.println("No team openings available for branch " + id);
             return;
         }

        ArrayList<String> selectedCandidates = new ArrayList<>();
        ArrayList<String> remainingCandidates = new ArrayList<>(this.candidates);
        int openingsToFill = Math.min(this.teamOpenings, remainingCandidates.size());

        System.out.println("Selecting " + openingsToFill + " members from " + remainingCandidates.size() + " candidates for branch " + id);

        Collections.shuffle(remainingCandidates);
        for (int i = 0; i < openingsToFill; i++) {
            selectedCandidates.add(remainingCandidates.get(i));
        }

        System.out.println("Selected candidates: " + selectedCandidates);

        this.teamOpenings -= selectedCandidates.size();
        DatabaseManager.updateBranchTeamOpenings(this.id, this.teamOpenings);

        this.candidates.removeAll(selectedCandidates);
        DatabaseManager.deleteBranchSelectedCandidates(this.id, selectedCandidates);

        // Add selected candidates to the team (map and DB table)
        for (String selectedUsername : selectedCandidates) {
            User selectedUser = TrustSystem.users.get(selectedUsername);
            if (selectedUser != null) {
                addTeamMember(selectedUser);
            } else {
                 System.out.println("Warning: Selected candidate user " + selectedUsername + " not found in TrustSystem.");
            }
        }
         System.out.println("Team selection complete for branch " + id + ". Remaining openings: " + this.teamOpenings);
    }

    public void addChild(int childId) {
        if (children == null) { children = new HashSet<>(); }
        children.add(childId);
    }
    
    public Set<Integer> getChildren() {
        return children != null ? children : new HashSet<>();
    }
    
    public Idea getIdea() {
        if (ideaId > 0 && TrustSystem.ideas.containsKey(ideaId)) {
            return TrustSystem.ideas.get(ideaId);
        }
        return null;
    }

    private PhaseBase getCurrentPhaseObject() {
        return switch (this.currentPhase) {
            case GENERATION -> getGeneration();
            case INVESTIGATION -> GetInvestigarion();
            case DEVELOPMENT -> GetDevelopment();
            case PRODUCTION -> getProduction();
            case DISTRIBUTION -> getDistribution();
            case MAINTENANCE -> getMaintenance();
            case RECYCLING -> getRecycling();
            case COMPLETED -> getCompleted();
        };
    }

    /**
     * Gets the expertise requirements for team openings in the current phase
     * @return Map of expertise ID to count of openings required (-1 represents "none")
     */
    public Map<Integer, Integer> getExpertiseRequirements() {
        if (this.expertiseRequirements == null) {
            this.expertiseRequirements = new HashMap<>();
        }
        return this.expertiseRequirements;
    }
    
    /**
     * Sets the expertise requirements for team openings
     * @param requirements Map where key is expertise ID (-1 for none) and value is number of openings
     */
    public void setExpertiseRequirements(Map<Integer, Integer> requirements) {
        this.expertiseRequirements = requirements != null ? 
            new HashMap<>(requirements) : new HashMap<>();
            
        // Recalculate total team openings based on the sum of all requirements
        int totalOpenings = 0;
        for (Integer count : this.expertiseRequirements.values()) {
            totalOpenings += count;
        }
        
        try {
            setTeamOpenings(totalOpenings);
        } catch (SQLException e) {
            System.err.println("Error updating team openings based on expertise requirements: " + e.getMessage());
        }
    }
    
    /**
     * Updates a specific expertise requirement
     * @param expertiseId Expertise ID (-1 for no specific expertise)
     * @param count Number of openings for this expertise
     * @throws SQLException If database update fails
     */
    public void updateExpertiseRequirement(int expertiseId, int count) throws SQLException {
        if (count <= 0) {
            // Remove the requirement if count is 0 or negative
            this.expertiseRequirements.remove(expertiseId);
        } else {
            this.expertiseRequirements.put(expertiseId, count);
        }
        
        // Save to database
        DatabaseManager.updateBranchExpertiseRequirements(this.id, this.currentPhase.name(), this.expertiseRequirements);
        
        // Update total team openings
        int totalOpenings = 0;
        for (Integer openingCount : this.expertiseRequirements.values()) {
            totalOpenings += openingCount;
        }
        setTeamOpenings(totalOpenings);
    }
    
    /**
     * Clears all expertise requirements
     * @throws SQLException If database update fails
     */
    public void clearExpertiseRequirements() throws SQLException {
        this.expertiseRequirements.clear();
        DatabaseManager.updateBranchExpertiseRequirements(this.id, this.currentPhase.name(), this.expertiseRequirements);
        setTeamOpenings(0);
    }
    
    /**
     * Load expertise requirements for the current phase from database
     * @throws SQLException If database access fails
     
    public void loadExpertiseRequirements() throws SQLException {
        this.expertiseRequirements = DatabaseManager.loadBranchExpertiseRequirements(
            this.id, this.currentPhase.name());
    }
    
    /**
     * Save expertise requirements for the current phase to database
     * @throws SQLException If database access fails
    
    public void saveExpertiseRequirements() throws SQLException {
        DatabaseManager.updateBranchExpertiseRequirements(
            this.id, this.currentPhase.name(), this.expertiseRequirements);
    }
    */
    /**
     * Loads expertise requirements from the database for the current phase
     * @throws SQLException If a database error occurs
     */
    public void loadExpertiseRequirements() throws SQLException {
        Map<Integer, Integer> dbRequirements = DatabaseManager.loadBranchExpertiseRequirements(
            this.id, this.currentPhase.name());
        
        this.expertiseRequirements = dbRequirements;
        
        // Update the total team openings count based on the loaded requirements
        int totalOpenings = 0;
        for (Integer count : this.expertiseRequirements.values()) {
            totalOpenings += count;
        }
        
        // Only update if different to avoid unnecessary database operations
        if (this.teamOpenings != totalOpenings) {
            this.teamOpenings = totalOpenings;
            // Update the database without calling setTeamOpenings to avoid circular calls
            DatabaseManager.updateBranchTeamOpenings(this.id, totalOpenings);
        }
        
        System.out.println("Loaded " + this.expertiseRequirements.size() + " expertise requirements for branch " + this.id + ", phase " + this.currentPhase);
    }

    /**
     * Saves the current expertise requirements to the database
     * @throws SQLException If a database error occurs
     */
    public void saveExpertiseRequirements() throws SQLException {
        DatabaseManager.updateBranchExpertiseRequirements(this.id, this.currentPhase.name(), this.expertiseRequirements);
        System.out.println("Saved " + this.expertiseRequirements.size() + " expertise requirements for branch " + this.id + ", phase " + this.currentPhase);
    }
}
