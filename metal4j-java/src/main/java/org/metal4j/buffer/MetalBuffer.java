package org.metal4j.buffer;

import org.metal4j.MetalObject;
import org.metal4j.state.MetalDevice;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public record MetalBuffer(MemorySegment handle, int length) implements MetalObject {

    public static final MethodHandle METAL_NEW_BUFFER = LINKER.downcallHandle(
        LOOKUP.find("metal_new_buffer").orElse(null),
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT)
    );
    public static final MethodHandle METAL_BUFFER_CONTENTS = LINKER.downcallHandle(
        LOOKUP.find("metal_buffer_contents").orElse(null),
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    );

    public static MetalBuffer allocate(MetalDevice device, int length) throws Throwable {
        MemorySegment ptr = (MemorySegment) METAL_NEW_BUFFER.invokeExact(device.handle(), length);
        return new MetalBuffer(ptr, length);
    }

    public MemorySegment contents() throws Throwable {
        return (MemorySegment) METAL_BUFFER_CONTENTS.invokeExact(handle());
    }

    public ByteBuffer asByteBuffer() throws Throwable {
        return contents()
            .reinterpret(length)
            .asByteBuffer()
            .order(ByteOrder.nativeOrder());
    }
}
