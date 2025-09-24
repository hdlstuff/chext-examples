package chext_examples

import chisel3._

import chext.{elastic => e}
import e.ConnectOp._

class Example0 extends Module with chext.TestBenchTop {
  val io = IO(new Bundle {
    val source = e.Source(UInt(32.W))
    val sink0 = e.Sink(UInt(32.W))
    val sink1 = e.Sink(UInt(32.W))
  })
  
  val fork0 = new e.Fork(e.SourceBuffer(io.source)) {
    fork(in + 9.U) :=> e.SinkBuffer(io.sink0)
    fork(in + 87.U) :=> e.SinkBuffer(io.sink1)
  }

  declareClock(clock)
  declareReset(reset)
  declareElasticInterface(io.source, "Source")
  declareElasticInterface(io.sink0, "Sink0")
  declareElasticInterface(io.sink1, "Sink1")
}

object Example0_Tb extends chext.TestBench {
  emit(new Example0)
}

object Example0_Emit extends App {
  emitVerilog(new Example0)
}
