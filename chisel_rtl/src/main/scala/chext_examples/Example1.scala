package chext_examples

import chisel3._

import chext.{elastic => e}
import e.ConnectOp._

class Example1 extends Module with chext.TestBenchTop {
  val io = IO(new Bundle {
    val sourceSelect = e.Source(new Bundle {
      val sourceId = UInt(1.W)
      val count = UInt(8.W)
    })

    val sourceA = e.Source(UInt(32.W))
    val sourceB = e.Source(UInt(32.W))
    val sink = e.Sink(UInt(32.W))
  })

  val ewire0 = e.EWire(UInt(1.W))

  val repeat0 = new e.Repeat(io.sourceSelect, ewire0, 8) {
    len { in => in.count }
    out { (in, index, first, last) => in.sourceId }
  }

  e.Mux(Seq(io.sourceA, io.sourceB), io.sink, ewire0)

  declareClock(clock)
  declareReset(reset)
  declareElasticInterface(io.sourceA, "SourceData")
  declareElasticInterface(io.sourceB, "SourceData")
  declareElasticInterface(io.sourceSelect, "SourceSelect")
  declareElasticInterface(io.sink, "Sink")
}

object Example1_Tb extends chext.TestBench {
  emit(new Example1)
}

object Example1_Emit extends App {
  emitVerilog(new Example1)
}
