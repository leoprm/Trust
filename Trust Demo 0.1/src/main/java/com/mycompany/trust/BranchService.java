package com.mycompany.trust;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BranchService {
    
    public static int createBranch(String name, String description, int parentId, Integer ideaId) throws SQLException {
        if (name == null || name.trim().isEmpty()) {
            DialogFactory.showError("Branch name cannot be empty");
            return -1;
        }
        
        if (description == null || description.trim().isEmpty()) {
            DialogFactory.showError("Branch description cannot be empty");
            return -1;
        }
        
        // Check if parent branch exists
        if (parentId != 0 && !TrustSystem.branches.containsKey(parentId)) {
            DialogFactory.showError("Invalid parent branch ID");
            return -1;
        }
        
        // Check if branch name already exists
        for (Branch branch : TrustSystem.branches.values()) {
            if (branch.getName().equalsIgnoreCase(name) && branch.getParentId() == parentId) {
                DialogFactory.showError("Branch name already exists under the same parent");
                return -1;
            }
        }

        // Create branch in database
        int branchId = DatabaseManager.createBranch(name, description, parentId, ideaId);
        if (branchId != -1) {
            Branch branch = new Branch(name, description, parentId);
            branch.setId(branchId);
            TrustSystem.branches.put(branchId, branch);
            
            // Add to parent's children if parent exists
            if (parentId != 0) {
                Branch parent = TrustSystem.branches.get(parentId);
                parent.addChild(branchId);
            }
            
            associateBranchWithIdea(branchId, ideaId);
            return branchId;
        } else {
            DialogFactory.showError("Failed to create branch. Database error occurred.");
            return -1;
        }
    }
    
    public static void associateBranchWithIdea(int branchId, int ideaId) throws SQLException {
        if (!TrustSystem.branches.containsKey(branchId)) {
            DialogFactory.showError("Invalid branch ID");
            return;
        }
        
        if (!TrustSystem.ideas.containsKey(ideaId)) {
            DialogFactory.showError("Invalid idea ID");
            return;
        }
        
        Branch branch = TrustSystem.branches.get(branchId);
        Idea idea = TrustSystem.ideas.get(ideaId);
        
        // Check if this idea already has this branch
        if (idea.hasBranch(branchId)) {
            DialogFactory.showError("This branch is already associated with the idea");
            return;
        }
        
        // Set the idea ID in the branch
        branch.setIdeaId(ideaId);
        
        // Add branch to the idea
        idea.addBranch(branchId);
        
        // Update database
        DatabaseManager.associateBranchWithIdea(branchId, ideaId);
        
        DialogFactory.showInfo("Branch Associated", "The branch has been associated with idea: " + idea.getName());
    }
    
    public static List<Branch> getChildBranches(int parentId) {
        List<Branch> children = new ArrayList<>();
        
        for (Branch branch : TrustSystem.branches.values()) {
            if (branch.getParentId() == parentId) {
                children.add(branch);
            }
        }
        
        return children;
    }
    
    public static String getBranchPath(int branchId) {
        if (!TrustSystem.branches.containsKey(branchId)) {
            return "";
        }
        
        StringBuilder path = new StringBuilder();
        int currentId = branchId;
        
        while (currentId != 0) {
            Branch branch = TrustSystem.branches.get(currentId);
            path.insert(0, branch.getName() + " > ");
            currentId = branch.getParentId();
        }
        
        path.insert(0, "Root > ");
        
        // Remove the last " > "
        if (path.length() > 3) {
            path.delete(path.length() - 3, path.length());
        }
        
        return path.toString();
    }
    
    public static String getBranchDetails(int branchId) {
        if (!TrustSystem.branches.containsKey(branchId)) {
            return "Branch not found";
        }
        
        Branch branch = TrustSystem.branches.get(branchId);
        StringBuilder details = new StringBuilder();
        details.append("Name: ").append(branch.getName()).append("\n");
        details.append("Description: ").append(branch.getDescription()).append("\n");
        details.append("Path: ").append(getBranchPath(branchId)).append("\n\n");
        
        // Show associated idea if any
        if (branch.getIdeaId() > 0 && TrustSystem.ideas.containsKey(branch.getIdeaId())) {
            Idea idea = TrustSystem.ideas.get(branch.getIdeaId());
            details.append("Associated Idea: ").append(idea.getName()).append("\n");
        } else {
            details.append("No associated idea\n");
        }
        
        return details.toString();
    }
}
