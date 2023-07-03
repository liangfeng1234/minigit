package com.minigit.util;

public class StringRequest {
    private String repoPath;

    public StringRequest() {
        // 无参构造器
    }

    public StringRequest(String repoPath) {
        this.repoPath = repoPath;
    }

    public String getRepoPath() {
        return repoPath;
    }
}
