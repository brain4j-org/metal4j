package org.metal4j;

import org.metal4j.state.MetalDevice;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

import static org.metal4j.MetalObject.*;

public class Metal {

    public static final MethodHandle METAL_CREATE_SYSTEM_DEVICE = LINKER.downcallHandle(
        LOOKUP.find("metal_create_system_device").orElse(null),
        FunctionDescriptor.of(ValueLayout.ADDRESS)
    );

    public static MetalDevice createSystemDevice() throws Throwable {
        MemorySegment ptr = (MemorySegment) METAL_CREATE_SYSTEM_DEVICE.invokeExact();
        return new MetalDevice(ptr);
    }
}
