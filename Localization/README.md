# CPM Translation guide

## Update existing
Missing entries will have an `UNLOCALIZED: ` prefix translate these strings.  
Potentially outdated entries will have a `MODIFIED: ` prefix, check the `start.json` and see if the existing translation matches the English version.
You can leave any UNLOCALIZED, and MODIFIED entries in the file, it won't show up in the release version.

## Creating a new file:
Check if your language is already present!  
Start with the `start.json` from this folder, copy and rename it to your locale.  

## Base files
If your language has multiple variants you can create a base file and overrides for each variant.  
Create a json called `<language id>-base.json` where you translate all of the entries.  
Then create override files with your locale id and place a `meta-base` tag at the top with the value of `<language id>`.  

## Formatting:
- `\\` characters creates line breaks.  
- `%s`, `%d` are variables. Do not remove any from the text. You can change the order by typing `%<number>$s` where the `<number>` is the id of the variable starting from 1.  

Make sure to save the file in `UTF-8` format.  

## Publishing
Create a Pull Request with your changes.  
Don't touch the `.version` files as these will be generated/updated automatically once the PR is merged.  

## Embed Locale
Some translations have to be embedded in code due to security issues (TinyFD on Linux)  
Use the `meta.cpm.embedLocale` tag and enter your language locale.  

# Wiki Translations
Check if your language is already present!  
Start with the `start` folder from this folder, copy and rename it to your locale.  
The locale name must follow github's naming system. Example: `en-US`  

Don't change any Markdown links `[...](...)`, the correct localized link will be inserted by the page preprocessor.  
When translating headers (`# ...`), please keep the original title inbetween `{...}`.  
Example: `# Translated title {Original Title}`  
This is necessary to keep the links working on the pages.  

## Base folder
If your language has multiple variants you can create a base folder and overrides for each variant.    
Create a folder called `<language id>-base` where you translate all of the entries.  
Then create an entry into the `config.json` to point at the base folder.

## config.json

### Base
Load extra files from the base folder  
Example `"base": "en-base"`, load from the `en-base` folder
