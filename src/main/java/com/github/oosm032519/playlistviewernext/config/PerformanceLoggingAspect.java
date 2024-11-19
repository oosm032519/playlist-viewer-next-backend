package com.github.oosm032519.playlistviewernext.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * サービスレイヤーのメソッド実行時間を記録するAspectクラスである。
 * Spring AOPを使用して、サービスクラスのメソッド実行時間をログに出力する。
 *
 * @since 1.0
 */
@Aspect
@Component
public class PerformanceLoggingAspect {

    /**
     * このクラスのロギング用のLoggerインスタンス
     */
    private static final Logger logger = LoggerFactory.getLogger(PerformanceLoggingAspect.class);

    /**
     * サービスレイヤーのメソッド実行時間を計測し、ログに出力する。
     * com.github.oosm032519.playlistviewernext.serviceパッケージ配下の
     * すべてのメソッドの実行をインターセプトする。
     *
     * @param joinPoint 実行中のメソッドに関する情報を含むJoinPoint
     * @return メソッドの実行結果
     * @throws Throwable メソッド実行時に発生した例外
     */
    @Around("execution(* com.github.oosm032519.playlistviewernext.service..*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        // メソッド実行開始時刻を記録
        long start = System.currentTimeMillis();

        // 対象メソッドを実行
        Object proceed = joinPoint.proceed();

        // 実行時間を計算
        long executionTime = System.currentTimeMillis() - start;

        // メソッド名と実行時間をログに出力
        logger.info("{} executed in {} ms", joinPoint.getSignature(), executionTime);

        return proceed;
    }
}
