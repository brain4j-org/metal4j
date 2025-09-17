package org.metal4j.state;

import org.metal4j.MetalObject;
import org.metal4j.buffer.MetalBuffer;
import org.metal4j.kernel.MetalLibrary;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

import static org.metal4j.Metal4J.*;

public record MetalDevice(MemorySegment handle) implements MetalObject {

    public static final MethodHandle METAL_DEVICE_NAME = LINKER.downcallHandle(
        LOOKUP.find("metal_device_name").get(),
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    );
    public static final MethodHandle METAL_CREATE_SYSTEM_DEVICE = LINKER.downcallHandle(
        LOOKUP.find("metal_create_system_device").get(),
        FunctionDescriptor.of(ValueLayout.ADDRESS)
    );

    public static MetalDevice createSystemDevice() throws Throwable {
        MemorySegment ptr = (MemorySegment) METAL_CREATE_SYSTEM_DEVICE.invokeExact();
        return new MetalDevice(ptr);
    }

    public String getName() throws Throwable {
        MemorySegment cstrPtr = (MemorySegment) METAL_DEVICE_NAME.invokeExact(handle);
        return cstrPtr.reinterpret(Long.MAX_VALUE).getString(0);
    }

    public MetalLibrary makeLibrary(String source) throws Throwable {
        return MetalLibrary.makeLibrary(this, source);
    }

    public MetalCommandQueue makeCommandQueue() throws Throwable {
        return MetalCommandQueue.create(this);
    }

    public MetalBuffer allocate(int length) throws Throwable {
        return MetalBuffer.allocate(this, length);
    }
}
