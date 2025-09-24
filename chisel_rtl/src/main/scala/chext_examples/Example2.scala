package chext_examples

import chisel3._

import chext.{elastic => e}
import e.ConnectOp._

class Example2_In(wData: Int = 8) extends Bundle {
  val data = UInt(wData.W)
  val last = Bool()
}

class Example2_Out extends Bundle {
  val data = UInt(8.W)
  val length = UInt(32.W)
  val last = Bool()
}

class Example2 extends Module with chext.TestBenchTop {
  val genIn = new Example2_In(8)
  val genOut = new Example2_Out

  val source = IO(e.Source(genIn))
  val sink = IO(e.Sink(genOut))

  val transducer0 = new e.Transducer(source, sink) {
    val rLastDataValid = RegInit(false.B)
    val rLastDataLength = RegInit(0.U(32.W))
    val rLastData = RegInit(0.U(8.W))

    packet {
      out.data := rLastData
      out.length := rLastDataLength
      out.last := in.last
      
      when(rLastDataValid) {
        when(in.data === rLastData && !in.last) {
          consume { rLastDataLength := rLastDataLength + 1.U }
        }.otherwise {
          accept {
            rLastData := in.data
            rLastDataLength := 0.U
            rLastDataValid := !in.last
          }
        }
      }.otherwise {
        when(in.last) {
          accept {}
        }.otherwise {
          consume {
            rLastData := in.data
            rLastDataValid := true.B
          }
        }
      }
    }
  }

  declareClock(clock)
  declareReset(reset)
  declareElasticInterface(source, "Source")
  declareElasticInterface(sink, "Sink")
}

object Example2_Tb extends chext.TestBench {
  emit(new Example2)
}

object Example2_Emit extends App {
  emitVerilog(new Example2)
}
