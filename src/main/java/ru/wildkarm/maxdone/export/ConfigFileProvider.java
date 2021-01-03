package ru.wildkarm.maxdone.export;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

public class ConfigFileProvider {

    public static final String CONFIG = "config/";

    public static Path getPathFromJar(String... suffix) throws URISyntaxException {
        Path parentPath = Path.of(
                ConfigFileProvider.class.getProtectionDomain()
                        .getCodeSource()
                        .getLocation().toURI())
                .getParent();

        return Path.of(parentPath.toAbsolutePath().toString(), suffix).toAbsolutePath();
    }

    public static String getConfigPath(String filename) throws FileNotFoundException {
        return getInnerFile(CONFIG + filename).getAbsolutePath();
    }

    public static File getInnerFile(String filename) throws FileNotFoundException {

        File file = new File(filename);
        System.out.println("InnerFile path1 = " + file.getAbsolutePath());

        if (!file.exists()) {
            ClassLoader classLoader = Main.class.getClassLoader();
            URL resource = classLoader.getResource(filename);
            if (resource != null) {
                file = new File(resource.getFile());
                System.out.println("InnerFile path2 = " + file.getAbsolutePath());
            }
        }

        if (!file.exists()) {
            try {
                file = getPathFromJar(filename).toFile();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            System.out.println("InnerFile path3 = " + file.getAbsolutePath());
        }

        if (!file.exists()) {
            throw new FileNotFoundException("FILE " + filename + " IS NOT FOUND");
        }

        return file;
    }
}
