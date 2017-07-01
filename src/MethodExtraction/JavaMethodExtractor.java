package MethodExtraction;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class JavaMethodExtractor extends ASTVisitor{
	
	private String filePath;
	private String documentText;
	private HashMap<String, String> methodBodies = new HashMap<>();
	private HashMap<String, ArrayList<String>> apiCalls = new HashMap<>();
	private boolean methodExtracted = false;
	
	public JavaMethodExtractor(String inputJavaFilePath) throws FileNotFoundException{
		this.filePath = inputJavaFilePath;
		Scanner scanner = new Scanner(new File(inputJavaFilePath));
		String fileString = scanner.nextLine();
		while (scanner.hasNextLine()) {
			fileString = fileString + "\n" + scanner.nextLine();
		}
		this.documentText = fileString;
		scanner.close();
	}
	
	
	public boolean visit(MethodDeclaration node){
		String  methodBody = node.getBody().toString();
		String text = node.toString();
		String methodSignature = text.substring(0, text.indexOf(methodBody));
		ArrayList<String> apis = new ArrayList<>();
		ASTVisitor methodInvocationASTVisitor = new ASTVisitor() {
			public boolean visit(MethodInvocation minode){
				apis.add(minode.toString());
				return true;
			}
		}; 
		
		node.accept(methodInvocationASTVisitor);
		methodBodies.put(methodSignature, methodBody);
		apiCalls.put(methodSignature, apis);
		return true;
	}
	
	public void extract() {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		
		Map<String, String> options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
		parser.setCompilerOptions(options);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(this.documentText.toCharArray());
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		String[] sources = { "" };
		String[] classpath = { System.getProperty("java.home") + "/lib/rt.jar" };
		parser.setUnitName(this.filePath);
		parser.setEnvironment(classpath, sources, new String[] { "UTF-8" }, true);
		
		CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		cu.accept(this);	
		methodExtracted = true;
	}
	
	public HashMap<String, String> getMethodBodies() throws Exception{
		if(methodExtracted){
			return methodBodies;
		}
		else {
			throw new Exception("Methods have not been extracted yet. Please call "
					+ "JavaMethodExtractor.extract() prior calling this method");
		}
	}
	
	public HashMap<String, ArrayList<String>> getAPICalls() throws Exception{
		if(methodExtracted){
			return apiCalls;
		}
		else {
			throw new Exception("Methods have not been extracted yet. Please call "
					+ "JavaMethodExtractor.extract() prior calling this method");
		}
	}
}
