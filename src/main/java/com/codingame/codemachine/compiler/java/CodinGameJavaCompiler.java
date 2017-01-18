package com.codingame.codemachine.compiler.java;

import com.codingame.codemachine.compiler.java.core.CompilationLogDto;
import com.codingame.codemachine.compiler.java.core.CompilationLogKind;
import com.codingame.codemachine.compiler.java.core.CompilationResult;
import com.google.gson.Gson;

import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;

public class CodinGameJavaCompiler {

    private static class NullOutputStream extends OutputStream {
        @Override
        public void write(int b) throws IOException {
        }
    }

    public static void main(String... args) throws IOException {
        PrintStream realOut = System.out;
        System.setOut(new PrintStream(new NullOutputStream(), true));
        System.setErr(new PrintStream(new NullOutputStream(), true));

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnosticsCollector = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager =
            compiler.getStandardFileManager(diagnosticsCollector, null, null);

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
            List<CompilationLogDto> logs = new ArrayList<>();

            Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromStrings(files);
            JavaCompiler.CompilationTask task =
                compiler.getTask(null, fileManager, diagnosticsCollector, options, null, compilationUnits);
            boolean success = task.call();
            for (Diagnostic<? extends JavaFileObject> diagnostic : diagnosticsCollector.getDiagnostics()) {
                switch (diagnostic.getKind()) {
                    case ERROR:
                    case WARNING:
                        CompilationLogDto log = new CompilationLogDto();
                        log.setFilename(diagnostic.getSource().getName());
                        log.setLine(diagnostic.getLineNumber());
                        log.setColumn(diagnostic.getColumnNumber());
                        log.setMessage(diagnostic.getMessage(null));
                        log.setKind(diagnostic.getKind() == Kind.ERROR ? CompilationLogKind.ERROR : CompilationLogKind.WARNING);
                        logs.add(log);
                        break;
                    default:
                        // nothing
                }
            }
            fileManager.close();

            CompilationResult result = new CompilationResult();
            result.setSuccess(success);
            result.setLogs(logs);
            realOut.println(new Gson().toJson(result));
            System.exit(success ? 0 : 1);
        }
        else {
            CompilationResult result = new CompilationResult();
            result.setSuccess(false);
            CompilationLogDto log = new CompilationLogDto();
            log.setKind(CompilationLogKind.ERROR);
            log.setMessage("no source file");
            result.setLogs(singletonList(log));
            realOut.println(new Gson().toJson(result));
            System.exit(2);
        }
    }
}
