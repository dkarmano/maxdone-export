package ru.wildkarm.maxdone.export;

import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;

import java.io.File;

@Slf4j
public class Main {
    
    public static void main(String[] args) {
        try {
            initLogger();
            log.info("START with args = {}", args);
            String inputPath = ConfigFileProvider.getInnerFile("..\\input_data").getAbsolutePath();
            if (args.length > 0) {
                inputPath = args[0].replace("\"", "");
                File inputFile = new File(inputPath);
                if (!inputFile.isAbsolute()) {
                    inputPath = ConfigFileProvider.getPathFromJar("..\\", inputPath).toString();
                }
            }

            MaxDoneDecomp.start(inputPath);
        } catch (Exception e) {
            log.error("main ERROR: ", e);
        }
    }
    
    public static void initLogger() throws Exception {
        PropertyConfigurator.configure(ConfigFileProvider.getConfigPath("log.properties"));
        //DOMConfigurator.configure(ConfigFileProvider.getConfigPath("log4j.xml"));
        log.info("**************** START maxdone.export ***************");
    }

}
