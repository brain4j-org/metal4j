package org.metal4j;

import org.jocl.*;
import org.metal4j.buffer.MetalBuffer;
import org.metal4j.buffer.MetalCommandBuffer;
import org.metal4j.state.MetalDevice;
import org.metal4j.kernel.MetalFunction;
import org.metal4j.kernel.MetalLibrary;
import org.metal4j.kernel.MetalPipeline;
import org.metal4j.state.MetalCommandQueue;
import org.metal4j.state.MetalEncoder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.jocl.CL.*;

public class MetalTest {

    public static final int N = 512 * 512 * 512;
    public static final int E = 16;

    public static void main(String[] args) throws Throwable {
       benchmarkMetal();
       benchmarkOpenCL();
    }

    private static void benchmarkMetal() throws Throwable {
        System.out.println("========= Benchmark for: Metal =========");

        byte[] raw = Files.readAllBytes(Path.of("resources/vector_add.metal"));
        String src = new String(raw);

        MetalDevice device = Metal.createSystemDevice();

        MetalLibrary lib = device.makeLibrary(src);
        MetalFunction function = lib.makeFunction("add");
        MetalPipeline pipeline = function.makePipeline();

        MetalBuffer bufA = device.makeBuffer(N * Float.BYTES);
        MetalBuffer bufB = device.makeBuffer(N * Float.BYTES);
        MetalBuffer bufC = device.makeBuffer(N * Float.BYTES);

        System.out.println("created buffers");
        float[] a = new float[N];
        float[] b = new float[N];

        for (int i = 0; i < N; i++) {
            a[i] = i;
            b[i] = i + 1;
        }

        bufA.put(a);
        bufB.put(b);

        MetalCommandQueue queue = device.makeCommandQueue();
        MetalCommandBuffer commandBuffer = queue.makeCommandBuffer();

        try (MetalEncoder encoder = commandBuffer.makeEncoder(pipeline)) {
            encoder.setBuffer(bufA, 0);
            encoder.setBuffer(bufB, 1);
            encoder.setBuffer(bufC, 2);

            encoder.dispatchThreads(N, 1, 1, 32, 1, 1);
        }

        long start = System.nanoTime();
        commandBuffer.commit();
        commandBuffer.waitUntilCompleted();
        long end = System.nanoTime();

        double took = (end - start) / 1e6;
        System.out.println("Took " + took + " millis");

        float[] C = new float[E];
        bufC.get(C);

        System.out.println("Device: " + device.getName());
        System.out.println("Computed on GPU: " + Arrays.toString(C));
    }

    private static void benchmarkOpenCL() throws IOException {
        System.out.println("======== Benchmark for: OpenCL ========");
        CL.setExceptionsEnabled(true);

        cl_platform_id[] platforms = new cl_platform_id[1];
        clGetPlatformIDs(1, platforms, null);

        cl_device_id[] devices = new cl_device_id[1];
        clGetDeviceIDs(platforms[0], CL_DEVICE_TYPE_GPU, 1, devices, null);

        cl_device_id device = devices[0];

        byte[] deviceNameBytes = new byte[1024];
        clGetDeviceInfo(device, CL_DEVICE_NAME, 1024, Pointer.to(deviceNameBytes), null);
        String deviceName = new String(deviceNameBytes).trim();
        System.out.println(STR."OpenCL Device: \{deviceName}");

        cl_context context = clCreateContext(
            null, 1, new cl_device_id[]{device}, null, null, null);

        cl_command_queue queue =
            clCreateCommandQueue(context, device, 0, null);

        String kernelSource = new String(Files.readAllBytes(Path.of("resources/vector_add.cl")));

        cl_program program = clCreateProgramWithSource(context, 1,
            new String[]{kernelSource}, null, null);
        clBuildProgram(program, 0, null, null, null, null);

        cl_kernel kernel = clCreateKernel(program, "vecAdd", null);

        float[] a = new float[N];
        float[] b = new float[N];

        for (int i = 0; i < N; i++) {
            a[i] = i;
            b[i] = i + 1;
        }

        cl_mem bufA = clCreateBuffer(context,
            CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
            Sizeof.cl_float * N, Pointer.to(a), null);

        cl_mem bufB = clCreateBuffer(context,
            CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
            Sizeof.cl_float * N, Pointer.to(b), null);

        cl_mem bufC = clCreateBuffer(context,
            CL_MEM_WRITE_ONLY,
            Sizeof.cl_float * N, null, null);

        clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(bufA));
        clSetKernelArg(kernel, 1, Sizeof.cl_mem, Pointer.to(bufB));
        clSetKernelArg(kernel, 2, Sizeof.cl_mem, Pointer.to(bufC));
        clSetKernelArg(kernel, 3, Sizeof.cl_int, Pointer.to(new int[]{N}));

        long globalWorkSize[] = new long[]{N};

        long start = System.nanoTime();
        clEnqueueNDRangeKernel(queue, kernel, 1, null, globalWorkSize, null,
            0, null, null);
        clFinish(queue);
        long end = System.nanoTime();

        double took = (end - start) / 1e6;
        System.out.println("Took " + took + " millis");

        float[] C = new float[E];
        clEnqueueReadBuffer(queue, bufC, CL_TRUE, 0,
            Sizeof.cl_float * C.length, Pointer.to(C),
            0, null, null);

        System.out.println("Computed on GPU: " + Arrays.toString(C));

        clReleaseMemObject(bufA);
        clReleaseMemObject(bufB);
        clReleaseMemObject(bufC);
        clReleaseKernel(kernel);
        clReleaseProgram(program);
        clReleaseCommandQueue(queue);
        clReleaseContext(context);
    }
}
