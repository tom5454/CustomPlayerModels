package com.tom.cpm.web.gwt;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.LambdaExpression;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.util.Util;

import com.google.gwt.dev.jjs.SourceInfo;
import com.google.gwt.dev.jjs.SourceOrigin;
import com.google.gwt.dev.jjs.impl.GwtAstBuilder.CudInfo;

public class SourceOriginPatch {

	public static SourceInfo makeSourceInfo(AbstractMethodDeclaration x, CudInfo curCud, String sourceMapPath) {
		int startLine = Util.getLineNumber(x.sourceStart, curCud.separatorPositions, 0, curCud.separatorPositions.length - 1);
		return SourceOrigin.create(x.sourceStart, x.bodyEnd, startLine, sourceMapPath, new String(x.binding.selector));
	}

	public static SourceInfo makeSourceInfo(ASTNode x, CudInfo curCud, String sourceMapPath) {
		int startLine = Util.getLineNumber(x.sourceStart, curCud.separatorPositions, 0, curCud.separatorPositions.length - 1);
		String nm = getName(x);
		if(nm != null) {
			return SourceOrigin.create(x.sourceStart, x.sourceEnd, startLine, sourceMapPath, nm);
		}
		return SourceOrigin.create(x.sourceStart, x.sourceEnd, startLine, sourceMapPath);
	}

	public static SourceInfo makeSourceInfo(ASTNode x, CudInfo curCud, String sourceMapPath, BlockScope scope) {
		ReferenceContext ctx = scope.referenceContext();
		int startLine = Util.getLineNumber(x.sourceStart, curCud.separatorPositions, 0, curCud.separatorPositions.length - 1);
		String nm = getName(ctx);
		if(nm != null) {
			return SourceOrigin.create(x.sourceStart, x.sourceEnd, startLine, sourceMapPath, nm);
		}
		return SourceOrigin.create(x.sourceStart, x.sourceEnd, startLine, sourceMapPath);
	}

	private static String getName(ASTNode x) {
		if(x instanceof TypeDeclaration) {
			return "<init>";
		} else if(x instanceof FieldDeclaration) {
			FieldDeclaration fd = (FieldDeclaration) x;
			return fd.isStatic() ? "<clinit>" : "<init>";
		} else if(x instanceof LocalDeclaration) {
			LocalDeclaration c = (LocalDeclaration) x;
			return getName(c.binding.declaringScope.referenceContext());
		}
		return null;
	}

	private static String getName(ReferenceContext ctx) {
		if(ctx instanceof AbstractMethodDeclaration) {
			AbstractMethodDeclaration c = (AbstractMethodDeclaration) ctx;
			return new String(c.binding.selector);
		} else if(ctx instanceof TypeDeclaration) {
			return "<init>";
		} else if(ctx instanceof LambdaExpression) {
			ReferenceContext rc = ((LambdaExpression)ctx).enclosingScope.referenceContext();
			return getName(rc) + "$lambda";
		}
		return null;
	}
}
