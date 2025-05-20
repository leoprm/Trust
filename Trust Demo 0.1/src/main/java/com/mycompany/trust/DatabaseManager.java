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

    private static void loadIdeaNeedAssociations() throws SQLException {
        String sql = "SELECT idea_id, need_id FROM idea_needs";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                int ideaId = rs.getInt("idea_id");
                int needId = rs.getInt("need_id");
                
                if (TrustSystem.ideas.containsKey(ideaId)) {
                    TrustSystem.ideas.get(ideaId).addAssociatedNeedId(needId);
                }
            }
        }
        System.out.println("Loaded idea-need associations.");
    }

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
                 }
                 // Load expertise for this user
                 user.setCertifiedExpertiseIds(getExpertiseIdsForUser(user.getUsername()));
                 users.put(user.getUsername(), user);
            }
        }
        return users;
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
                         allPhaseTeams.computeIfAbsent(branchId, k -> new HashMap<>());

                     ArrayList<String> teamList =
                         branchPhaseMap.computeIfAbsent(phase, k -> new ArrayList<>());

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
                userBerries.computeIfAbsent(username, k -> new ArrayList<>()).add(berry);
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
    }

    public static Map<Integer, BerryValidityProposal> loadAllBerryValidityProposals() throws SQLException {
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
    
    public static int createNeedThresholdProposal(String proposer, double globalThresholdPercent, 
                                                 double personalThresholdPercent, int timeLimit) throws SQLException {
        String sql = "INSERT INTO need_threshold_proposals (proposer_username, global_threshold_percent, " +
                     "personal_threshold_percent, time_limit_months, branch_id, votes) " +
                     "VALUES (?, ?, ?, ?, -1, 0)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, proposer);
            pstmt.setDouble(2, globalThresholdPercent);
            pstmt.setDouble(3, personalThresholdPercent);
            pstmt.setInt(4, timeLimit);
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }
        return -1;
    }
    
    public static void addNeedThresholdProposalVote(int proposalId) throws SQLException {
        String sql = "UPDATE need_threshold_proposals SET votes = votes + 1 WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, proposalId);
            pstmt.executeUpdate();
        }
    }

    public static int createLevelProposal(String proposer, double xpIncreasePercentage, double xpThreshold) throws SQLException {
        String sql = "INSERT INTO level_proposals (proposer_username, xp_increase_percentage, xp_threshold, votes) VALUES (?, ?, ?, 0)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, proposer);
            stmt.setDouble(2, xpIncreasePercentage);
            stmt.setDouble(3, xpThreshold);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating level proposal failed, no rows affected.");
            }
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating level proposal failed, no ID obtained.");
                }
            }
        }
    }

    public static int createBerryEarningProposal(String proposer, int initialEarning) throws SQLException {
        String sql = "INSERT INTO berry_earning_proposals (proposer_username, initial_level_one_berry_earning, votes) VALUES (?, ?, 0)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, proposer);
            stmt.setInt(2, initialEarning);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Creating berry earning proposal failed, no rows affected.");
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) return generatedKeys.getInt(1);
                else throw new SQLException("Creating berry earning proposal failed, no ID obtained.");
            }
        }
    }

    public static int createBerryValidityProposal(String proposer, int months) throws SQLException {
        String sql = "INSERT INTO berry_validity_proposals (proposer_username, months, votes) VALUES (?, ?, 0)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, proposer);
            stmt.setInt(2, months);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Creating berry validity proposal failed, no rows affected.");
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) return generatedKeys.getInt(1);
                else throw new SQLException("Creating berry validity proposal failed, no ID obtained.");
            }
        }
    }

    public static int createBerryConversionProposal(String proposer, double percentage, int period) throws SQLException {
        String sql = "INSERT INTO berry_conversion_proposals (proposer_username, conversion_percentage, conversion_period, votes) VALUES (?, ?, ?, 0)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, proposer);
            stmt.setDouble(2, percentage);
            stmt.setInt(3, period);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Creating berry conversion proposal failed, no rows affected.");
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) return generatedKeys.getInt(1);
                else throw new SQLException("Creating berry conversion proposal failed, no ID obtained.");
            }
        }
    }

    public static void addLevelProposalVote(int proposalId) throws SQLException {
        String sql = "UPDATE level_proposals SET votes = votes + 1 WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, proposalId);
            pstmt.executeUpdate();
        }
    }

    public static void addBerryEarningProposalVote(int proposalId) throws SQLException {
        String sql = "UPDATE berry_earning_proposals SET votes = votes + 1 WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, proposalId);
            pstmt.executeUpdate();
        }
    }

    public static void addBerryValidityProposalVote(int proposalId) throws SQLException {
        String sql = "UPDATE berry_validity_proposals SET votes = votes + 1 WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, proposalId);
            pstmt.executeUpdate();
        }
    }

    public static void addBerryConversionProposalVote(int proposalId) throws SQLException {
        String sql = "UPDATE berry_conversion_proposals SET votes = votes + 1 WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, proposalId);
            pstmt.executeUpdate();
        }
    }

    public static int saveBerry(Berry berry) throws SQLException {
        if (berry.getId() <= 0) {
            String sql = "INSERT INTO berries (username, amount, source, expiration_date) VALUES (?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                
                pstmt.setString(1, berry.getUsername());
                pstmt.setInt(2, berry.getAmount());
                pstmt.setString(3, berry.getSource());
                if (berry.getExpirationDate() != null) {
                    pstmt.setTimestamp(4, Timestamp.valueOf(berry.getExpirationDate()));
                } else {
                    pstmt.setNull(4, java.sql.Types.TIMESTAMP);
                    System.out.println("Warning: Saving berry with null expiration date for user " + berry.getUsername());
                }
                
                pstmt.executeUpdate();
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    berry.setId(rs.getInt(1));
                    return berry.getId();
                } else {
                    return -1;
                }
            }
        } else {
            String sql = "UPDATE berries SET username = ?, amount = ?, source = ?, expiration_date = ?, expired = ? WHERE id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, berry.getUsername());
                pstmt.setInt(2, berry.getAmount());
                pstmt.setString(3, berry.getSource());
                if (berry.getExpirationDate() != null) {
                    pstmt.setTimestamp(4, Timestamp.valueOf(berry.getExpirationDate()));
                } else {
                    pstmt.setNull(4, java.sql.Types.TIMESTAMP);
                }
                pstmt.setBoolean(5, berry.isExpired());
                pstmt.setInt(6, berry.getId());
                pstmt.executeUpdate();
                return berry.getId();
            }
        }
    }

    public static void markBerryExpired(int berryId) throws SQLException {
        String sql = "UPDATE berries SET expired = true WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, berryId);
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

    public static int createBranch(String name, String description, int parentId, Integer ideaId) throws SQLException {
        String sql = "INSERT INTO branches (name, description, parent_id, idea_id) VALUES (?, ?, ?, ?)";
        int branchId = -1;
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
            
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                branchId = rs.getInt(1);
                if (ideaId != null && ideaId > 0) {
                    associateBranchWithIdea(branchId, ideaId);
                }
                initializeBranch(branchId);
            }
        }
        return branchId;
    }

    public static void associateBranchWithIdea(int branchId, int ideaId) throws SQLException {
        String checkSql = "SELECT 1 FROM branch_ideas WHERE branch_id = ? AND idea_id = ?";
        boolean exists = false;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkPstmt = conn.prepareStatement(checkSql)) {
            checkPstmt.setInt(1, branchId);
            checkPstmt.setInt(2, ideaId);
            try (ResultSet rs = checkPstmt.executeQuery()) {
                exists = rs.next();
            }
        }

        if (!exists) {
             String sql = "INSERT INTO branch_ideas (branch_id, idea_id) VALUES (?, ?)";
             try (Connection conn = DatabaseConnection.getConnection();
                  PreparedStatement pstmt = conn.prepareStatement(sql)) {
                 pstmt.setInt(1, branchId);
                 pstmt.setInt(2, ideaId);
                 pstmt.executeUpdate();
             }
        } else {
            System.out.println("Association between branch " + branchId + " and idea " + ideaId + " already exists.");
        }
    }

    public static int saveUser(User user) throws SQLException {
        User existingUser = getUser(user.getUsername());
        
        if (existingUser == null) {
            String sql = "INSERT INTO users (username, password, display_name, level, xp, points, total_berries_earned) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, user.getUsername());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getDisplayName() != null ? user.getDisplayName() : user.getUsername());
                pstmt.setInt(4, user.getLevel());
                pstmt.setInt(5, user.getXp());
                pstmt.setInt(6, user.getPoints());
                pstmt.setInt(7, user.getTotalBerriesEarned());
                pstmt.executeUpdate();
                
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    return -1;
                }
            }
        } else {
            updateUser(user);
            return existingUser.getId();
        }
    }

    public static int saveRating(Rating rating) throws SQLException {
        String sql = "INSERT INTO ratings (rater_username, branch_id, phase_type, rating_value, comment) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, rating.getRaterUsername());
            pstmt.setInt(2, rating.getBranchId());
            pstmt.setString(3, rating.getPhaseType());
            pstmt.setInt(4, rating.getRatingValue());
            pstmt.setString(5, rating.getComment());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating rating failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    rating.setId(id);
                    return id;
                } else {
                    throw new SQLException("Creating rating failed, no ID obtained.");
                }
            }
        }
    }

    public static List<Rating> loadRatingsForBranchPhase(int branchId, String phaseType) throws SQLException {
        List<Rating> ratings = new ArrayList<>();
        String sql = "SELECT * FROM ratings WHERE branch_id = ? AND phase_type = ? ORDER BY timestamp DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, branchId);
            pstmt.setString(2, phaseType);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Rating rating = new Rating(
                        rs.getString("rater_username"),
                        rs.getInt("branch_id"),
                        rs.getString("phase_type"),
                        rs.getInt("rating_value"),
                        rs.getString("comment")
                );
                rating.setId(rs.getInt("id"));
                rating.setTimestamp(rs.getTimestamp("timestamp"));
                ratings.add(rating);
            }
        }
        return ratings;
    }

    public static Rating getRatingByUserForBranchPhase(String raterUsername, int branchId, String phaseType) throws SQLException {
        String sql = "SELECT * FROM ratings WHERE rater_username = ? AND branch_id = ? AND phase_type = ? LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, raterUsername);
            pstmt.setInt(2, branchId);
            pstmt.setString(3, phaseType);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Rating rating = new Rating(
                        rs.getString("rater_username"),
                        rs.getInt("branch_id"),
                        rs.getString("phase_type"),
                        rs.getInt("rating_value"),
                        rs.getString("comment")
                );
                rating.setId(rs.getInt("id"));
                rating.setTimestamp(rs.getTimestamp("timestamp"));
                return rating;
            } else {
                return null;
            }
        }
    }

    public static double getAverageRatingForBranchPhase(int branchId, String phaseType) throws SQLException {
        List<Rating> ratings = loadRatingsForBranchPhase(branchId, phaseType);
        if (ratings == null || ratings.isEmpty()) {
            return 50.0;
        }

        double sum = 0;
        for (Rating rating : ratings) {
            sum += rating.getRatingValue();
        }
        return sum / ratings.size();
    }

    public static void deleteRating(int ratingId) throws SQLException {
        String sql = "DELETE FROM ratings WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, ratingId);
            int affectedRows = pstmt.executeUpdate();
             if (affectedRows == 0) {
                 System.out.println("Warning: No rating found with ID " + ratingId + " to delete.");
             }
        }
    }

    // --- Field of Expertise (FoE) Operations ---

    public static int createFieldOfExpertise(String name, String description, Integer parentId) throws SQLException { // Added parentId parameter
        String sql = "INSERT INTO fields_of_expertise (name, description, parent_id) VALUES (?, ?, ?)"; // Added parent_id to query
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            pstmt.setString(2, description);
            if (parentId != null) { // Handle null parentId
                pstmt.setInt(3, parentId);
            } else {
                pstmt.setNull(3, java.sql.Types.INTEGER);
            }
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating Field of Expertise failed, no rows affected.");
            }
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating Field of Expertise failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            // Handle unique constraint violation for name
            if (e.getMessage().toLowerCase().contains("unique constraint") || e.getMessage().toLowerCase().contains("duplicate entry")) {
                System.err.println("Field of Expertise with name '" + name + "' already exists.");
                return -1; // Indicate failure due to duplicate name
            } else {
                throw e; // Re-throw other SQL errors
            }
        }
    }

    public static FieldOfExpertise getFieldOfExpertise(int id) throws SQLException {
        String sql = "SELECT * FROM fields_of_expertise WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Integer parentId = rs.getObject("parent_id") != null ? rs.getInt("parent_id") : null; // Get parentId, handle null
                    return new FieldOfExpertise(rs.getInt("id"), rs.getString("name"), rs.getString("description"), parentId);
                }
            }
        }
        return null;
    }

    public static Map<Integer, FieldOfExpertise> loadAllFieldsOfExpertise() throws SQLException {
        Map<Integer, FieldOfExpertise> expertiseMap = new HashMap<>();
        String sql = "SELECT * FROM fields_of_expertise ORDER BY name";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Integer parentId = rs.getObject("parent_id") != null ? rs.getInt("parent_id") : null; // Get parentId, handle null
                FieldOfExpertise foe = new FieldOfExpertise(rs.getInt("id"), rs.getString("name"), rs.getString("description"), parentId);
                expertiseMap.put(foe.getId(), foe);
            }
        }
        return expertiseMap;
    }

    public static void updateFieldOfExpertise(FieldOfExpertise foe) throws SQLException {
        String sql = "UPDATE fields_of_expertise SET name = ?, description = ?, parent_id = ? WHERE id = ?"; // Added parent_id to query
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, foe.getName());
            pstmt.setString(2, foe.getDescription());
            if (foe.getParentId() != null) { // Handle null parentId
                pstmt.setInt(3, foe.getParentId());
            } else {
                pstmt.setNull(3, java.sql.Types.INTEGER);
            }
            pstmt.setInt(4, foe.getId());
            pstmt.executeUpdate();
        }
    }

    public static void deleteFieldOfExpertise(int id) throws SQLException {
        // Note: Deleting an FoE might require handling cascading deletes or preventing deletion
        // if it's linked in user_expertise, depending on FK constraints.
        String sql = "DELETE FROM fields_of_expertise WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                 System.out.println("Warning: No Field of Expertise found with ID " + id + " to delete.");
            }
        }
    }

    // --- User Expertise Certification ---

    public static void certifyUserExpertise(String username, int expertiseId) throws SQLException {
        String sql = "INSERT IGNORE INTO user_expertise (username, expertise_id) VALUES (?, ?)"; // Use INSERT IGNORE or similar
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setInt(2, expertiseId);
            pstmt.executeUpdate();
        }
    }

    public static void revokeUserExpertise(String username, int expertiseId) throws SQLException {
        String sql = "DELETE FROM user_expertise WHERE username = ? AND expertise_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setInt(2, expertiseId);
            pstmt.executeUpdate();
        }
    }

    public static Set<Integer> getExpertiseIdsForUser(String username) throws SQLException {
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

    // --- Notification Methods ---

    public static int saveNotification(Notification notification) throws SQLException {
        String sql = "INSERT INTO notifications (username, message, is_read, related_branch_id, timestamp) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, notification.getUsername());
            pstmt.setString(2, notification.getMessage());
            pstmt.setBoolean(3, notification.isRead());
            if (notification.getRelatedBranchId() != null) {
                pstmt.setInt(4, notification.getRelatedBranchId());
            } else {
                pstmt.setNull(4, java.sql.Types.INTEGER);
            }
            pstmt.setTimestamp(5, notification.getTimestamp());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1); // Return the new notification ID
                    }
                }
            }
        }
        return -1; // Indicate failure
    }

    public static List<Notification> loadUnreadNotifications(String username) throws SQLException {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE username = ? AND is_read = FALSE ORDER BY timestamp DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Integer relatedBranchId = rs.getObject("related_branch_id", Integer.class);
                    notifications.add(new Notification(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("message"),
                        rs.getTimestamp("timestamp"),
                        rs.getBoolean("is_read"),
                        relatedBranchId
                    ));
                }
            }
        }
        return notifications;
    }

    public static void markNotificationAsRead(int notificationId) throws SQLException {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, notificationId);
            pstmt.executeUpdate();
        }
    }

    public static void markAllNotificationsAsRead(String username) throws SQLException {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE username = ? AND is_read = FALSE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        }
    }

    /**
     * Updates the expertise requirements for a branch's phase
     * 
     * @param branchId The branch ID
     * @param phaseName The phase name
     * @param expertiseRequirements Map of expertise ID to count of openings
     * @throws SQLException If a database error occurs
     */
    public static void updateBranchExpertiseRequirements(int branchId, String phaseName, Map<Integer, Integer> expertiseRequirements) 
            throws SQLException {
        // First, clear existing requirements
        String deleteSql = "DELETE FROM branch_expertise_requirements WHERE branch_id = ? AND phase_name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
            deleteStmt.setInt(1, branchId);
            deleteStmt.setString(2, phaseName);
            deleteStmt.executeUpdate();
        }
        
        // Insert new requirements
        if (expertiseRequirements != null && !expertiseRequirements.isEmpty()) {
            String insertSql = "INSERT INTO branch_expertise_requirements (branch_id, phase_name, expertise_id, openings_count) VALUES (?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                
                for (Map.Entry<Integer, Integer> entry : expertiseRequirements.entrySet()) {
                    if (entry.getValue() > 0) {  // Only insert positive counts
                        insertStmt.setInt(1, branchId);
                        insertStmt.setString(2, phaseName);
                        insertStmt.setInt(3, entry.getKey());
                        insertStmt.setInt(4, entry.getValue());
                        insertStmt.executeUpdate();
                    }
                }
            }
        }
    }
    
    /**
     * Loads expertise requirements for a branch's phase
     * 
     * @param branchId The branch ID
     * @param phaseName The phase name
     * @return Map of expertise ID to count of openings
     * @throws SQLException If a database error occurs
     */
    public static Map<Integer, Integer> loadBranchExpertiseRequirements(int branchId, String phaseName) throws SQLException {
        Map<Integer, Integer> requirements = new HashMap<>();
        String sql = "SELECT expertise_id, openings_count FROM branch_expertise_requirements WHERE branch_id = ? AND phase_name = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, branchId);
            stmt.setString(2, phaseName);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int expertiseId = rs.getInt("expertise_id");
                    int count = rs.getInt("openings_count");
                    requirements.put(expertiseId, count);
                }
            }
        }
        
        return requirements;
    }
    
    // Helper method to get expertise IDs for a phase - for backward compatibility
    public static Set<Integer> getExpertiseIdsForPhase(int branchId, String phaseName) throws SQLException {
        // This method now uses branch_expertise_requirements instead of phase_expertise
        Set<Integer> expertiseIds = new HashSet<>();
        Map<Integer, Integer> requirements = loadBranchExpertiseRequirements(branchId, phaseName);
        if (requirements != null) {
            expertiseIds.addAll(requirements.keySet());
        }
        return expertiseIds;
    }
    
    /**
     * Ensures that a special "General" expertise entry exists with ID = -1
     * This is used for general team openings with no specific expertise requirements
     * 
     * @throws SQLException If a database error occurs
     */
    public static void ensureGeneralExpertiseExists() throws SQLException {
        // Check if entry with ID -1 exists
        String checkSql = "SELECT COUNT(*) FROM fields_of_expertise WHERE id = -1";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {
            
            rs.next();
            int count = rs.getInt(1);
            
            if (count == 0) {
                // Create the special entry if it doesn't exist
                String insertSql = "INSERT INTO fields_of_expertise (id, name, description) VALUES (-1, 'General (No specific expertise)', 'General team openings with no specific expertise requirements')";
                try (Statement insertStmt = conn.createStatement()) {
                    // We may need to disable foreign key checks temporarily for this special ID
                    insertStmt.execute("SET FOREIGN_KEY_CHECKS=0");
                    insertStmt.executeUpdate(insertSql);
                    insertStmt.execute("SET FOREIGN_KEY_CHECKS=1");
                    System.out.println("Created special 'General' expertise entry with ID -1");
                }
            }
        }
    }

    // --- Proposal Voting ---
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
        // Also increment the vote count
        String updateSql = "UPDATE level_proposals SET votes = votes + 1 WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
            pstmt.setInt(1, proposalId);
            pstmt.executeUpdate();
        }
    }

    public static Set<String> loadLevelProposalVoters(int proposalId) throws SQLException {
        Set<String> voters = new java.util.HashSet<>();
        String sql = "SELECT username FROM level_proposal_votes WHERE proposal_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, proposalId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                voters.add(rs.getString("username"));
            }
        }
        return voters;
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
        String updateSql = "UPDATE berry_earning_proposals SET votes = votes + 1 WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
            pstmt.setInt(1, proposalId);
            pstmt.executeUpdate();
        }
    }

    public static Set<String> loadBerryEarningProposalVoters(int proposalId) throws SQLException {
        Set<String> voters = new java.util.HashSet<>();
        String sql = "SELECT username FROM berry_earning_proposal_votes WHERE proposal_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, proposalId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                voters.add(rs.getString("username"));
            }
        }
        return voters;
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
        String updateSql = "UPDATE berry_validity_proposals SET votes = votes + 1 WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
            pstmt.setInt(1, proposalId);
            pstmt.executeUpdate();
        }
    }

    public static Set<String> loadBerryValidityProposalVoters(int proposalId) throws SQLException {
        Set<String> voters = new java.util.HashSet<>();
        String sql = "SELECT username FROM berry_validity_proposal_votes WHERE proposal_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, proposalId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                voters.add(rs.getString("username"));
            }
        }
        return voters;
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
        String updateSql = "UPDATE berry_conversion_proposals SET votes = votes + 1 WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
            pstmt.setInt(1, proposalId);
            pstmt.executeUpdate();
        }
    }

    public static Set<String> loadBerryConversionProposalVoters(int proposalId) throws SQLException {
        Set<String> voters = new java.util.HashSet<>();
        String sql = "SELECT username FROM berry_conversion_proposal_votes WHERE proposal_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, proposalId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                voters.add(rs.getString("username"));
            }
        }
        return voters;
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
        String updateSql = "UPDATE need_threshold_proposals SET votes = votes + 1 WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
            pstmt.setInt(1, proposalId);
            pstmt.executeUpdate();
        }
    }

    public static Set<String> loadNeedThresholdProposalVoters(int proposalId) throws SQLException {
        Set<String> voters = new java.util.HashSet<>();
        String sql = "SELECT username FROM need_threshold_proposal_votes WHERE proposal_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, proposalId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                voters.add(rs.getString("username"));
            }
        }
        return voters;
    }
}