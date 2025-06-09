package com.mycompany.trust;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.*;
import java.util.Base64;
import java.util.List;
import java.sql.Timestamp;

public class DatabaseManager {
    
    // User operations
    public static void createUser(String username, String password) throws SQLException {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, PasswordHasher.hashPassword(password));
            pstmt.executeUpdate();
        }
    }
    
    public static User getUser(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                User user = new User(
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getInt("level"),
                    rs.getInt("xp"),
                    rs.getInt("points"),
                    rs.getInt("total_berries_earned")
                );
                try {
                    user.setDisplayName(rs.getString("display_name"));
                } catch (SQLException e) {
                    if (!e.getMessage().contains("display_name")) { throw e; }
                    System.out.println("Note: display_name column not found for user " + username);
                    user.setDisplayName(username);
                }
                return user;
            }
        }
        return null;
    }
    
    /**
     * Checks if a user exists in the database
     * @param username The username to check
     * @return true if user exists, false otherwise
     * @throws SQLException if database error occurs
     */
    public static boolean userExists(String username) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE username = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        }
    }
    
    public static void updateUser(User user) throws SQLException {
        String sql = "UPDATE users SET level = ?, xp = ?, points = ?, total_berries_earned = ?, display_name = ? WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, user.getLevel());
            pstmt.setInt(2, user.getXp());
            pstmt.setInt(3, user.getPoints());
            pstmt.setInt(4, user.getTotalBerriesEarned());
            pstmt.setString(5, user.getDisplayName());
            pstmt.setString(6, user.getUsername());
            pstmt.executeUpdate();
        }
    }

    /**
     * Updates only the display name for a specific user
     * @param username The username of the user to update
     * @param displayName The new display name
     * @throws SQLException if database error occurs
     */
    public static void updateUserDisplayName(String username, String displayName) throws SQLException {
        String sql = "UPDATE users SET display_name = ? WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, displayName);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        }
    }

    public static int saveUser(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password, display_name, level, xp, points, total_berries_earned) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword()); // Assuming password in User object is already hashed
            pstmt.setString(3, user.getDisplayName());
            pstmt.setInt(4, user.getLevel());
            pstmt.setInt(5, user.getXp());
            pstmt.setInt(6, user.getPoints());
            pstmt.setInt(7, user.getTotalBerriesEarned());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1); // Return the generated user ID
                    }
                }
            }
            return -1; // Return -1 if save failed or ID not retrieved
        } catch (SQLException e) {
            // Log error or handle more gracefully
            System.err.println("Error saving user " + user.getUsername() + ": " + e.getMessage());
            // Check for duplicate username specifically if your DB throws a specific error code
            // For example, MySQL error code for duplicate entry is 1062
            if (e.getErrorCode() == 1062 || (e.getMessage() != null && e.getMessage().toLowerCase().contains("duplicate entry"))) {
                // Optionally, you could throw a custom exception or return a specific code for duplicate user
                System.err.println("Attempted to save a user with a duplicate username: " + user.getUsername());
            }
            return -1; // Return -1 on SQL exception
        }
    }
    
    // Need operations
    public static int createNeed(String name) throws SQLException {
        String sql = "INSERT INTO needs (name) VALUES (?)";
        int needId = -1;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                needId = rs.getInt(1);
            }
        }
        return needId;
    }
    
    public static void addNeedSupporter(int needId, String username, int points) throws SQLException {
        // First check if the user already has points allocated to this need
        String checkSql = "SELECT points FROM need_supporters WHERE need_id = ? AND username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
            pstmt.setInt(1, needId);
            pstmt.setString(2, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                // User already has points, update the existing entry
                int currentPoints = rs.getInt("points");
                String updateSql = "UPDATE need_supporters SET points = ? WHERE need_id = ? AND username = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setInt(1, currentPoints + points);
                    updateStmt.setInt(2, needId);
                    updateStmt.setString(3, username);
                    updateStmt.executeUpdate();
                }
            } else {
                // User doesn't have points yet, create new entry
                String insertSql = "INSERT INTO need_supporters (need_id, username, points) VALUES (?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setInt(1, needId);
                    insertStmt.setString(2, username);
                    insertStmt.setInt(3, points);
                    insertStmt.executeUpdate();
                }
            }
        }
    }
    
    public static void addNeedAffectedUser(int needId, String username, String location) throws SQLException {
        String sql = "INSERT INTO need_affected (need_id, username, location) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, needId);
            pstmt.setString(2, username);
            pstmt.setString(3, location);
            pstmt.executeUpdate();
        }
    }

    public static Idea getIdea(int ideaId) throws SQLException {
        String sql = "SELECT id, name, description, author, vote_count, status FROM ideas WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, ideaId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Idea idea = new Idea(
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("author")
                    );
                    idea.setId(rs.getInt("id"));
                    idea.setVoteCount(rs.getInt("vote_count"));
                    idea.setStatus(rs.getString("status"));

                    // Load associated need IDs
                    String needSql = "SELECT need_id FROM idea_needs WHERE idea_id = ?";
                    try (PreparedStatement needPstmt = conn.prepareStatement(needSql)) {
                        needPstmt.setInt(1, ideaId);
                        try (ResultSet needRs = needPstmt.executeQuery()) {
                            Set<Integer> needIds = new HashSet<>();
                            while (needRs.next()) {
                                needIds.add(needRs.getInt("need_id"));
                            }
                            idea.setAssociatedNeedIds(needIds);
                        }
                    }

                    // Load associated branch IDs
                    String branchSql = "SELECT branch_id FROM branch_ideas WHERE idea_id = ?";
                    try (PreparedStatement branchPstmt = conn.prepareStatement(branchSql)) {
                        branchPstmt.setInt(1, ideaId);
                        try (ResultSet branchRs = branchPstmt.executeQuery()) {
                            Set<Integer> branchIds = new HashSet<>();
                            while (branchRs.next()) {
                                branchIds.add(branchRs.getInt("branch_id"));
                            }
                            idea.setBranches(branchIds);
                        }
                    }
                      // Load supporters
                    String supporterSql = "SELECT username FROM idea_supporters WHERE idea_id = ?";
                    try (PreparedStatement supporterPstmt = conn.prepareStatement(supporterSql)) {
                        supporterPstmt.setInt(1, ideaId);
                        try (ResultSet supporterRs = supporterPstmt.executeQuery()) {
                            Set<String> supporters = new HashSet<>();
                            while (supporterRs.next()) {
                                supporters.add(supporterRs.getString("username"));
                            }
                            idea.setSupporters(supporters);
                            // Sync vote count with actual supporter count
                            idea.setVoteCount(supporters.size());
                        }
                    }

                    return idea;
                }
            }
        }
        return null; // Return null if idea not found
    }
    
    // Idea operations
    public static int createIdea(String name, String description, String author) throws SQLException {
        String sql = "INSERT INTO ideas (name, description, author) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setString(3, author);
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return -1;
    }
    
    public static void addIdeaSupporter(int ideaId, String username) throws SQLException {
        String sql = "INSERT INTO idea_supporters (idea_id, username) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, ideaId);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        }
    }
      public static void updateIdea(Idea idea) throws SQLException {
        String sql = "UPDATE ideas SET vote_count = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idea.getVoteCount());
            pstmt.setInt(2, idea.getId());
            pstmt.executeUpdate();
        }
    }
    
    public static void updateIdeaStatus(int ideaId, String newStatus) throws SQLException {
        String sql = "UPDATE ideas SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, ideaId);
            pstmt.executeUpdate();
        }
    }
    
    // Branch operations
    public static void addBranchTeamMember(int branchId, String username, String phaseType) throws SQLException {
        String sql = "INSERT INTO branch_team (branch_id, username, phase_type) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, branchId);
            pstmt.setString(2, username);
            pstmt.setString(3, phaseType);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            String duplicateKeyErrorCode = "1062";
            if (e.getMessage() != null && e.getMessage().contains(duplicateKeyErrorCode)) {
                 System.out.println("Team member " + username + " for branch " + branchId + " phase " + phaseType + " already exists.");
            } else {
                throw e;
            }
        }
    }
    
    public static void addBranchCandidate(int branchId, String username) throws SQLException {
        String sql = "INSERT INTO branch_candidates (branch_id, username) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, branchId);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            String duplicateKeyErrorCode = "1062";
            if (e.getMessage() != null && e.getMessage().contains(duplicateKeyErrorCode)) {
                 System.out.println("Candidate " + username + " for branch " + branchId + " already exists.");
            } else {
                throw e;
            }
        }
    }
    
    public static void updateBranch(Branch branch) throws SQLException {
        String sql = "UPDATE branches SET current_phase = ?, team_openings = ?, name = ?, description = ?, parent_id = ?, idea_id = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, branch.getCurrentPhase().name());
            pstmt.setInt(2, branch.getTeamOpenings());
            pstmt.setString(3, branch.getName());
            pstmt.setString(4, branch.getDescription());
            pstmt.setInt(5, branch.getParentId());
            if (branch.getIdeaId() > 0) {
                 pstmt.setInt(6, branch.getIdeaId());
            } else {
                 pstmt.setNull(6, java.sql.Types.INTEGER);
            }
            pstmt.setInt(7, branch.getId());
            pstmt.executeUpdate();
        }
    }

    public static void deleteBranchCandidates(int branchId) throws SQLException {
        String sql = "DELETE FROM branch_candidates WHERE branch_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, branchId);
            pstmt.executeUpdate();
        }
    }

    public static void deleteBranchSelectedCandidates(int branchId, List<String> candidates) throws SQLException {
        if (candidates == null || candidates.isEmpty()) {
            return;
        }
        StringBuilder sqlBuilder = new StringBuilder("DELETE FROM branch_candidates WHERE branch_id = ? AND username IN (");
        for (int i = 0; i < candidates.size(); i++) {
            sqlBuilder.append("?");
            if (i < candidates.size() - 1) {
                sqlBuilder.append(",");
            }
        }
        sqlBuilder.append(")");
        
        String sql = sqlBuilder.toString();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, branchId);
            for (int i = 0; i < candidates.size(); i++) {
                pstmt.setString(i + 2, candidates.get(i));
            }
            pstmt.executeUpdate();
        }
    }
    
    // Load all data
    /* 
    public static void loadAllData() throws SQLException {
        loadUsers();
        loadNeeds();
        loadIdeas();
        loadBranches();
        loadBerriesFromDatabase();
        loadProposalsFromDatabase();
        loadFieldsOfExpertise();
        loadIdeaNeedAssociations();
        ensureGeneralExpertiseExists();
        System.out.println("All initial data loaded.");
    }

    public static void loadUsers() throws SQLException {
        TrustSystem.users = loadAllUsers();
    }

    private static void loadNeeds() throws SQLException {
        TrustSystem.needs = loadAllNeeds();
    }

    private static void loadIdeas() throws SQLException {
        TrustSystem.ideas = loadAllIdeas();
    }

    private static void loadBranches() throws SQLException {
        TrustSystem.branches = loadAllBranches();
    }
*/
    private static void loadBerriesFromDatabase() throws SQLException {
        TrustSystem.userBerries = loadAllBerries();
    }

    private static void loadProposalsFromDatabase() throws SQLException {
        TrustSystem.levelProposals = loadAllLevelProposals();
        TrustSystem.berryEarningProposals = loadAllBerryEarningProposals();
        TrustSystem.berryValidityProposals = loadAllBerryValidityProposals();
        TrustSystem.berryConversionProposals = loadAllBerryConversionProposals();
        TrustSystem.needThresholdProposals = loadAllNeedThresholdProposals();
        System.out.println("Loaded proposals.");
    }

    private static void loadFieldsOfExpertise() throws SQLException {
        TrustSystem.fieldsOfExpertise = loadAllFieldsOfExpertise();
        System.out.println("Loaded " + TrustSystem.fieldsOfExpertise.size() + " Fields of Expertise into TrustSystem.");
    }

    // private static void loadIdeaNeedAssociations() throws SQLException {
    //     String sql = "SELECT idea_id, need_id FROM idea_needs";
    //     try (Connection conn = DatabaseConnection.getConnection();
    //          PreparedStatement pstmt = conn.prepareStatement(sql);
    //          ResultSet rs = pstmt.executeQuery()) {
            
    //         while (rs.next()) {
    //             int ideaId = rs.getInt("idea_id");
    //             int needId = rs.getInt("need_id");
                
    //             if (TrustSystem.ideas.containsKey(ideaId)) {
    //                 TrustSystem.ideas.get(ideaId).addAssociatedNeedId(needId);
    //             }
    //         }
    //     }
    //     System.out.println("Loaded idea-need associations.");
    // }

    private static void loadPhasesForBranch(Branch branch) throws SQLException {
        for (Branch.Phase phaseEnum : Branch.Phase.values()) {
            Object phaseObjRaw = loadPhase(branch.getId(), phaseEnum);
            if (phaseObjRaw instanceof PhaseBase phaseBaseObj) {
                // Set the loaded phase object back into the branch
                switch (phaseEnum) {
                    case GENERATION: branch.setGeneration((IdeaGenerationPhase) phaseBaseObj); break;
                    case INVESTIGATION: branch.setInvestigation((InvestigationPhase) phaseBaseObj); break;
                    case DEVELOPMENT: branch.setDevelopment((DevelopmentPhase) phaseBaseObj); break;
                    case PRODUCTION: branch.setProduction((ProductionPhase) phaseBaseObj); break;
                    case DISTRIBUTION: branch.setDistribution((DistributionPhase) phaseBaseObj); break;
                    case MAINTENANCE: branch.setMaintenace((MaintenancePhase) phaseBaseObj); break;
                    case RECYCLING: branch.setRecycling((RecyclingPhase) phaseBaseObj); break;
                    case COMPLETED: branch.setCompleted((CompletedPhase) phaseBaseObj); break;
                }
            } else if (phaseObjRaw != null) {
                 System.err.println("Warning: Loaded phase object for branch " + branch.getId() + " phase " + phaseEnum + " is not an instance of PhaseBase.");
            }
            // If loadPhase returned null, a default instance was created already (or should be handled there)
        }
    }

    public static void updateBranchTeamOpenings(int branchId, int teamOpenings) throws SQLException {
        String sql = "UPDATE branches SET team_openings = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, teamOpenings);
            pstmt.setInt(2, branchId);
            pstmt.executeUpdate();
        }
    }

    public static void associateIdeaWithNeed(int ideaId, int needId) throws SQLException {
        String sql = "INSERT INTO idea_needs (idea_id, need_id) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, ideaId);
            pstmt.setInt(2, needId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            String duplicateKeyErrorCode = "1062";
            if (e.getMessage() != null && e.getMessage().contains(duplicateKeyErrorCode)) {
                 System.out.println("Association between idea " + ideaId + " and need " + needId + " already exists.");
            } else {
                throw e;
            }
        }
    }

    public static void savePhase(int branchId, Branch.Phase phase, Object phaseData) throws SQLException {
        String query = "INSERT INTO phases (branch_id, phase_type, phase_data) VALUES (?, ?, ?) " +
                       "ON DUPLICATE KEY UPDATE phase_data = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, branchId);
            stmt.setString(2, phase.name());
            
            String serializedData = serializeToBase64(phaseData);
            
            stmt.setString(3, serializedData);
            stmt.setString(4, serializedData);
            stmt.executeUpdate();
        }
    }

    public static Object loadPhase(int branchId, Branch.Phase phase) throws SQLException {
        String query = "SELECT phase_data FROM phases WHERE branch_id = ? AND phase_type = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, branchId);
            stmt.setString(2, phase.name());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String base64Data = rs.getString("phase_data");
                    if (base64Data != null && !base64Data.isEmpty()) {
                        return deserializeFromBase64(base64Data);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading phase " + phase + " for branch " + branchId + ": " + e.getMessage());
            throw e;
        }
        System.out.println("No saved phase data found for branch " + branchId + phase + ". Returning new instance.");
        try {
            return switch (phase) {
                case GENERATION -> new IdeaGenerationPhase();
                case INVESTIGATION -> new InvestigationPhase();
                case DEVELOPMENT -> new DevelopmentPhase();
                case PRODUCTION -> new ProductionPhase();
                case DISTRIBUTION -> new DistributionPhase();
                case MAINTENANCE -> new MaintenancePhase();
                case RECYCLING -> new RecyclingPhase();
                case COMPLETED -> new CompletedPhase();
            };
        } catch (Exception e) {
             System.err.println("Error creating default instance for phase " + phase + ": " + e.getMessage());
             return null;
        }
    }

    private static String serializeToBase64(Object obj) throws SQLException {
        if (obj == null) return null;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new SQLException("Serialization error: " + e.getMessage(), e);
        }
    }

    private static Object deserializeFromBase64(String base64) throws SQLException {
         if (base64 == null || base64.isEmpty()) return null;
        try {
            byte[] data = Base64.getDecoder().decode(base64);
            try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
                 ObjectInputStream ois = new ObjectInputStream(bais)) {
                return ois.readObject();
            }
        } catch (IOException | ClassNotFoundException | IllegalArgumentException e) {
            System.err.println("Deserialization error for data starting with: " + base64.substring(0, Math.min(50, base64.length())));
            throw new SQLException("Deserialization error: " + e.getMessage(), e);
        }
    }

    public static void deletePhases(int branchId) throws SQLException {
        String sql = "DELETE FROM phases WHERE branch_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, branchId);
            pstmt.executeUpdate();
        }
    }

    public static void initializeBranch(int branchId) throws SQLException {
        boolean phasesExist = false;
        String checkSql = "SELECT 1 FROM phases WHERE branch_id = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, branchId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                phasesExist = rs.next();
            }
        }

        if (!phasesExist) {
            try {
                savePhase(branchId, Branch.Phase.GENERATION, new IdeaGenerationPhase());
                savePhase(branchId, Branch.Phase.INVESTIGATION, new InvestigationPhase());
                savePhase(branchId, Branch.Phase.DEVELOPMENT, new DevelopmentPhase());
                savePhase(branchId, Branch.Phase.PRODUCTION, new ProductionPhase());
                savePhase(branchId, Branch.Phase.DISTRIBUTION, new DistributionPhase());
                savePhase(branchId, Branch.Phase.MAINTENANCE, new MaintenancePhase());
                savePhase(branchId, Branch.Phase.RECYCLING, new RecyclingPhase());
                savePhase(branchId, Branch.Phase.COMPLETED, new CompletedPhase());
            } catch (SQLException e) {
                System.err.println("Error initializing phases for branch " + branchId + ": " + e.getMessage());
            }
        } else {
             System.out.println("Phases already exist for branch " + branchId + ". Skipping initialization.");
        }
    }

    public static Map<String, User> loadAllUsers() throws SQLException {
        Map<String, User> users = new HashMap<>();
        String sql = "SELECT * FROM users";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                 User user = new User(
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getInt("level"),
                    rs.getInt("xp"),
                    rs.getInt("points"),
                    rs.getInt("total_berries_earned")
                );
                 try {
                    user.setDisplayName(rs.getString("display_name"));
                 } catch (SQLException e) {
                      if (!e.getMessage().contains("display_name")) { throw e; }
                     System.out.println("Note: display_name column not found for user " + user.getUsername() + " during bulk load.");
                     user.setDisplayName(user.getUsername());
                 }                 // Load expertise for this user
                 user.setCertifiedExpertiseIds(getExpertiseIdsForUser(user.getUsername()));
                 users.put(user.getUsername(), user);
            }
        }
        return users;
    }

    /**
     * Retrieves a single Need from the database by ID with all associated data
     * @param needId The ID of the need to retrieve
     * @return Need object with supporters and affected users loaded, or null if not found
     * @throws SQLException if database error occurs
     */
    public static Need getNeed(int needId) throws SQLException {
        Need need = null;
        String needSql = "SELECT * FROM needs WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(needSql)) {
            pstmt.setInt(1, needId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                need = new Need(rs.getString("name"));
                need.setId(rs.getInt("id"));
            }
        }
        
        if (need == null) {
            return null; // Need not found
        }
        
        // Load supporters for this need
        String supporterSql = "SELECT * FROM need_supporters WHERE need_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(supporterSql)) {
            pstmt.setInt(1, needId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String username = rs.getString("username");
                int points = rs.getInt("points");
                need.addSupporter(username, points);
            }
        }
        
        // Load affected users for this need
        String affectedSql = "SELECT * FROM need_affected WHERE need_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(affectedSql)) {
            pstmt.setInt(1, needId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String username = rs.getString("username");
                String location = rs.getString("location");
                need.addAffectedUser(username, location);
            }
        }
        
        return need;
    }

    public static Map<Integer, Need> loadAllNeeds() throws SQLException {
        Map<Integer, Need> needs = new HashMap<>();
        String needSql = "SELECT * FROM needs";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(needSql)) {
            while (rs.next()) {
                 Need need = new Need(rs.getString("name"));
                 need.setId(rs.getInt("id"));
                 needs.put(need.getId(), need);
            }
        }
        
        String needSupporterSql = "SELECT * FROM need_supporters";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(needSupporterSql)) {
            while (rs.next()) {
                int needId = rs.getInt("need_id");
                String username = rs.getString("username");
                int points = rs.getInt("points");
                if (needs.containsKey(needId)) {
                    needs.get(needId).addSupporter(username, points);
                }
            }
        }
        
        String needAffectedSql = "SELECT * FROM need_affected";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(needAffectedSql)) {
            while (rs.next()) {
                int needId = rs.getInt("need_id");
                String username = rs.getString("username");
                String location = rs.getString("location");
                if (needs.containsKey(needId)) {
                    needs.get(needId).addAffectedUser(username, location);
                }
            }
        }
        return needs;
    }

    public static Map<Integer, Idea> loadAllIdeas() throws SQLException {
        Map<Integer, Idea> ideas = new HashMap<>();
        String ideaSql = "SELECT * FROM ideas";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(ideaSql)) {
            while (rs.next()) {
                 Idea idea = new Idea(
                     rs.getString("name"),
                     rs.getString("description"),
                     rs.getString("author")
                 );
                 idea.setId(rs.getInt("id"));
                 idea.setVoteCount(rs.getInt("vote_count"));
                 idea.setStatus(rs.getString("status"));
                 ideas.put(idea.getId(), idea);
            }
        }
          String ideaSupporterSql = "SELECT * FROM idea_supporters";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(ideaSupporterSql)) {
            while (rs.next()) {
                int ideaId = rs.getInt("idea_id");
                String username = rs.getString("username");
                if (ideas.containsKey(ideaId)) {
                    ideas.get(ideaId).addSupporter(username);
                }
            }
        }
        
        // Sync vote counts with actual supporter counts
        for (Idea idea : ideas.values()) {
            idea.setVoteCount(idea.getSupporters().size());
        }
        
        String ideaNeedSql = "SELECT idea_id, need_id FROM idea_needs";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(ideaNeedSql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                int ideaId = rs.getInt("idea_id");
                int needId = rs.getInt("need_id");
                if (ideas.containsKey(ideaId)) {
                    ideas.get(ideaId).addAssociatedNeedId(needId);
                }
            }
        }

        String branchesQuery = "SELECT branch_id, idea_id FROM branch_ideas";
         try (Connection conn = DatabaseConnection.getConnection();
              PreparedStatement stmt = conn.prepareStatement(branchesQuery);
              ResultSet rs = stmt.executeQuery()) {
             while (rs.next()) {
                 int branchId = rs.getInt("branch_id");
                 int ideaId = rs.getInt("idea_id");
                 if (ideas.containsKey(ideaId)) {
                     ideas.get(ideaId).addBranch(branchId);
                 }
             }
         }
        return ideas;
    }

    public static Map<Integer, Branch> loadAllBranches() throws SQLException {
         Map<Integer, Branch> branches = new HashMap<>();
         String branchSql = "SELECT * FROM branches";
         try (Connection conn = DatabaseConnection.getConnection();
              Statement stmt = conn.createStatement();
              ResultSet rs = stmt.executeQuery(branchSql)) {
             while (rs.next()) {
                 Branch branch = new Branch(
                     rs.getInt("id"),
                     rs.getString("name"),
                     rs.getString("description"),
                     rs.getInt("parent_id"),
                     rs.getInt("idea_id"),
                     Branch.Phase.valueOf(rs.getString("current_phase"))
                 );
                 branch.setTeamOpenings(rs.getInt("team_openings"));
                 branches.put(branch.getId(), branch);
             }
         }

         String teamSql = "SELECT branch_id, username, phase_type FROM branch_team";
         Map<Integer, Map<Branch.Phase, ArrayList<String>>> allPhaseTeams = new HashMap<>();

         try (Connection conn = DatabaseConnection.getConnection();
              Statement stmt = conn.createStatement();
              ResultSet rs = stmt.executeQuery(teamSql)) {
             while (rs.next()) {
                 int branchId = rs.getInt("branch_id");
                 String username = rs.getString("username");
                 try {
                     Branch.Phase phase = Branch.Phase.valueOf(rs.getString("phase_type"));

                     Map<Branch.Phase, ArrayList<String>> branchPhaseMap =
                         allPhaseTeams.computeIfAbsent(branchId, _ -> new HashMap<>());

                     ArrayList<String> teamList =
                         branchPhaseMap.computeIfAbsent(phase, _ -> new ArrayList<>());

                     if (!teamList.contains(username)) {
                         teamList.add(username);
                     }
                 } catch (IllegalArgumentException e) {
                     System.err.println("Warning: Invalid phase_type '" + rs.getString("phase_type") + "' found in branch_team table for branch " + branchId);
                 }
             }
         }

         for (Map.Entry<Integer, Branch> entry : branches.entrySet()) {
             int branchId = entry.getKey();
             Branch branch = entry.getValue();
             Map<Branch.Phase, ArrayList<String>> loadedTeams = allPhaseTeams.get(branchId);
             branch.setPhaseTeams(loadedTeams);
         }

         String branchCandidateSql = "SELECT branch_id, username FROM branch_candidates";
         try (Connection conn = DatabaseConnection.getConnection();
              Statement stmt = conn.createStatement();
              ResultSet rs = stmt.executeQuery(branchCandidateSql)) {
             while (rs.next()) {
                 int branchId = rs.getInt("branch_id");
                 String username = rs.getString("username");
                 if (branches.containsKey(branchId)) {
                     Branch branch = branches.get(branchId);
                     branch.getCandidates().add(username);
                 }
             }
         }

         for (Branch branch : branches.values()) {
             if (branch.getParentId() != 0 && branches.containsKey(branch.getParentId())) {
                 branches.get(branch.getParentId()).addChild(branch.getId());
             }
         }

         for (Branch branch : branches.values()) {
             loadPhasesForBranch(branch);
         }

         return branches;
    }

    public static Map<String, List<Berry>> loadAllBerries() throws SQLException {
        Map<String, List<Berry>> userBerries = new HashMap<>();
        String sql = "SELECT * FROM berries WHERE expired = false";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String username = rs.getString("username");
                LocalDateTime expiration = null;
                Timestamp ts = rs.getTimestamp("expiration_date");
                if (ts != null) {
                    expiration = ts.toLocalDateTime();
                } else {
                     System.out.println("Warning: Null expiration date found for berry ID " + rs.getInt("id"));
                    expiration = LocalDateTime.now().plusYears(100);
                }

                Berry berry = new Berry(
                    username,
                    rs.getInt("amount"),
                    rs.getString("source"),
                    expiration
                );
                berry.setId(rs.getInt("id"));
                berry.setExpired(rs.getBoolean("expired"));
                userBerries.computeIfAbsent(username, _ -> new ArrayList<>()).add(berry);
            }
        }
        return userBerries;
    }

    public static <T extends Proposal> Map<Integer, T> loadAllProposals(String type, Class<T> clazz) throws SQLException {
        System.out.println("Warning: Generic loadAllProposals called. Consider using specific loaders.");
        return new HashMap<>();
    }

    public static Map<Integer, LevelProposal> loadAllLevelProposals() throws SQLException {
        Map<Integer, LevelProposal> proposals = new HashMap<>();
        String sql = "SELECT * FROM level_proposals";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                LevelProposal proposal = new LevelProposal(
                    rs.getString("proposer_username"),
                    rs.getDouble("xp_increase_percentage"),
                    rs.getDouble("xp_threshold")
                );
                proposal.setId(rs.getInt("id"));
                proposal.setVotes(rs.getInt("votes"));
                proposals.put(proposal.getId(), proposal);
            }
        }
        return proposals;
    }

    public static Map<Integer, BerryEarningProposal> loadAllBerryEarningProposals() throws SQLException {
        Map<Integer, BerryEarningProposal> proposals = new HashMap<>();
        String sql = "SELECT * FROM berry_earning_proposals";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                BerryEarningProposal proposal = new BerryEarningProposal(
                    rs.getString("proposer_username"),
                    rs.getInt("initial_level_one_berry_earning")
                );
                proposal.setId(rs.getInt("id"));
                proposal.setVotes(rs.getInt("votes"));
                proposals.put(proposal.getId(), proposal);
            }
        }
        return proposals;
    }    public static Map<Integer, BerryValidityProposal> loadAllBerryValidityProposals() throws SQLException {
        Map<Integer, BerryValidityProposal> proposals = new HashMap<>();
        String sql = "SELECT * FROM berry_validity_proposals";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                BerryValidityProposal proposal = new BerryValidityProposal(
                    rs.getString("proposer_username"),
                    rs.getInt("months")
                );
                proposal.setId(rs.getInt("id"));
                proposal.setVotes(rs.getInt("votes"));
                proposals.put(proposal.getId(), proposal);
            }
        }
        return proposals;
    }

    public static Map<Integer, BerryConversionProposal> loadAllBerryConversionProposals() throws SQLException {
        Map<Integer, BerryConversionProposal> proposals = new HashMap<>();
        String sql = "SELECT * FROM berry_conversion_proposals";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                BerryConversionProposal proposal = new BerryConversionProposal(
                    rs.getInt("id"),
                    rs.getString("proposer_username"),
                    rs.getDouble("conversion_percentage"),
                    rs.getInt("conversion_period"),
                    rs.getInt("votes")
                );
                proposals.put(proposal.getId(), proposal);
            }
        }
        return proposals;
    }

    public static Map<Integer, NeedThresholdProposal> loadAllNeedThresholdProposals() throws SQLException {
        Map<Integer, NeedThresholdProposal> proposals = new HashMap<>();
        String sql = "SELECT * FROM need_threshold_proposals";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                NeedThresholdProposal proposal = new NeedThresholdProposal(
                    rs.getInt("id"),
                    rs.getString("proposer_username"),
                    rs.getDouble("global_threshold_percent"),
                    rs.getDouble("personal_threshold_percent"),
                    rs.getInt("time_limit_months"),
                    rs.getInt("votes")
                );
                proposals.put(proposal.getId(), proposal);
            }
        }
        return proposals;
    }

    // Proposal creation methods
    public static int createLevelProposal(String proposer, double xpIncrease, double xpThreshold) throws SQLException {
        String sql = "INSERT INTO level_proposals (proposer_username, xp_increase_percentage, xp_threshold, votes) VALUES (?, ?, ?, 0)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, proposer);
            pstmt.setDouble(2, xpIncrease);
            pstmt.setDouble(3, xpThreshold);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1); // Return the generated proposal ID
                    }
                }
            }
            return -1; // Return -1 if save failed or ID not retrieved
        } catch (SQLException e) {
            System.err.println("Error creating level proposal: " + e.getMessage());
            throw e;
        }
    }

    public static int createBerryEarningProposal(String proposer, int initialEarning) throws SQLException {
        String sql = "INSERT INTO berry_earning_proposals (proposer_username, initial_level_one_berry_earning, votes) VALUES (?, ?, 0)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, proposer);
            pstmt.setInt(2, initialEarning);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1); // Return the generated proposal ID
                    }
                }
            }
            return -1; // Return -1 if save failed or ID not retrieved
        } catch (SQLException e) {
            System.err.println("Error creating berry earning proposal: " + e.getMessage());
            throw e;
        }
    }

    public static int createBerryValidityProposal(String proposer, int months) throws SQLException {
        String sql = "INSERT INTO berry_validity_proposals (proposer_username, months, votes) VALUES (?, ?, 0)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, proposer);
            pstmt.setInt(2, months);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1); // Return the generated proposal ID
                    }
                }
            }
            return -1; // Return -1 if save failed or ID not retrieved
        } catch (SQLException e) {
            System.err.println("Error creating berry validity proposal: " + e.getMessage());
            throw e;
        }
    }

    public static int createBerryConversionProposal(String proposer, double conversionPercentage, int conversionPeriod) throws SQLException {
        String sql = "INSERT INTO berry_conversion_proposals (proposer_username, conversion_percentage, conversion_period, votes) VALUES (?, ?, ?, 0)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, proposer);
            pstmt.setDouble(2, conversionPercentage);
            pstmt.setInt(3, conversionPeriod);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1); // Return the generated proposal ID
                    }
                }
            }
            return -1; // Return -1 if save failed or ID not retrieved
        } catch (SQLException e) {
            System.err.println("Error creating berry conversion proposal: " + e.getMessage());
            throw e;
        }
    }

    public static int createNeedThresholdProposal(String proposer, double globalThresholdPercent, 
                                                double personalThresholdPercent, int timeLimit) throws SQLException {
        String sql = "INSERT INTO need_threshold_proposals (proposer_username, global_threshold_percent, personal_threshold_percent, time_limit_months, votes) VALUES (?, ?, ?, ?, 0)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, proposer);
            pstmt.setDouble(2, globalThresholdPercent);
            pstmt.setDouble(3, personalThresholdPercent);
            pstmt.setInt(4, timeLimit);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1); // Return the generated proposal ID
                    }
                }
            }
            return -1; // Return -1 if save failed or ID not retrieved
        } catch (SQLException e) {
            System.err.println("Error creating need threshold proposal: " + e.getMessage());
            throw e;
        }    }

    // Proposal voting methods
    public static boolean hasLevelProposalVote(int proposalId, String username) throws SQLException {
        String sql = "SELECT 1 FROM level_proposal_votes WHERE proposal_id = ? AND username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, proposalId);
            pstmt.setString(2, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        }
    }

    public static void addLevelProposalVote(int proposalId, String username) throws SQLException {
        String sql = "INSERT INTO level_proposal_votes (proposal_id, username) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, proposalId);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        }
    }

    public static boolean hasBerryEarningProposalVote(int proposalId, String username) throws SQLException {
        String sql = "SELECT 1 FROM berry_earning_proposal_votes WHERE proposal_id = ? AND username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, proposalId);
            pstmt.setString(2, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        }
    }

    public static void addBerryEarningProposalVote(int proposalId, String username) throws SQLException {
        String sql = "INSERT INTO berry_earning_proposal_votes (proposal_id, username) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, proposalId);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        }
    }

    public static boolean hasBerryValidityProposalVote(int proposalId, String username) throws SQLException {
        String sql = "SELECT 1 FROM berry_validity_proposal_votes WHERE proposal_id = ? AND username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, proposalId);
            pstmt.setString(2, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        }
    }

    public static void addBerryValidityProposalVote(int proposalId, String username) throws SQLException {
        String sql = "INSERT INTO berry_validity_proposal_votes (proposal_id, username) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, proposalId);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        }
    }

    public static boolean hasBerryConversionProposalVote(int proposalId, String username) throws SQLException {
        String sql = "SELECT 1 FROM berry_conversion_proposal_votes WHERE proposal_id = ? AND username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, proposalId);
            pstmt.setString(2, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        }
    }

    public static void addBerryConversionProposalVote(int proposalId, String username) throws SQLException {
        String sql = "INSERT INTO berry_conversion_proposal_votes (proposal_id, username) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, proposalId);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        }
    }

    public static boolean hasNeedThresholdProposalVote(int proposalId, String username) throws SQLException {
        String sql = "SELECT 1 FROM need_threshold_proposal_votes WHERE proposal_id = ? AND username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, proposalId);
            pstmt.setString(2, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        }
    }

    public static void addNeedThresholdProposalVote(int proposalId, String username) throws SQLException {
        String sql = "INSERT INTO need_threshold_proposal_votes (proposal_id, username) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, proposalId);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        }
    }

    public static double getAverageRatingForBranchPhase(int branchId, String phaseName) throws SQLException {
        String sql = "SELECT AVG(rating_value) as average_rating FROM branch_phase_ratings WHERE branch_id = ? AND phase_type = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, branchId);
            pstmt.setString(2, phaseName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("average_rating");
            }
        }
        return 0.0; // Return 0.0 if no ratings found or in case of an error before query execution
    }

    public static Map<Integer, FieldOfExpertise> loadAllFieldsOfExpertise() throws SQLException {
        Map<Integer, FieldOfExpertise> expertiseMap = new HashMap<>();
        String sql = "SELECT id, name, description, parent_id FROM fields_of_expertise";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String description = rs.getString("description");
                Integer parentId = rs.getObject("parent_id", Integer.class); // Handles NULL parent_id
                FieldOfExpertise foe = new FieldOfExpertise(id, name, description, parentId);
                expertiseMap.put(id, foe);
            }
        }
        return expertiseMap;
    }

    public static List<User> getAllUsersList() throws SQLException {
        List<User> userList = new ArrayList<>();
        String sql = "SELECT id, username, password, display_name, level, xp, points, total_berries_earned FROM users";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                User user = new User(
                    rs.getString("username"),
                    rs.getString("password"), // Storing hashed password
                    rs.getInt("level"),
                    rs.getInt("xp"),
                    rs.getInt("points"),
                    rs.getInt("total_berries_earned")
                );
                user.setId(rs.getInt("id"));
                user.setDisplayName(rs.getString("display_name"));
                // Note: Berries and certified expertise are not loaded here for performance.
                // They should be loaded on-demand when a specific user's details are needed.
                userList.add(user);
            }
        }
        return userList;
    }

    public static int saveBerry(Berry berry) throws SQLException {
        String sql = "INSERT INTO berries (username, amount, source, expiration_date, expired) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, berry.getUsername());
            pstmt.setInt(2, berry.getAmount());
            pstmt.setString(3, berry.getSource());
            pstmt.setTimestamp(4, Timestamp.valueOf(berry.getExpirationDate()));
            pstmt.setBoolean(5, berry.isExpired());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1); // Return the generated berry ID
                    }
                }
            }
            return -1; // Return -1 if save failed or ID not retrieved
        } catch (SQLException e) {
            System.err.println("Error saving berry for user " + berry.getUsername() + ": " + e.getMessage());
            return -1; // Return -1 on SQL exception
        }
    }

    public static void markBerryExpired(int berryId) throws SQLException {
        String sql = "UPDATE berries SET expired = true WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, berryId);
            pstmt.executeUpdate();
        }
    }    public static Set<Integer> getExpertiseIdsForPhase(int branchId, String phaseName) throws SQLException {
        Set<Integer> expertiseIds = new HashSet<>();
        String sql = "SELECT expertise_id FROM branch_expertise_requirements WHERE branch_id = ? AND phase_name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, branchId);
            pstmt.setString(2, phaseName);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                expertiseIds.add(rs.getInt("expertise_id"));
            }
        }
        return expertiseIds;
    }

    public static Map<Integer, Integer> loadBranchExpertiseRequirements(int branchId, String phaseName) throws SQLException {
        Map<Integer, Integer> requirements = new HashMap<>();
        String sql = "SELECT expertise_id, openings_count FROM branch_expertise_requirements WHERE branch_id = ? AND phase_name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, branchId);
            pstmt.setString(2, phaseName);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int expertiseId = rs.getInt("expertise_id");
                int count = rs.getInt("openings_count");
                requirements.put(expertiseId, count);
            }
        }
        return requirements;
    }

    public static void updateBranchExpertiseRequirements(int branchId, String phaseName, Map<Integer, Integer> expertiseRequirements) throws SQLException {
        // First, delete existing requirements for this branch and phase
        String deleteSql = "DELETE FROM branch_expertise_requirements WHERE branch_id = ? AND phase_name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
            deleteStmt.setInt(1, branchId);
            deleteStmt.setString(2, phaseName);
            deleteStmt.executeUpdate();
        }
          // Then, insert the new requirements if any exist
        if (expertiseRequirements != null && !expertiseRequirements.isEmpty()) {
            String insertSql = "INSERT INTO branch_expertise_requirements (branch_id, phase_name, expertise_id, openings_count) VALUES (?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                for (Map.Entry<Integer, Integer> entry : expertiseRequirements.entrySet()) {
                    Integer expertiseId = entry.getKey();
                    Integer count = entry.getValue();
                    
                    // Only insert if count is positive
                    if (count != null && count > 0) {
                        insertStmt.setInt(1, branchId);
                        insertStmt.setString(2, phaseName);
                        insertStmt.setInt(3, expertiseId);
                        insertStmt.setInt(4, count);
                        insertStmt.executeUpdate();
                    }
                }
            }
        }
    }

    public static void saveNotification(Notification notification) throws SQLException {
        String sql = "INSERT INTO notifications (username, message, related_branch_id, is_read, timestamp) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, notification.getUsername());
            pstmt.setString(2, notification.getMessage());
            if (notification.getRelatedBranchId() != null && notification.getRelatedBranchId() > 0) {
                pstmt.setInt(3, notification.getRelatedBranchId());
            } else {
                pstmt.setNull(3, java.sql.Types.INTEGER);
            }
            pstmt.setBoolean(4, notification.isRead());
            pstmt.setTimestamp(5, notification.getTimestamp());
            pstmt.executeUpdate();
        }
    }

    public static List<Notification> getUserNotifications(String username) throws SQLException {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE username = ? ORDER BY timestamp DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    notifications.add(new Notification(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("message"),
                        rs.getTimestamp("timestamp"),
                        rs.getBoolean("is_read"),
                        rs.getObject("related_branch_id", Integer.class)
                    ));
                }
            }
        }
        return notifications;
    }

    public static List<Berry> getUserBerries(String username) throws SQLException {
        List<Berry> userBerries = new ArrayList<>();
        String sql = "SELECT * FROM berries WHERE username = ? ORDER BY expiration_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Berry berry = new Berry(
                        rs.getString("username"),
                        rs.getInt("amount"),
                        rs.getString("source"),
                        rs.getTimestamp("expiration_date").toLocalDateTime()
                    );
                    berry.setExpired(rs.getBoolean("expired"));
                    userBerries.add(berry);
                }
            }
        }
        return userBerries;
    }

    public static List<String> getUserBranchApplications(String username) throws SQLException {
        List<String> applications = new ArrayList<>();
        String sql = "SELECT b.name, bc.application_date FROM branch_candidates bc " +
                    "JOIN branches b ON bc.branch_id = b.id " +
                    "WHERE bc.username = ? ORDER BY bc.application_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String branchName = rs.getString("name");
                    String applicationDate = rs.getTimestamp("application_date").toString();
                    applications.add(branchName + " (Applied: " + applicationDate.substring(0, 10) + ")");
                }
            }
        }
        return applications;
    }

    public static void markNotificationAsRead(int notificationId) throws SQLException {
        String sql = "UPDATE notifications SET is_read = true WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, notificationId);
            pstmt.executeUpdate();
        }
    }    public static void markAllNotificationsAsRead(String username) throws SQLException {
        String sql = "UPDATE notifications SET is_read = true WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        }
    }

    public static Map<String, String> getUsersWithExpertise(int expertiseId) throws SQLException {
        Map<String, String> certifiedUsers = new HashMap<>();
        String sql = "SELECT u.username, ue.certification_date FROM user_expertise ue " +
                    "JOIN users u ON ue.username = u.username " +
                    "WHERE ue.expertise_id = ? ORDER BY ue.certification_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, expertiseId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String username = rs.getString("username");
                    String certificationDate = rs.getTimestamp("certification_date").toString().substring(0, 10);
                    certifiedUsers.put(username, certificationDate);
                }
            }
        }
        return certifiedUsers;
    }

    public static void addUserExpertise(String username, int expertiseId) throws SQLException {
        String sql = "INSERT INTO user_expertise (username, expertise_id, certification_date) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setInt(2, expertiseId);
            pstmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            pstmt.executeUpdate();
        }
    }    public static void removeUserExpertise(String username, int expertiseId) throws SQLException {
        String sql = "DELETE FROM user_expertise WHERE username = ? AND expertise_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setInt(2, expertiseId);
            pstmt.executeUpdate();
        }
    }    public static Set<Integer> getExpertiseIdsForUser(String username) throws SQLException {
        Set<Integer> expertiseIds = new HashSet<>();
        String sql = "SELECT expertise_id FROM user_expertise WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    expertiseIds.add(rs.getInt("expertise_id"));
                }
            }
        }
        return expertiseIds;
    }

    public static void updateFieldOfExpertise(int fieldId, String name, String description, Integer parentId) throws SQLException {
        String sql = "UPDATE fields_of_expertise SET name = ?, description = ?, parent_id = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, description);
            if (parentId != null && parentId > 0) {
                pstmt.setInt(3, parentId);
            } else {
                pstmt.setNull(3, java.sql.Types.INTEGER);
            }
            pstmt.setInt(4, fieldId);
            pstmt.executeUpdate();
        }
    }

    public static int createFieldOfExpertise(String name, String description, int parentId) throws SQLException {
        String sql = "INSERT INTO fields_of_expertise (name, description, parent_id) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            pstmt.setString(2, description);
            if (parentId > 0) {
                pstmt.setInt(3, parentId);
            } else {
                pstmt.setNull(3, java.sql.Types.INTEGER);
            }
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        }
        return -1;
    }    public static List<FieldOfExpertise> getSubFields(int parentFieldId) throws SQLException {
        List<FieldOfExpertise> subFields = new ArrayList<>();
        String sql = "SELECT * FROM fields_of_expertise WHERE parent_id = ? ORDER BY name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, parentFieldId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    FieldOfExpertise field = new FieldOfExpertise(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getObject("parent_id", Integer.class)
                    );
                    subFields.add(field);
                }
            }
        }
        return subFields;
    }

    public static boolean branchExists(int branchId) throws SQLException {
        String sql = "SELECT 1 FROM branches WHERE id = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, branchId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static List<Branch> getBranchesByParentId(int parentId) throws SQLException {
        List<Branch> branches = new ArrayList<>();
        String sql = "SELECT * FROM branches WHERE parent_id = ? ORDER BY name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, parentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Branch branch = new Branch(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getInt("parent_id")
                    );
                    branch.setCurrentPhase(Branch.Phase.valueOf(rs.getString("current_phase")));
                    branch.setTeamOpenings(rs.getInt("team_openings"));
                    Integer ideaId = rs.getObject("idea_id", Integer.class);
                    if (ideaId != null) {
                        branch.setIdeaId(ideaId);
                    }
                    branches.add(branch);
                }
            }
        }
        return branches;
    }

    public static int createBranch(String name, String description, int parentId, Integer ideaId) throws SQLException {
        String sql = "INSERT INTO branches (name, description, parent_id, idea_id, current_phase, team_openings) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setInt(3, parentId);
            if (ideaId != null && ideaId > 0) {
                pstmt.setInt(4, ideaId);
            } else {
                pstmt.setNull(4, java.sql.Types.INTEGER);
            }
            pstmt.setString(5, Branch.Phase.GENERATION.name());
            pstmt.setInt(6, 0); // Default team openings
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        }
        return -1;
    }

    public static Branch getBranch(int branchId) throws SQLException {
        String sql = "SELECT * FROM branches WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, branchId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Branch branch = new Branch(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getInt("parent_id")
                    );
                    branch.setCurrentPhase(Branch.Phase.valueOf(rs.getString("current_phase")));
                    branch.setTeamOpenings(rs.getInt("team_openings"));
                    Integer ideaId = rs.getObject("idea_id", Integer.class);
                    if (ideaId != null) {
                        branch.setIdeaId(ideaId);
                    }
                    return branch;
                }
            }
        }
        return null;
    }

    public static void associateBranchWithIdea(int branchId, int ideaId) throws SQLException {
        String sql = "UPDATE branches SET idea_id = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, ideaId);
            pstmt.setInt(2, branchId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Updates the idea associated with a branch (sets the idea_id column for the branch).
     * @param branchId The branch ID to update
     * @param ideaId The idea ID to associate (or 0/null to clear)
     * @throws SQLException if a database error occurs
     */
    public static void updateBranchIdea(int branchId, int ideaId) throws SQLException {
        String sql = "UPDATE branches SET idea_id = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (ideaId > 0) {
                pstmt.setInt(1, ideaId);
            } else {
                pstmt.setNull(1, java.sql.Types.INTEGER);
            }
            pstmt.setInt(2, branchId);
            pstmt.executeUpdate();
        }
    }

    // Additional proposal and user management methods
    public static int getCertifiedUserCount(int fieldId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM user_expertise WHERE expertise_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, fieldId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public static List<User> getUsersCertifiedInField(int fieldId) throws SQLException {
        List<User> certifiedUsers = new ArrayList<>();
        String sql = "SELECT u.* FROM users u " +
                    "JOIN user_expertise ue ON u.username = ue.username " +
                    "WHERE ue.expertise_id = ? ORDER BY ue.certification_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, fieldId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    User user = new User(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getInt("level"),
                        rs.getInt("xp"),
                        rs.getInt("points"),
                        rs.getInt("total_berries_earned")
                    );
                    user.setId(rs.getInt("id"));
                    try {
                        user.setDisplayName(rs.getString("display_name"));
                    } catch (SQLException e) {
                        user.setDisplayName(user.getUsername());
                    }
                    certifiedUsers.add(user);
                }
            }
        }
        return certifiedUsers;
    }

    public static List<Proposal> getProposalsByCategory(String category) throws SQLException {
        List<Proposal> proposals = new ArrayList<>();
        
        switch (category.toUpperCase()) {
            case "LEVEL":
                for (LevelProposal proposal : loadAllLevelProposals().values()) {
                    proposals.add(proposal);
                }
                break;
            case "BERRY_EARNING":
                for (BerryEarningProposal proposal : loadAllBerryEarningProposals().values()) {
                    proposals.add(proposal);
                }
                break;
            case "BERRY_VALIDITY":
                for (BerryValidityProposal proposal : loadAllBerryValidityProposals().values()) {
                    proposals.add(proposal);
                }
                break;
            case "BERRY_CONVERSION":
                for (BerryConversionProposal proposal : loadAllBerryConversionProposals().values()) {
                    proposals.add(proposal);
                }
                break;
            case "NEED_THRESHOLD":
                for (NeedThresholdProposal proposal : loadAllNeedThresholdProposals().values()) {
                    proposals.add(proposal);
                }
                break;
        }
        
        return proposals;
    }

    public static List<Proposal> getActiveProposals() throws SQLException {
        List<Proposal> activeProposals = new ArrayList<>();
        
        // Get all proposals and filter active ones (those with votes > 0 or status = "ACTIVE")
        activeProposals.addAll(getProposalsByCategory("LEVEL"));
        activeProposals.addAll(getProposalsByCategory("BERRY_EARNING"));
        activeProposals.addAll(getProposalsByCategory("BERRY_VALIDITY"));
        activeProposals.addAll(getProposalsByCategory("BERRY_CONVERSION"));
        activeProposals.addAll(getProposalsByCategory("NEED_THRESHOLD"));
        
        // Filter to only return proposals that are considered "active"
        return activeProposals.stream()
                .filter(p -> p.getVotes() > 0 || "ACTIVE".equals(p.getStatus()))
                .collect(ArrayList::new, (list, item) -> list.add(item), (list1, list2) -> list1.addAll(list2));
    }

    /**
     * Updates the needs associated with a given idea.
     * Removes all existing associations and inserts the new set.
     * @param ideaId The ID of the idea.
     * @param needIds The set of need IDs to associate with the idea.
     * @throws SQLException if a database error occurs.
     */
    public static void updateIdeaNeeds(int ideaId, Set<Integer> needIds) throws SQLException {
        String deleteSql = "DELETE FROM idea_needs WHERE idea_id = ?";
        String insertSql = "INSERT INTO idea_needs (idea_id, need_id) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            // Remove all existing associations
            deleteStmt.setInt(1, ideaId);
            deleteStmt.executeUpdate();
            // Insert new associations
            if (needIds != null) {
                for (Integer needId : needIds) {
                    insertStmt.setInt(1, ideaId);
                    insertStmt.setInt(2, needId);
                    insertStmt.addBatch();
                }
                insertStmt.executeBatch();
            }
        }
    }

    /**
     * Updates the current phase of a branch in the database.
     * @param branchId The ID of the branch to update.
     * @param phase The new phase to set (as Branch.Phase enum).
     * @throws SQLException if a database error occurs.
     */
    public static void updateBranchPhase(int branchId, Branch.Phase phase) throws SQLException {
        String sql = "UPDATE branches SET phase = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phase != null ? phase.name() : null);
            pstmt.setInt(2, branchId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Returns a list of users who have certification in the given field of expertise (by fieldId).
     * @param fieldId The ID of the field of expertise
     * @return List of User objects certified in the field
     * @throws SQLException if a database error occurs
     */
    public static List<User> getUsersByField(int fieldId) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT u.* FROM users u " +
                     "JOIN user_fields uf ON u.username = uf.username " +
                     "WHERE uf.field_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, fieldId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Assuming a method to load a User from ResultSet exists
                    User user = loadUserFromResultSet(rs);
                    users.add(user);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    // Helper method to load a User from a ResultSet (adjust as needed for your User schema)
    private static User loadUserFromResultSet(ResultSet rs) throws SQLException {
        String username = rs.getString("username");
        String password = rs.getString("password");
        String displayName = rs.getString("display_name");
        int xp = rs.getInt("xp");
        int level = rs.getInt("level");
        int points = rs.getInt("points");
        // Add more fields as needed
        User user = new User(username, password, displayName, xp, level, points);
        // Set additional fields if your User class has them
        return user;
    }    /**
     * Updates an existing FieldOfExpertise in the database.
     * @param selectedField The FieldOfExpertise object with updated values (must have a valid id).
     * @throws SQLException if a database error occurs
     */
    public static void updateFieldOfExpertise(FieldOfExpertise selectedField) throws SQLException {
        String sql = "UPDATE fields_of_expertise SET name = ?, description = ?, parent_id = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, selectedField.getName());
            stmt.setString(2, selectedField.getDescription());
            if (selectedField.getParentId() == null) {
                stmt.setNull(3, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(3, selectedField.getParentId());
            }
            stmt.setInt(4, selectedField.getId());
            stmt.executeUpdate();
        }
    }

    /**
     * Deletes a field of expertise and all its sub-fields recursively.
     * Also removes all user certifications for the deleted fields.
     * @param fieldId The ID of the field to delete
     * @throws SQLException if a database error occurs
     */
    public static void deleteFieldOfExpertise(int fieldId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // First, recursively delete all sub-fields
                deleteSubFieldsRecursively(conn, fieldId);
                
                // Delete user certifications for this field
                String deleteUserCertsSql = "DELETE FROM user_expertise WHERE expertise_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(deleteUserCertsSql)) {
                    stmt.setInt(1, fieldId);
                    stmt.executeUpdate();
                }
                
                // Delete branch expertise requirements for this field
                String deleteBranchReqsSql = "DELETE FROM branch_expertise_requirements WHERE expertise_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(deleteBranchReqsSql)) {
                    stmt.setInt(1, fieldId);
                    stmt.executeUpdate();
                }
                
                // Finally, delete the field itself
                String deleteFieldSql = "DELETE FROM fields_of_expertise WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(deleteFieldSql)) {
                    stmt.setInt(1, fieldId);
                    stmt.executeUpdate();
                }
                
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
    
    /**
     * Helper method to recursively delete sub-fields.
     * @param conn The database connection
     * @param parentFieldId The parent field ID whose children should be deleted
     * @throws SQLException if a database error occurs
     */
    private static void deleteSubFieldsRecursively(Connection conn, int parentFieldId) throws SQLException {
        // Get all sub-fields of the current field
        String getSubFieldsSql = "SELECT id FROM fields_of_expertise WHERE parent_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(getSubFieldsSql)) {
            stmt.setInt(1, parentFieldId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int subFieldId = rs.getInt("id");
                    
                    // Recursively delete sub-fields of this sub-field
                    deleteSubFieldsRecursively(conn, subFieldId);
                    
                    // Delete user certifications for this sub-field
                    String deleteUserCertsSql = "DELETE FROM user_expertise WHERE expertise_id = ?";
                    try (PreparedStatement deleteUserCertsStmt = conn.prepareStatement(deleteUserCertsSql)) {
                        deleteUserCertsStmt.setInt(1, subFieldId);
                        deleteUserCertsStmt.executeUpdate();
                    }
                    
                    // Delete branch expertise requirements for this sub-field
                    String deleteBranchReqsSql = "DELETE FROM branch_expertise_requirements WHERE expertise_id = ?";
                    try (PreparedStatement deleteBranchReqsStmt = conn.prepareStatement(deleteBranchReqsSql)) {
                        deleteBranchReqsStmt.setInt(1, subFieldId);
                        deleteBranchReqsStmt.executeUpdate();
                    }
                    
                    // Delete the sub-field itself
                    String deleteSubFieldSql = "DELETE FROM fields_of_expertise WHERE id = ?";
                    try (PreparedStatement deleteSubFieldStmt = conn.prepareStatement(deleteSubFieldSql)) {
                        deleteSubFieldStmt.setInt(1, subFieldId);
                        deleteSubFieldStmt.executeUpdate();
                    }
                }
            }
        }
    }

    /**
     * Adds a certification for a user in a specific field of expertise.
     * @param username The username of the user to certify.
     * @param fieldId The ID of the field of expertise.
     * @throws SQLException if a database error occurs
     */
    public static void addUserCertification(String username, int fieldId) throws SQLException {
        String sql = "INSERT INTO user_certifications (username, field_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setInt(2, fieldId);
            stmt.executeUpdate();
        }
    }

    /**
     * Removes a certification for a user in a specific field of expertise.
     * @param username The username of the user.
     * @param fieldId The ID of the field of expertise.
     * @throws SQLException if a database error occurs
     */
    public static void removeUserCertification(String username, int fieldId) throws SQLException {
        String sql = "DELETE FROM user_certifications WHERE username = ? AND field_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setInt(2, fieldId);
            stmt.executeUpdate();
        }
    }

    /**
     * Saves the expertise requirements for a branch and phase.
     * This will remove all existing requirements for the branch/phase and insert the new ones.
     * @param branchId The branch ID
     * @param phase The phase name (String)
     * @param requirements Map of fieldId to required level
     * @throws SQLException if a database error occurs
     */
    public static void saveBranchExpertiseRequirements(int branchId, String phase, Map<Integer, Integer> requirements) throws SQLException {
        String deleteSql = "DELETE FROM branch_expertise_requirements WHERE branch_id = ? AND phase = ?";
        String insertSql = "INSERT INTO branch_expertise_requirements (branch_id, phase, field_id, required_level) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, branchId);
                deleteStmt.setString(2, phase);
                deleteStmt.executeUpdate();
            }
            if (requirements != null && !requirements.isEmpty()) {
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    for (Map.Entry<Integer, Integer> entry : requirements.entrySet()) {
                        insertStmt.setInt(1, branchId);
                        insertStmt.setString(2, phase);
                        insertStmt.setInt(3, entry.getKey());
                        insertStmt.setInt(4, entry.getValue());
                        insertStmt.addBatch();
                    }
                    insertStmt.executeBatch();
                }
            }
            conn.commit();
        } catch (SQLException e) {
            throw e;
        }
    }

    public static List<ExpertiseRequirement> getAllTeamOpeningsForBranch(int branchId) throws SQLException {
        List<ExpertiseRequirement> openings = new ArrayList<>();
        String sql = "SELECT expertise_id, openings_count FROM branch_expertise_requirements WHERE branch_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, branchId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                openings.add(new ExpertiseRequirement(rs.getInt("expertise_id"), rs.getInt("openings_count")));
            }
        }
        return openings;
    }
}