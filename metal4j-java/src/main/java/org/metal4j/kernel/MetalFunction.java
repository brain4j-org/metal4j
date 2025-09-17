package org.metal4j.kernel;

import org.metal4j.MetalObject;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

import static org.metal4j.Metal4J.*;

public record MetalFunction(MemorySegment handle) implements MetalObject {

    public static final MethodHandle METAL_CREATE_FUNCTION = LINKER.downcallHandle(
        LOOKUP.find("metal_create_function").get(),
        FunctionDescriptor.of(ValueLayout.ADDRESS, // return MTLFunction*
            ValueLayout.ADDRESS,                  // library (MTLLibrary*)
            ValueLayout.ADDRESS)                  // function name (char*)
    );

    public static MetalFunction create(MetalLibrary library, String name) throws Throwable {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment fnName = arena.allocateFrom(name);
            MemorySegment fnPtr = (MemorySegment) METAL_CREATE_FUNCTION.invokeExact(library.handle(), fnName);

            if (fnPtr == null) {
                throw new RuntimeException("Function '" + name + "' not found in library");
            }

            return new MetalFunction(fnPtr);
        }
    }

    public MetalPipeline makePipeline() throws Throwable {
        return MetalPipeline.makePipeline(this);
    }
}
