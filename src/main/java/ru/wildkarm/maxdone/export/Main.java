package ru.wildkarm.maxdone.export;

import java.io.File;
import java.nio.file.Path;

import org.apache.log4j.PropertyConfigurator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {
    
    public static void main(String[] args) {
        
        
        try {
            initlogger();
            log.info("SART with args = {}", args);

            JsonParser.testJsonParsing();
            
            
            String inputPath = "..\\..\\data";
            if(args.length > 0) {
                inputPath = args[0].replace("\"", "");
            }

            MaxDoneDecomp.start(inputPath);
        } catch (Exception e) {
            log.error("main ERROR: ", e);
        }
    }
    
    public static void initlogger() throws Exception {
        PropertyConfigurator.configure(Main.getConfigPath("log.properties"));
        log.info("**************** START TEST POM ***************");
    }

    public static String getConfigPath(String filename) throws Exception {

        File file = new File("config/" + filename);
        System.out.println("config1 path = " + file.getAbsolutePath());

        if (!file.exists()) {
            ClassLoader classLoader = Main.class.getClassLoader();
            file = new File(classLoader.getResource("config/" + filename).getFile());
            System.out.println("config2 path = " + file.getAbsolutePath());
        }

        if (!file.exists()) {
            Path parentPath = Path.of(
                    Main.class
                            .getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI())
                    .getParent();

            file = Path.of(parentPath.toAbsolutePath().toString(), "/config", filename).toFile();
            System.out.println("config3 path = " + file.getAbsolutePath());
        }

        if (!file.exists()) {
            throw new Exception("WOW PROBLEM!");
        }

        return file.getAbsolutePath();
    }

}
