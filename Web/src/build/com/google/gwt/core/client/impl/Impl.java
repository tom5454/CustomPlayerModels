package com.google.gwt.core.client.impl;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.core.client.JavaScriptObject;

/**
 * Private implementation class for GWT core. This API is should not be
 * considered public or stable.
 */
public final class Impl {

	static {
		if (GWT.isScript() && StackTraceCreator.collector != null) {
			// Just enforces loading of StackTraceCreator early on, nothing else to do here...
		}
	}

	/**
	 * This method should be used whenever GWT code is entered from a JS context
	 * and there is no GWT code in the same module on the call stack. Examples
	 * include event handlers, exported methods, and module initialization.
	 * <p>
	 * The GWT compiler and Development Mode will provide a module-scoped
	 * variable, <code>$entry</code>, which is an alias for this method.
	 * <p>
	 * This method can be called reentrantly, which will simply delegate to the
	 * function.
	 * <p>
	 * The function passed to this method will be invoked via
	 * <code>Function.apply()</code> with the current <code>this</code> value and
	 * the invocation arguments passed to <code>$entry</code>.
	 *
	 * @param jsFunction a JS function to invoke, which is typically a JSNI
	 *          reference to a static Java method
	 * @return the value returned when <code>jsFunction</code> is invoked, or
	 *         <code>undefined</code> if the UncaughtExceptionHandler catches an
	 *         exception raised by <code>jsFunction</code>
	 */
	public static native JavaScriptObject entry(JavaScriptObject jsFunction) /*-{
    return function() {
      return jsFunction.apply(this, arguments);
    };
  }-*/;

	public static native String getHostPageBaseURL() /*-{
    var s = $doc.location.href;

    // Pull off any hash.
    var i = s.indexOf('#');
    if (i != -1)
      s = s.substring(0, i);

    // Pull off any query string.
    i = s.indexOf('?');
    if (i != -1)
      s = s.substring(0, i);

    // Rip off everything after the last slash.
    i = s.lastIndexOf('/');
    if (i != -1)
      s = s.substring(0, i);

    // Ensure a final slash if non-empty.
    return s.length > 0 ? s + "/" : "";
  }-*/;

	public static native String getModuleBaseURL() /*-{
    // Check to see if DevModeRedirectHook has set an alternate value.
    // The key should match DevModeRedirectHook.js.
    var key = "__gwtDevModeHook:" + $moduleName + ":moduleBase";
    var global = $wnd || self;
    return global[key] || $moduleBase;
  }-*/;

	public static native String getModuleBaseURLForStaticFiles() /*-{
    return $moduleBase;
  }-*/;

	public static native String getModuleName() /*-{
    return $moduleName;
  }-*/;

	/**
	 * Returns the obfuscated name of members in the compiled output. This is a thin wrapper around
	 * JNameOf AST nodes and is therefore meaningless to implement in Development Mode.
	 * If the requested member is a method, the method will not be devirtualized, inlined or prunned.
	 *
	 * @param jsniIdent a string literal specifying a type, field, or method. Raw
	 *          type names may also be used to obtain the name of the type's seed
	 *          function.
	 * @return the name by which the named member can be accessed at runtime, or
	 *         <code>null</code> if the requested member has been pruned from the
	 *         output.
	 */
	public static String getNameOf(String jsniIdent) {
		/*
		 * In Production Mode, the compiler directly replaces calls to this method
		 * with a string literal expression.
		 */
		assert !GWT.isScript() : "ReplaceRebinds failed to replace this method";
		throw new UnsupportedOperationException(
				"Impl.getNameOf() is unimplemented in Development Mode");
	}

	public static native String getPermutationStrongName() /*-{
    return $strongName;
  }-*/;

	/**
	 * Set an uncaught exception handler to spy on uncaught exceptions in unit
	 * tests.
	 * <p>
	 * Setting this method will not interfere with any exception handling logic;
	 * i.e. {@link GWT#getUncaughtExceptionHandler()} will still return null if a
	 * handler is not set via {@link GWT#setUncaughtExceptionHandler}.
	 */
	public static void setUncaughtExceptionHandlerForTest(
			UncaughtExceptionHandler handler) {
	}

	public static void reportUncaughtException(Throwable e) {
	}

	/**
	 * Indicates if <code>$entry</code> has been called.
	 */
	public static boolean isEntryOnStack() {
		return true;
	}

	/**
	 * Indicates if <code>$entry</code> is present on the stack more than once.
	 */
	public static boolean isNestedEntry() {
		return false;
	}

	/**
	 * Implicitly called by JavaToJavaScriptCompiler.findEntryPoints().
	 */
	public static native JavaScriptObject registerEntry() /*-{
    return @Impl::entry(*);
  }-*/;

	public static void maybeInitializeWindowOnError() {}
}
