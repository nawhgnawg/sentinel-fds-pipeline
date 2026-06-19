package com.sentinel.fds.model;

public enum TransactionType {
    TRANSFER,       // 계좌 이체
    LOGIN,          // 로그인
    DEVICE_CHANGE,   // 기기 변경
    LOGIN_ATTEMPT   // 해커 공격

}