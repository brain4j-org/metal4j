package org.metal4j;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

import static org.metal4j.Metal4J.LINKER;
import static org.metal4j.Metal4J.LOOKUP;

public interface MetalObject {

    MethodHandle metalReleaseObject = LINKER.downcallHandle(
        LOOKUP.find("metal_release_object").get(),
        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
    );

    MemorySegment handle();

    default void release() throws Throwable {
        metalReleaseObject.invokeExact(handle());
    }
}
