package org.metal4j.state;

import org.metal4j.MetalObject;
import org.metal4j.buffer.MetalBuffer;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

import static org.metal4j.Metal4J.*;

public record MetalEncoder(MemorySegment handle) implements MetalObject, AutoCloseable {

    public static final MethodHandle METAL_ENCODER_SET_BUFFER = LINKER.downcallHandle(
            LOOKUP.find("metal_encoder_set_buffer").get(),
            FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT)
    );
    public static final MethodHandle METAL_DISPATCH = LINKER.downcallHandle(
        LOOKUP.find("metal_dispatch").get(),
        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT)
    );
    public static final MethodHandle METAL_END_ENCODING = LINKER.downcallHandle(
        LOOKUP.find("metal_end_encoding").get(),
        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
    );

    public void setBuffer(MetalBuffer buf, int index) throws Throwable {
        METAL_ENCODER_SET_BUFFER.invokeExact(handle, buf.handle(), index);
    }

    public void dispatch(int threadCount) throws Throwable {
        METAL_DISPATCH.invokeExact(handle, threadCount);
    }

    public void endEncoding() throws Throwable {
        METAL_END_ENCODING.invokeExact(handle);
    }

    @Override
    public void close() {
        try {
            endEncoding();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
