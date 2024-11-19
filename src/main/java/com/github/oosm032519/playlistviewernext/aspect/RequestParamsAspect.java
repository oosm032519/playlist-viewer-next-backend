package com.github.oosm032519.playlistviewernext.aspect;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;

/**
 * リクエストパラメータをログに記録するためのAspectクラス。
 * Spring Aspectによって、コントローラメソッドが呼び出される前にリクエストパラメータを取得し、ログに出力する。
 */
@Aspect
@Component
public class RequestParamsAspect {

    /**
     * ログを出力するためのLoggerインスタンス。
     * このクラスで発生したイベントやエラーを記録する。
     */
    private static final Logger logger = LoggerFactory.getLogger(RequestParamsAspect.class);

    /**
     * コントローラのメソッドが実行される前に、HTTPリクエストのパラメータをログに記録する。
     *
     * @param joinPoint アスペクトがインターセプトしたメソッド呼び出しの情報を提供するオブジェクト。
     *                  これには、メソッド名、引数、ターゲットオブジェクトなどが含まれる。
     */
    @Before("execution(* com.github.oosm032519.playlistviewernext.controller..*(..))")
    public void logRequestParams(JoinPoint joinPoint) {
        try {
            // 現在のリクエスト属性を取得する
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            // attributesがnullでない場合に続行する
            if (attributes != null) {
                // HttpServletRequestを取得
                HttpServletRequest request = attributes.getRequest();

                // リクエストのパラメータマップを取得（キーはパラメータ名、値はその値の配列）
                Map<String, String[]> parameterMap = request.getParameterMap();

                // パラメータが存在する場合のみ処理を行う
                if (!parameterMap.isEmpty()) {
                    StringBuilder params = new StringBuilder();

                    // 各パラメータをキー=値の形式で文字列に追加
                    parameterMap.forEach((key, values) ->
                            params.append(key).append("=").append(String.join(",", values)).append("&")
                    );

                    // 最後の "&" を削除
                    if (!params.isEmpty()) {
                        params.deleteCharAt(params.length() - 1);
                    }

                    // メソッドシグネチャとリクエストパラメータをデバッグログに出力
                    logger.debug("{} のリクエストパラメータ: {}", joinPoint.getSignature(), params);
                }
            }
        } catch (Exception e) {
            // パラメータ取得中に発生したエラーを記録
            logger.error("リクエストパラメータの取得中にエラーが発生しました。", e);
        }
    }
}
