## How to develop using GWT

Here is a list of gradle tasks for development:
- `devMode`: Runs the GWT super-dev-mode with the correct classpath
- `compileEditor`: Compiles the web editor (output is in war/cpmweb/<name>.cache.js)
- `compileViewer`: Compiles the web viewer
- `compileBlockbench`: Compiles the blockbench plugin (output is in war/cpmblockbench/cpmblockbench.nocache.js, rename it to `cpm_plugin.js` if you want to load it)
- `blockbenchDevServer`: Run the dev server for the blockbench plugin

Currently you have to use `-DuseLib=true` argument or it will fail to compile.

Fixing weird issues with GWT:
- Delete the contents of `gwt-temp`, and `gwt-unitCache` and retry (restart `devMode`).

## Using devMode
The server is available at `http://127.0.0.1:8888/CPMWeb.html?app=<app name>`.  
Replace `<app name>`:
- viewer
- fbxtool
Remove the `?app...` to open the CPM Editor.