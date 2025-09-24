#include <Example0.hpp>

#include <chext_test/amba/axi4/full/ReadWriteTester.hpp>
#include <chext_test/amba/axi4/full/Transaction.hpp>
#include <chext_test/chext_test.hpp>
#include <chext_test/util/Spawn.hpp>

#include <chext_test/amba/axi4/lite/hal.hpp>
#include <chext_test/util/hal.hpp>
#include <hdlstuff_hal/hal.hpp>

#include <jqr/core.hpp>
#include <jqr/dump.hpp>

#include <systemc>
#include <verilated_vcd_sc.h>

#include <algorithm>
#include <fstream>
#include <map>
#include <unordered_map>
#include <unordered_set>
#include <vector>

#include <boost/endian.hpp>
#include <chext_test/amba/axi4/full/Transaction.hpp>

using namespace chext_test;
using namespace sc_core;
using namespace sc_dt;

struct Testbench : public chext_test::TestBenchBase {
    Testbench(sc_module_name const& name = "testbench")
        : TestBenchBase { name }
        , SC_NAMED(dut)
        , SC_NAMED(clock, sc_time(2, SC_NS))
        , SC_NAMED(reset)
    // , memoryHandler { std::make_shared<amba::axi4::full::BasicMemoryHandler>(8ull << 30, 0x0) }
    // , SC_NAMED(
    //       memory,
    //       memoryHandler,
    //       16, 16,
    //       sc_time(120, SC_NS), sc_time(120, SC_NS),
    //       true // log memory accesses
    //   )
    {

        dut.clock(clock);
        dut.reset(reset);
        // memory.spawn(dut.M_AXI);
    }

    Example0 dut;

private:
    sc_clock clock;
    sc_signal<bool> reset;

    // std::shared_ptr<amba::axi4::full::BasicMemoryHandler> memoryHandler;
    // amba::axi4::full::Memory memory;

    void entry() override {
        resetDut();

        // dut.io_source.send(sc_bv<32>(98));
        // fmt::println("sink0: {}", dut.io_sink0.receive().to_int64());
        // fmt::println("sink1: {}", dut.io_sink1.receive().to_int64());

        sc_join j;

        SC_SPAWN_TO(j) {
            for (int i = 0; i < 1024; ++i)
                dut.io_source.send(sc_bv<32>(98));
        };

        SC_SPAWN_TO(j) {
            for (int i = 0; i < 1024; ++i) {
                auto x = dut.io_sink0.receive().to_int64();
                fmt::println("sink0: {} @{}", x, sc_time_stamp().to_string());
            }
        };

        SC_SPAWN_TO(j) {
            for (int i = 0; i < 1024; ++i) {
                auto x = dut.io_sink1.receive().to_int64();
                fmt::println("sink1: {} @{}", x, sc_time_stamp().to_string());
            }
        };

        j.wait();

        finish();
    }

    void resetDut() {
        wait(clock.negedge_event());
        reset.write(true);

        wait(clock.negedge_event());
        wait(clock.negedge_event());

        reset.write(false);

        wait(clock.negedge_event());
    }
};

int sc_main(int argc, char** argv) {
    Verilated::commandArgs(argc, argv);
    Verilated::traceEverOn(true);

    Testbench tb;
    sc_start(SC_ZERO_TIME);

    std::unique_ptr<VerilatedVcdSc> trace_file = std::make_unique<VerilatedVcdSc>();
    tb.dut.traceVerilated(trace_file.get(), 99);
    trace_file->open("MyTestBench.vcd");

    sc_start(sc_time(1, SC_US));
    trace_file->close();

    return 0;
}
