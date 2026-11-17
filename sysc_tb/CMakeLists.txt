cmake_minimum_required(VERSION 3.28)
project(sysc_tb)

add_compile_definitions(SC_DISABLE_API_VERSION_CHECK)
add_compile_definitions(SC_ALLOW_DEPRECATED_IEEE_API)

cmake_policy(SET CMP0167 OLD)

find_package(Boost 1.80 REQUIRED COMPONENTS endian)
find_package(SystemCLanguage REQUIRED)
find_package(verilator REQUIRED)
find_package(fmt REQUIRED)

find_package(hdlscw REQUIRED)
find_package(chext_test REQUIRED)
find_package(hdlstuff_hal REQUIRED)

include(CMakeParseArguments)

function(generate_tb)
    set(options "")
    set(oneValueArgs TARGET_NAME)
    set(multiValueArgs CPP_SOURCES HDL_MODULES VERILATOR_ARGS)
    cmake_parse_arguments(GEN_TB "${options}" "${oneValueArgs}" "${multiValueArgs}" ${ARGN})

    set(GEN_TB_OUTPUT_NAME ${GEN_TB_TARGET_NAME})
    set(GEN_TB_TARGET_NAME ${GEN_TB_TARGET_NAME})

    add_executable(${GEN_TB_TARGET_NAME})
    set_target_properties(${GEN_TB_TARGET_NAME} PROPERTIES OUTPUT_NAME ${GEN_TB_OUTPUT_NAME})
    target_compile_definitions(${GEN_TB_TARGET_NAME} PUBLIC VERILATED_TRACE_ENABLED)

    foreach(SOURCE_FILE IN LISTS GEN_TB_CPP_SOURCES)
        target_sources(
            ${GEN_TB_TARGET_NAME}
            PUBLIC
            ${CMAKE_SOURCE_DIR}/src/${SOURCE_FILE}
        )
    endforeach()

    target_include_directories(${GEN_TB_TARGET_NAME} PUBLIC include/)
    target_include_directories(${GEN_TB_TARGET_NAME} PUBLIC ${CMAKE_SOURCE_DIR}/common/include)

    target_link_libraries(
        ${GEN_TB_TARGET_NAME}
        PUBLIC
        SystemC::systemc
        fmt::fmt
        Boost::boost
        Boost::endian
        hdlstuff::hdlscw
        hdlstuff::hal
        hdlstuff::chext_test
    )

    foreach(COMMON_SRC IN LISTS COMMON_SRCS)
        target_sources(
            ${GEN_TB_TARGET_NAME}
            PUBLIC
            ${CMAKE_SOURCE_DIR}/common/src/${COMMON_SRC}
        )
    endforeach()

    foreach(MODULE IN LISTS GEN_TB_HDL_MODULES)
        verilate(
            ${GEN_TB_TARGET_NAME}
            SYSTEMC
            SOURCES ${CMAKE_SOURCE_DIR}/hdl/${MODULE}.sv
            VERILATOR_ARGS -I${CMAKE_SOURCE_DIR}/common/hdl ${GEN_TB_VERILATOR_ARGS}
        )

        hdlscw(
            TARGET ${GEN_TB_TARGET_NAME}
            HDLINFO ${CMAKE_SOURCE_DIR}/hdl/${MODULE}.hdlinfo.json
            ARGS -r ${CMAKE_SOURCE_DIR}/protocols.py
        )
    endforeach()
endfunction()


# add source shared among all the targets here
set(COMMON_SRCS)

# please add your targets here
# generate_tb(
#     TARGET_NAME <target_name>
#     CPP_SOURCES <cpp_sources>  # relative to the ./src/
#     HDL_MODULES <hdl_modules>  # relative to the ./hdl/
# )

generate_tb(
    TARGET_NAME chext_examples.Example0
    CPP_SOURCES chext_examples/Example0.cpp
    HDL_MODULES chext_examples/Example0
    VERILATOR_ARGS --trace
)

generate_tb(
    TARGET_NAME chext_examples.Example1
    CPP_SOURCES chext_examples/Example1.cpp
    HDL_MODULES chext_examples/Example1
    VERILATOR_ARGS --trace
)

generate_tb(
    TARGET_NAME chext_examples.Example2
    CPP_SOURCES chext_examples/Example2.cpp
    HDL_MODULES chext_examples/Example2
    VERILATOR_ARGS --trace
)

generate_tb(
    TARGET_NAME chext_examples.Example3
    CPP_SOURCES chext_examples/Example3.cpp
    HDL_MODULES chext_examples/Example3
    VERILATOR_ARGS --trace
)
