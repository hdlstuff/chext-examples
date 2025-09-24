package chext_examples

import chisel3._

import chext.{elastic => e}
import e.ConnectOp._

import chext.amba.axi4
import axi4.Ops._

import chext.{ldstr => ldst}
import chext.stream

class Example3_Task extends Bundle {
  val ptrArray = UInt(64.W) // uint32_t[uLength]
  val uLength = UInt(64.W)

  val ptrResult = UInt(64.W)
}

class Example3_Result extends Bundle {}

case class Example3_Config() {
  val genTask = new Example3_Task
  val genResult = new Example3_Result

  val axiCfg = axi4.Config(wId = 6, wAddr = 64, wData = 64)
  val axiRdCfg = axi4.Config(wAddr = 64, wData = 32)
  val axiStCfg = axi4.Config(wAddr = 64, wData = 32)
}

class Example3_Basic(cfg: Example3_Config) extends Module {
  import cfg._

  val sourceTask = IO(e.Source(genTask))
  val sinkResult = IO(e.Sink(genResult))

  val m_axi_rd = IO(axi4.full.Master(axiRdCfg))
  val m_axi_st = IO(axi4.full.Master(axiStCfg))

  {
    val rd = Module(
      new stream.Read(
        stream.ReadConfig(
          axiRdCfg,
          resultMode = stream.ReadResultMode.LastSometimesInvalid
        )
      )
    )
    rd.m_axi :=> m_axi_rd

    val st = Module(new ldst.Store(ldst.StoreConfig(axiStCfg)))
    st.m_axi :=> m_axi_st

    val fork0 = new e.Fork(sourceTask) {
      val transform0 = new e.Transform(e.SourceBuffer(fork(), 8), rd.sourceTask) {
        out.address := in.ptrArray
        out.length := in.uLength
        out.user := 0.U
      }

      val fold0 =
        new e.Fold(rd.sinkResult, e.Const(0.U, "zero"), st.sourceData) {
          operand { in => in.data }
          last { in => in.last }
          zero { in => !in.valid }

          val join0 = new e.Join(e.SinkBuffer(sourceResult)) {
            out := join(sinkA) + join(sinkB)
          }
        }

      val transform1 = new e.Transform(e.SourceBuffer(fork(), 8), st.sourceTask) {
        out.address := in.ptrResult
        out.user := 0.U
      }
    }

    val transform0 = new e.Transform(st.sinkResult, sinkResult) {}
  }
}

import chext.naming.prefix

class Example3(cfg: Example3_Config) extends Module with chext.TestBenchTop {
  import cfg._

  val M_AXI = IO(axi4.Master(axiCfg))

  val sourceTask = IO(e.Source(genTask))
  val sinkResult = IO(e.Sink(genResult))

  {
    val dut = Module(new Example3_Basic(cfg))

    import axi4.full.{components => comp}

    val mux = Module(new comp.Mux(comp.MuxConfig(axiCfg.copy(wId = 0), 2)))

    prefix("rd") {
      val upscale = Module(new comp.Upscale(comp.UpscaleConfig(axiRdCfg, 64)))
      dut.m_axi_rd :=> upscale.s_axi
      upscale.m_axi :=> mux.s_axi(0)
    }

    prefix("st") {
      val upscale = Module(new comp.Upscale(comp.UpscaleConfig(axiStCfg, 64)))
      dut.m_axi_st :=> upscale.s_axi
      upscale.m_axi :=> mux.s_axi(1)
    }

    mux.m_axi :=> M_AXI.asFull

    sourceTask :=> dut.sourceTask
    dut.sinkResult :=> sinkResult
  }

  declareClock(clock)
  declareReset(reset)
  declareAxi4Interface(M_AXI)
  declareElasticInterface(sourceTask, "Task")
  declareElasticInterface(sinkResult, "Result")
}

object Example3_Tb extends chext.TestBench {
  emit(new Example3(Example3_Config()))
}

object Example3_Emit extends App {
  emitVerilog(new Example3(Example3_Config()))
}
