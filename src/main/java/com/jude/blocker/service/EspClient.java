package com.jude.blocker.service;

/** Spring 서버 -> ESP32 로 게이트 제어 신호를 전송하는 클라이언트 */
public interface EspClient {
    /**
     * 게이트 열기/닫기 신호 전송
     * @param open true=열기(ON), false=닫기(OFF)
     * @return 전송 성공 여부 (ESP가 2xx 응답하면 true)
     */
    boolean sendOpen(boolean open);
}