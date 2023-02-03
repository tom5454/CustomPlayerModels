package com.tom.cpm.blockbench.util;

import java.util.List;
import java.util.function.Function;

import com.tom.cpm.blockbench.PluginStart;
import com.tom.cpm.blockbench.convert.WarnEntry;
import com.tom.cpm.blockbench.proxy.Dialog;
import com.tom.cpm.web.client.util.Clipboard;
import com.tom.cpm.web.client.util.I18n;
import com.tom.ugwt.client.ExceptionUtil;
import com.tom.ugwt.client.GlobalFunc;

import elemental2.core.JsError;
import elemental2.dom.DomGlobal;
import elemental2.promise.Promise;
import elemental2.promise.Promise.PromiseExecutorCallbackFn.ResolveCallbackFn;
import jsinterop.annotations.JsFunction;

public class PopupDialogs {
	public static Dialog errorDialog;
	public static Dialog infoDialog;
	public static Dialog warnDialog;
	public static GlobalFunc copyErrorFunc;

	static {
		Dialog.DialogProperties dctr = new Dialog.DialogProperties();
		dctr.id = "cpm_error";
		dctr.title = I18n.get("bb-label.error.title");
		dctr.lines = new String[] {"?"};
		dctr.singleButton = true;
		errorDialog = new Dialog(dctr);

		dctr = new Dialog.DialogProperties();
		dctr.id = "cpm_warn";
		dctr.title = I18n.get("label.cpm.warning");
		dctr.lines = new String[] {"?"};
		warnDialog = new Dialog(dctr);

		dctr = new Dialog.DialogProperties();
		dctr.id = "cpm_info";
		dctr.title = I18n.get("label.cpm.info");
		dctr.lines = new String[] {"?"};
		infoDialog = new Dialog(dctr);

		copyErrorFunc = GlobalFunc.pushGlobalFunc(CopyErrorCallback.class, err -> Clipboard.writeText(DomGlobal.atob(err)));
		PluginStart.cleanup.add(copyErrorFunc);
	}

	public static void displayError(String msg, Object err) {
		if(err instanceof PopupDialogs.CancelException)return;
		String errorMsg;
		if(err instanceof Throwable) {
			String st = ExceptionUtil.getStackTrace((Throwable) err);
			errorMsg = msg + "\n" + st;
			DomGlobal.console.error(st);
		} else if(err instanceof JsError) {
			String st = ExceptionUtil.getStackTrace((JsError) err);
			errorMsg = msg + "\n" + st;
			DomGlobal.console.error(st);
		} else {
			DomGlobal.console.log(err);
			errorMsg = msg + "\n" + err;
		}
		errorDialog.setLines(msg, "<br>", err.toString(), "<br>", I18n.get("bb-label.pleaseReport"), "<br>", "<button onclick='" + copyErrorFunc + "(\"" + DomGlobal.btoa(errorMsg) + "\")'>" + I18n.get("bb-button.copyErrorMsg") + "</button>");
		errorDialog.show();
	}

	public static void displayMessage(String msg) {
		infoDialog.setLines(msg);
		infoDialog.show();
	}

	public static class CancelException extends Exception {
	}

	@JsFunction
	public interface QuickFixCallback {
		void run(int id);
	}

	@JsFunction
	public interface CopyErrorCallback {
		void run(String b64);
	}

	public interface WarnCallback {
		Promise<Void> updateState(Function<List<WarnEntry>, Promise<Void>> openWarn);
	}

	private static class WarnDialogHandler {
		private WarnCallback cb;
		private List<WarnEntry> ent;
		private boolean quickFixed;
		private GlobalFunc quickFixFunc;
		private ResolveCallbackFn<Void> res;

		public WarnDialogHandler(List<WarnEntry> ent, WarnCallback cb) {
			this.ent = ent;
			this.cb = cb;
		}

		public Promise<Void> display() {
			return new Promise<Void>((res, rej) -> {
				this.res = res;
				quickFixFunc = GlobalFunc.pushGlobalFunc(QuickFixCallback.class, this::applyQuickFix);
				warnDialog.onCancel = () -> {
					rej.onInvoke(new CancelException());
					return true;
				};
				warnDialog.onConfirm = dr -> {
					if(quickFixed) {
						res.onInvoke(cb.updateState(w -> Promise.resolve((Void) null)));
					} else {
						res.onInvoke((Void) null);
					}
					return true;
				};
				fillWarnDialog(ent, quickFixFunc);
			}).finally_(() -> {
				if(quickFixFunc != null)quickFixFunc.run();
			});
		}

		private void applyQuickFix(int id) {
			ent.get(id).runQuickFix().then(f -> {
				if(f) {
					cb.updateState(w -> {
						this.ent = w;
						fillWarnDialog(w, quickFixFunc);
						return Promise.reject(null);
					}).then(__ -> {
						warnDialog.hide();
						res.onInvoke((Void) null);
						return null;
					}).catch_(e -> null);
				}
				return null;
			});
			quickFixed = true;
		}
	}

	public static Promise<Void> runTaskWithWarning(WarnCallback cb) {
		return cb.updateState(w -> displayWarning(cb, w));
	}

	public static Promise<Void> runTaskWithWarning(WarnCallback cb, Runnable onWarn) {
		return cb.updateState(w -> {
			onWarn.run();
			return displayWarning(cb, w);
		});
	}

	public static Promise<Void> displayWarning(WarnCallback cb, List<WarnEntry> ent) {
		return new WarnDialogHandler(ent, cb).display();
	}

	private static void fillWarnDialog(List<WarnEntry> ent, GlobalFunc quickFix) {
		StringBuilder sb = new StringBuilder();
		sb.append("<div style='display: flex;flex-direction: column;'><style>.cpm_autofix_button {float: right;} .cpm_warn_msg {text-align: left;}</style>");
		ent.stream().filter(w -> !w.isFixed()).sorted().forEach(w -> {
			String msg = w.getMessage();
			sb.append("<div class='tool widget cpm_warn_msg'>");
			if(w.getTooltip() != null) {
				sb.append("<div class='tooltip' style='margin-left: 0px;'>" + w.getTooltip() + "</div>");
			}
			sb.append(msg);
			if(w.canQuickFix()) {
				sb.append("<button onclick='" + quickFix + "(" + ent.indexOf(w) + ")' class='cpm_autofix_button'>" + I18n.get("bb-button.autoFix") + "</button>");
			}
			sb.append("</div>");
		});
		sb.append("</div>");
		warnDialog.setLines(sb.toString());
		warnDialog.show();
	}
}
