# FastDaemon Examples & Benchmarks

This directory contains executable showcases and JMH microbenchmarks for **FastDaemon**.

## Projects

- **[Demo/](Demo)**: Interactive demonstration of starting a local daemon, probing heartbeat with `ping()`, dispatching raw IPC commands, running an in-process latency benchmark, and performing graceful shutdown.
- **[Benchmark/](Benchmark)**: Formal OpenJDK JMH microbenchmark suite measuring round-trip latency and loopback socket dispatch throughput.

## Launchers

- `run-demo.bat` — Compiles and executes the interactive demo showcase.
- `run-benchmark.bat` — Packages the JMH benchmark suite and runs it in the terminal.
