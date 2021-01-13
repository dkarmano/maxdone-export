"%JAVA_HOME%\jpackage" --type msi --input project\ --runtime-image runtime_base --name MaxdoneExport0 --name MaxdoneExport --main-jar project\maxdone-export.jar --main-class ru.wildkarm.maxdone.export.Main --java-options '-Dfile.encoding=UTF-8'  --win-shortcut --win-dir-chooser


echo done
pause