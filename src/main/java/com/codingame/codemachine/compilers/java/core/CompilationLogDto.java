package com.codingame.codemachine.compilers.java.core;

public class CompilationLogDto {
    private CompilationLogKind kind;
    private Long line, column;
    private String message;
    private String filename;

    public CompilationLogKind getKind() {
        return kind;
    }

    public void setKind(CompilationLogKind kind) {
        this.kind = kind;
    }

    public Long getLine() {
        return line;
    }

    public void setLine(Long line) {
        this.line = line;
    }

    public Long getColumn() {
        return column;
    }

    public void setColumn(Long column) {
        this.column = column;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
