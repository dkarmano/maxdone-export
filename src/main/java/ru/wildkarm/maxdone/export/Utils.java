package ru.wildkarm.maxdone.export;

import java.net.URISyntaxException;
import java.nio.file.Path;

public class Utils {
    public static Path getPathFromJar(String... suffix) throws URISyntaxException {
        Path parentPath = Path.of(
                Main.class
                        .getProtectionDomain()
                        .getCodeSource()
                        .getLocation()
                        .toURI())
                .getParent();

        return Path.of(parentPath.toAbsolutePath().toString(), suffix);

    }
}
