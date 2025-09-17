import Foundation
import Metal

@_cdecl("metal_create_command_buffer")
public func metal_create_command_buffer(queuePtr: UnsafeMutableRawPointer) -> UnsafeMutableRawPointer? {
    let queue: MTLCommandQueue = pointerToObject(queuePtr)
    guard let cmdBuf = queue.makeCommandBuffer() else { return nil }
    return objectToPointer(cmdBuf)
}

@_cdecl("metal_make_encoder")
public func metal_make_encoder(
    cmdBufPtr: UnsafeMutableRawPointer,
    pipelinePtr: UnsafeMutableRawPointer
) -> UnsafeMutableRawPointer? {
    let cmdBuf: MTLCommandBuffer = pointerToObject(cmdBufPtr)
    let encoder = cmdBuf.makeComputeCommandEncoder()!
    let pipeline: MTLComputePipelineState = pointerToObject(pipelinePtr)
    encoder.setComputePipelineState(pipeline)
    return objectToPointer(encoder)
}

@_cdecl("metal_encoder_set_buffer")
public func metal_encoder_set_buffer(
    encPtr: UnsafeMutableRawPointer,
    bufPtr: UnsafeMutableRawPointer,
    index: Int
) {
    let encoder: MTLComputeCommandEncoder = pointerToObject(encPtr)
    let buffer: MTLBuffer = pointerToObject(bufPtr)
    encoder.setBuffer(buffer, offset: 0, index: index)
}

@_cdecl("metal_dispatch")
public func metal_dispatch(
    encPtr: UnsafeMutableRawPointer,
    cmdBufPtr: UnsafeMutableRawPointer,
    threadCount: Int
) {
    let encoder: MTLComputeCommandEncoder = pointerToObject(encPtr)
    let cmdBuf: MTLCommandBuffer = pointerToObject(cmdBufPtr)

    let gridSize = MTLSize(width: threadCount, height: 1, depth: 1)
    let threadgroupSize = MTLSize(width: 1, height: 1, depth: 1)

    encoder.dispatchThreads(gridSize, threadsPerThreadgroup: threadgroupSize)
    encoder.endEncoding()
    cmdBuf.commit()
    cmdBuf.waitUntilCompleted()
}
