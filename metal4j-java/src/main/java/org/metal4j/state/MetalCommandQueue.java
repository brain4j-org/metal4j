package org.metal4j.state;

import org.metal4j.MetalObject;
import org.metal4j.buffer.MetalCommandBuffer;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

import static org.metal4j.Metal4J.*;

public record MetalCommandQueue(MemorySegment handle) implements MetalObject {

    public static final MethodHandle METAL_CREATE_COMMAND_QUEUE = LINKER.downcallHandle(
        LOOKUP.find("metal_create_command_queue").get(),
        FunctionDescriptor.of(ValueLayout.ADDRESS, // return MTLCommandQueue*
            ValueLayout.ADDRESS)                   // device (MTLDevice*)
    );

    public static MetalCommandQueue create(MetalDevice device) throws Throwable {
        MemorySegment queuePtr = (MemorySegment) METAL_CREATE_COMMAND_QUEUE.invokeExact(device.handle());

        if (queuePtr == null) {
            throw new RuntimeException("Failed to create Metal command queue");
        }

        return new MetalCommandQueue(queuePtr);
    }

    public MetalCommandBuffer makeCommandBuffer() throws Throwable {
        return MetalCommandBuffer.make(this);
    }
}

