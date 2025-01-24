package com.tom.cpm.blockbench;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import com.tom.cpl.gui.elements.FileChooserPopup;
import com.tom.cpl.gui.elements.FileChooserPopup.FileFilter;
import com.tom.cpm.blockbench.ee.EmbeddedEditorHandler;
import com.tom.cpm.blockbench.proxy.electron.Electron;
import com.tom.cpm.blockbench.proxy.electron.ElectronDialog.DialogProperties;
import com.tom.cpm.blockbench.proxy.electron.ElectronDialog.FileFilterJS;
import com.tom.cpm.blockbench.proxy.electron.FileSystem;
import com.tom.cpm.blockbench.proxy.electron.FileSystem.Buffer;
import com.tom.cpm.blockbench.util.SystemFileChooser;
import com.tom.cpm.web.client.FS.IFS;
import com.tom.cpm.web.client.IFile;
import com.tom.cpm.web.client.java.Java;
import com.tom.cpm.web.client.java.io.FileNotFoundException;

import elemental2.promise.Promise;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;

public class BlockBenchFS implements IFS {
	public static final FileSystem fs = Js.uncheckedCast(require("fs"));

	@Override
	public String getContentSync(String path) throws FileNotFoundException {
		try {
			return fs.readFileSync(path).toString("base64");
		} catch (Exception e) {
			throw new FileNotFoundException(e.getMessage());
		}
	}

	@Override
	public Promise<String> getContent(String path) {
		return new Promise<>((res, rej) -> {
			fs.readFile(path, (err, dt) -> {
				if(err != null)rej.onInvoke(new FileNotFoundException(err.toString()));
				else res.onInvoke(dt.toString("base64"));
			});
		});
	}

	@Override
	public Promise<Void> setContent(String path, String content) {
		return new Promise<>((res, rej) -> {
			fs.writeFile(path, Buffer.from(content, "base64"), err -> {
				if(err != null)rej.onInvoke(new FileNotFoundException(err.toString()));
				else res.onInvoke((Void) null);
			});
		});
	}

	@Override
	public Promise<File> mount(elemental2.dom.File file) {
		FileEx f = Js.uncheckedCast(file);
		return Promise.resolve(new File(f.path));
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "File")
	public static class FileEx extends elemental2.dom.File {
		public String path;
	}

	@Override
	public void mount(String b64, String name) {
	}

	@Override
	public boolean needFileManager() {
		return false;
	}

	@Override
	public IFile getImpl(IFile parent, String path) {
		if(parent != null)return new FileImpl((FileImpl) parent, path);
		else return new FileImpl(path);
	}

	private static class FileImpl implements IFile {
		private String path;

		public FileImpl(String path) {
			this.path = path;
		}

		public FileImpl(FileImpl parent, String path) {
			this.path = parent.path + "/" + path;
		}

		@Override
		public String getName() {
			return PATH.basename(path);
		}

		@Override
		public boolean exists() {
			return fs.existsSync(getAbsolutePath());
		}

		@Override
		public IFile getParentFile() {
			return new FileImpl(PATH.dirname(path));
		}

		@Override
		public void mkdirs() {
			fs.mkdirs(getAbsolutePath());
		}

		@Override
		public boolean isDirectory() {
			String path = getAbsolutePath();
			return fs.existsSync(path) && fs.lstatSync(path).isDirectory();
		}

		@Override
		public String getAbsolutePath() {
			return PATH.resolve(WD, path);
		}

		@Override
		public boolean isHidden() {
			return false;
		}

		@Override
		public String getCanonicalPath() throws IOException {
			return getAbsolutePath();
		}

		@Override
		public boolean isFile() {
			String path = getAbsolutePath();
			return fs.existsSync(path) && !fs.lstatSync(path).isDirectory();
		}

		@Override
		public String[] list() {
			return fs.readdirSync(getAbsolutePath());
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((path == null) ? 0 : path.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			FileImpl other = (FileImpl) obj;
			if (path == null) {
				if (other.path != null) return false;
			} else if (!path.equals(other.path)) return false;
			return true;
		}

		@Override
		public boolean isRoot() {
			String path = getAbsolutePath();
			return PATH.dirname(path).equals(path);
		}
	}

	@JsMethod(namespace = JsPackage.GLOBAL)
	private static native Object require(String name);

	@JsProperty(namespace = JsPackage.GLOBAL, name = "process.platform")
	private static native String getPlatform();

	@JsProperty(namespace = JsPackage.GLOBAL, name = "__dirname")
	private static native String getAppPackagePath();

	@JsProperty(namespace = JsPackage.GLOBAL, name = "_editorWorkDir")
	private static native String getEmbeddedWorkingDirectory();

	@JsType(isNative = true, namespace = JsPackage.GLOBAL)
	public static class PathAPI {
		public native String dirname(String path);
		public native String resolve(String... path);
		public native String basename(String path);
		public native boolean isAbsolute(String path);
		public native String join(String... path);
	}

	public static final PathAPI PATH = Js.uncheckedCast(require("path"));
	private static final String WD = Js.typeof(getEmbeddedWorkingDirectory()) != "undefined" ? getEmbeddedWorkingDirectory() : PATH.resolve(Electron.app.getPath("userData"), "CPM Plugin Data");
	private static final boolean WIN = getPlatform().startsWith("win");

	static {
		fs.mkdirs(WD);
	}

	@Override
	public String getWorkDir() {
		return WD;
	}

	@Override
	public CompletableFuture<File> openFileChooser(FileChooserPopup fc) {
		CompletableFuture<File> f = new CompletableFuture<>();
		Java.promiseToCf(openFileChooser0(fc), f);
		return f;
	}

	private static Promise<File> openFileChooser0(FileChooserPopup fc) {
		DialogProperties dp = new DialogProperties();
		dp.defaultPath = fc.getCurrentDirectory().getAbsolutePath() + "/";
		if(WIN)dp.defaultPath = dp.defaultPath.replace('/', '\\');
		dp.title = fc.getTitle();
		if(fc.getFilter() instanceof FileFilter) {
			FileFilter ff = (FileFilter) fc.getFilter();
			if(ff.isFolder()) {
				dp.properties = new String[] {"openDirectory"};
				return openFileChooser1(false, dp);
			} else if(ff.getExt() != null) {
				dp.filters = new FileFilterJS[] {FileFilterJS.make(fc.getDescription(), ff.getExt())};
			}
		}
		if(fc.isSaveDialog()) {
			return openFileChooser1(true, dp);
		} else {
			dp.properties = new String[] {"openFile"};
			return openFileChooser1(false, dp);
		}
	}

	private static Promise<File> openFileChooser1(boolean save, DialogProperties dialog) {
		Promise<String> pr;
		if (EmbeddedEditorHandler.isRunningInEmbeddedEditor()) {
			pr = EmbeddedEditorHandler.openFileDialog(save, dialog);
		} else {
			if (save) {
				pr = SystemFileChooser.showSaveDialog(dialog);
			} else {
				pr = SystemFileChooser.showOpenDialog(dialog);
			}
		}
		return pr.then(p -> Promise.resolve(p != null ? new File(p) : null));
	}

	public static String getLibrary(String name) {
		String path = PATH.join(getAppPackagePath(), "lib", name);
		return Js.cast(fs.readFileSync(path, "utf8"));
	}
}
