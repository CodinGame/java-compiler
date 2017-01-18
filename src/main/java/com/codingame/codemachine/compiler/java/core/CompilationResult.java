package com.codingame.codemachine.compiler.java.core;

import java.util.List;

public class CompilationResult {
    private List<CompilationLogDto> logs;
    private boolean success;

    public List<CompilationLogDto> getLogs() {
        return logs;
    }

    public void setLogs(List<CompilationLogDto> logs) {
        this.logs = logs;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
