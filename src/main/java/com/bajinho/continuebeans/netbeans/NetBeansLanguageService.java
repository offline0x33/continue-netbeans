package com.bajinho.continuebeans.netbeans;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePathScanner;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/** Adapter over NetBeans' native Java language infrastructure. */
public final class NetBeansLanguageService {
    private NetBeansLanguageService() { }

    public static Map<String, Object> analyzeJavaFile(String path) throws IOException {
        File file = new File(path);
        FileObject fileObject = FileUtil.toFileObject(file);
        if (fileObject == null || !fileObject.isData()) {
            throw new IOException("Java source not found: " + path);
        }
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        if (javaSource == null) {
            throw new IOException("NetBeans Java language infrastructure is unavailable for: " + path);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        List<Map<String, Object>> symbols = new ArrayList<>();
        javaSource.runUserActionTask((CompilationController controller) -> {
            controller.toPhase(JavaSource.Phase.RESOLVED);
            CompilationUnitTree unit = controller.getCompilationUnit();
            if (unit == null) return;
            result.put("package", unit.getPackageName() == null ? "" : unit.getPackageName().toString());
            result.put("sourceFile", fileObject.getPath());
            new TreePathScanner<Void, Void>() {
                @Override public Void visitClass(ClassTree tree, Void unused) {
                    addSymbol(controller, unit, tree, "type", tree.getSimpleName().toString(), symbols);
                    return super.visitClass(tree, unused);
                }
                @Override public Void visitMethod(MethodTree tree, Void unused) {
                    addSymbol(controller, unit, tree, "method", tree.getName().toString(), symbols);
                    return super.visitMethod(tree, unused);
                }
                @Override public Void visitVariable(VariableTree tree, Void unused) {
                    addSymbol(controller, unit, tree, "variable", tree.getName().toString(), symbols);
                    return super.visitVariable(tree, unused);
                }
            }.scan(unit, null);
        }, true);
        result.put("language", "java");
        result.put("symbols", symbols);
        result.put("symbolCount", symbols.size());
        result.put("status", "success");
        return result;
    }

    private static void addSymbol(CompilationController controller, CompilationUnitTree unit,
                                  com.sun.source.tree.Tree tree, String kind, String name,
                                  List<Map<String, Object>> symbols) {
        long start = controller.getTrees().getSourcePositions().getStartPosition(unit, tree);
        if (start < 0) return;
        long line = unit.getLineMap() == null ? -1 : unit.getLineMap().getLineNumber(start);
        long column = unit.getLineMap() == null ? -1 : unit.getLineMap().getColumnNumber(start);
        Map<String, Object> symbol = new LinkedHashMap<>();
        symbol.put("kind", kind);
        symbol.put("name", name);
        symbol.put("line", line);
        symbol.put("column", column);
        com.sun.source.util.TreePath path = controller.getTrees().getPath(unit, tree);
        if (path != null) {
            javax.lang.model.element.Element element = controller.getTrees().getElement(path);
            if (element != null) {
                symbol.put("qualifiedName", element.toString());
                symbol.put("elementKind", element.getKind().name());
            }
        }
        symbols.add(symbol);
    }
}
