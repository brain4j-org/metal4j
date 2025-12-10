# Metal4J

**This project is in early development stage!** Expect bugs and issues.

Metal4J provides **pure Java bindings** for Apple’s **Metal API**, built on top of the **Foreign Function & Memory (FFM) API** 
introduced in Java 22 and stabilized in Java 25. It enables GPU programming on macOS directly from Java.

## Features

* Implemented entirely in Java, no JNI dependencies
* Uses the stable Foreign Function & Memory API (Java 25). No JNI
* Wrappers for key Metal objects: `MTLDevice`, `MTLBuffer`, `MTLCommandQueue`, `MTLLibrary`, `MTLFunction`, `MTLPipeline`,
  `MTLCommandBuffer`, `MTLEncoder`
* Designed to be as close as possible to native Metal

## Requirements

* macOS 13 or newer (Metal supported natively)
* Java 22 or later
* Gradle or Maven build tool

## Roadmap

- [X] Device handling
- [X] Command queues
- [X] Buffers
- [ ] Multiple device integration
- [ ] Pre-compiled kernel support

## Documentation

* [Apple Metal API](https://developer.apple.com/metal/)
* [JEP 454: Foreign Function & Memory API](https://openjdk.org/jeps/454)

## Contributing

Contributions, issues, and pull requests are welcome.
Please open an issue to discuss major changes before submitting a PR.

## License

Metal4J is licensed under the Apache License, Version 2.0.