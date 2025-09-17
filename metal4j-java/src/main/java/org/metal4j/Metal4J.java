package org.metal4j;

import java.lang.foreign.*;
import java.nio.file.Path;

public class Metal4J {

    public static final Linker LINKER = Linker.nativeLinker();
    public static final Path LIB_PATH = Path.of("metal4j-java/natives/libmetal4j.dylib");
    public static final SymbolLookup LOOKUP = SymbolLookup.libraryLookup(
        LIB_PATH.toAbsolutePath().toString(), Arena.global()
    );
}
