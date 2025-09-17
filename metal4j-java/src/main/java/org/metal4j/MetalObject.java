package org.metal4j;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.file.Path;

public interface MetalObject {

    Linker LINKER = Linker.nativeLinker();
    Path LIB_PATH = Path.of("metal4j-java/natives/libmetal4j.dylib");
    SymbolLookup LOOKUP = SymbolLookup.libraryLookup(LIB_PATH.toAbsolutePath().toString(), Arena.global());

    MethodHandle METAL_RELEASE_OBJECT = LINKER.downcallHandle(
        LOOKUP.find("metal_release_object").orElse(null),
        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
    );

    default void release() throws Throwable {
        METAL_RELEASE_OBJECT.invokeExact(handle());
    }

    MemorySegment handle();
}
