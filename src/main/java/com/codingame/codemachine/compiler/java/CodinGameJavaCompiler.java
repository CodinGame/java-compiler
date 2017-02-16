package com.codingame.codemachine.compiler.java;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class CodinGameJavaCompiler {

    public static void main(String... args) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnosticsCollector = new DiagnosticCollector<>();
        int resultCode = 1;
        try(StandardJavaFileManager fileManager =
            compiler.getStandardFileManager(diagnosticsCollector, null, null)) {

            List<String> files = new ArrayList<>();
            List<String> options = new ArrayList<>();
    
            for (int i = 0; i < args.length; ++i) {
                String arg = args[i];
    
                if (arg.startsWith("-")) {
                    int paramCount = compiler.isSupportedOption(arg);
                    if (paramCount < 0) {
                        paramCount = fileManager.isSupportedOption(arg);
                    }
                    if (paramCount < 0) {
                        // unsupported, let javacTask show the error
                        paramCount = 0;
                    }
                    options.add(arg);
                    for (int j = 0; j < paramCount; ++j, i++) {
                        options.add(args[i + 1]);
                    }
                }
                else {
                    files.add(arg);
                }
            }
    
            if (!files.isEmpty()) {
                Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromStrings(files);
                JavaCompiler.CompilationTask task =
                    compiler.getTask(null, fileManager, diagnosticsCollector, options, null, compilationUnits);
                boolean success = task.call();
                for (Diagnostic<? extends JavaFileObject> diagnostic : diagnosticsCollector.getDiagnostics()) {
                    
                    LineNumberReader reader = new LineNumberReader(new StringReader(diagnostic.getSource().getCharContent(true).toString()));
                    String line = reader.lines().skip(diagnostic.getLineNumber() - 1).limit(1).findAny().get();
                    
                    String type = null;
                    switch (diagnostic.getKind()) {
                    case ERROR:
                        type = "ERROR";
                        break;
                    case WARNING:
                    case MANDATORY_WARNING:
                        type = "WARNING";
                        break;
                    case NOTE:
                        type = "INFO";
                        break;
                    case OTHER:
                        continue;
                    }
                    
                    System.err.println(String.format("%s:%d: %s: %s\n%s\n%"+diagnostic.getColumnNumber()+"s", 
                            diagnostic.getSource().getName(),
                            diagnostic.getLineNumber(),
                            diagnostic.getKind().name().toLowerCase(),
                            diagnostic.getMessage(null),
                            line,
                            "^"
                            ));
                    
                    System.out.println(String.format("CG> annotate --type \"%s\" --file \"%s\" --position \"%s\" --message \"%s\"",
                            type,
                            diagnostic.getSource().getName(),
                            diagnostic.getLineNumber()+":"+diagnostic.getColumnNumber(),
                            diagnostic.getMessage(null).replaceAll("\"","\\\"")
                            ));
                }
                resultCode = success ? 0 : 1;
            }
            else {
                System.err.println("no source file");
                resultCode = 2;
            }
        }

        System.exit(resultCode);
    }
}
