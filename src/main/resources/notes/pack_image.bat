"%JAVA_HOME%\jpackage" --type app-image --input project\ --runtime-image runtime_base --name MaxdoneExport --main-jar project\maxdone-export.jar --main-class ru.wildkarm.maxdone.export.Main --java-options '-Dfile.encoding=UTF-8'

echo done
pause
