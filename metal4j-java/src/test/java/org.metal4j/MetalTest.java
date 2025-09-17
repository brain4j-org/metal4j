package org.metal4j;

import org.metal4j.buffer.MetalBuffer;
import org.metal4j.buffer.MetalCommandBuffer;
import org.metal4j.state.MetalDevice;
import org.metal4j.kernel.MetalFunction;
import org.metal4j.kernel.MetalLibrary;
import org.metal4j.kernel.MetalPipeline;
import org.metal4j.state.MetalCommandQueue;
import org.metal4j.state.MetalEncoder;

import java.util.Arrays;

public class MetalTest {
    public static void main(String[] args) throws Throwable {
        MetalDevice device = MetalDevice.createSystemDevice();
        System.out.println("Device: " + device.getName());

        String src = """
        kernel void vadd(
            device const float* A [[ buffer(0) ]],
            device const float* B [[ buffer(1) ]],
            device float* C [[ buffer(2) ]],
            uint id [[ thread_position_in_grid ]]
        ) {
            C[id] = A[id] + B[id];
        }
        """;

        MetalLibrary lib = device.makeLibrary(src);
        MetalFunction function = lib.makeFunction("vadd");
        MetalPipeline pipeline = function.makePipeline();

        System.out.println("Kernel compiled and queue ready!");

        MetalBuffer bufA = device.allocate(8 * Float.BYTES);
        MetalBuffer bufB = device.allocate(8 * Float.BYTES);
        MetalBuffer bufC = device.allocate(8 * Float.BYTES);

        bufA.asByteBuffer().asFloatBuffer().put(new float[]{1,2,3,4,5,6,7,8});
        bufB.asByteBuffer().asFloatBuffer().put(new float[]{10,20,30,40,50,60,70,80});

        MetalCommandQueue queue = device.makeCommandQueue();
        MetalCommandBuffer commandBuffer = queue.makeCommandBuffer();

        try (MetalEncoder encoder = commandBuffer.makeEncoder(pipeline)) {
            encoder.setBuffer(bufA, 0);
            encoder.setBuffer(bufB, 1);
            encoder.setBuffer(bufC, 2);

            for (int i = 0; i < 100; i++) {
                encoder.dispatch(8);
            }
        }

        long start = System.nanoTime();

        commandBuffer.commit();
        commandBuffer.waitUntilCompleted();

        long end = System.nanoTime();
        double took = (end - start) / 1e6;
        System.out.println("Took " + took + " millis");

        float[] C = new float[8];
        bufC.asByteBuffer().asFloatBuffer().get(C);

        System.out.println("Computed on GPU: " + Arrays.toString(C));
    }
}
