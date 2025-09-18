package org.metal4j;

import java.io.IOException;
import java.io.InputStream;
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public interface MetalObject {
    
    Linker LINKER = Linker.nativeLinker();
    SymbolLookup LOOKUP = loadFromResources("/libmetal4j.dylib");
    MethodHandle METAL_RELEASE_OBJECT = LINKER.downcallHandle(
        LOOKUP.find("metal_release_object").orElse(null),
        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
    );

    default void release() throws Throwable {
        METAL_RELEASE_OBJECT.invokeExact(handle());
    }
    
    static SymbolLookup loadFromResources(String resourceName) {
        try (InputStream in = MetalObject.class.getResourceAsStream(resourceName)) {
            if (in == null) {
                throw new IllegalArgumentException("Resource not found: " + resourceName);
            }
            
            String suffix = resourceName.substring(resourceName.lastIndexOf('.'));
            Path tempFile = Files.createTempFile("nativeLib", suffix);
            
            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
            tempFile.toFile().deleteOnExit();
            
            return SymbolLookup.libraryLookup(tempFile.toString(), Arena.global());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    MemorySegment handle();
}
