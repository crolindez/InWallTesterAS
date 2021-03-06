package es.carlosrolindez.btcomm;

import java.util.UUID;


public interface BtConstants {

    UUID SPP_UUID =         UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    UUID HSP_UUID =         UUID.fromString("00001108-0000-1000-8000-00805F9B34FB");
    UUID A2DP_SOURCE_UUID = UUID.fromString("0000110A-0000-1000-8000-00805F9B34FB");
    UUID A2DP_SINK_UUID =   UUID.fromString("0000110B-0000-1000-8000-00805F9B34FB");
    UUID HDP =              UUID.fromString("00001400-0000-1000-8000-00805F9B34FB");
    UUID HDP_SOURCE =       UUID.fromString("00001401-0000-1000-8000-00805F9B34FB");
    UUID HDP_SINK =         UUID.fromString("00001402-0000-1000-8000-00805F9B34FB");



}
