package com.github.oosm032519.playlistviewernext.aspect;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestParamsAspectTest {

    @InjectMocks
    private RequestParamsAspect requestParamsAspect;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private HttpServletRequest request;

    @Test
    @DisplayName("正常系：リクエストパラメータが存在する場合のログ出力テスト")
    void logRequestParams_WithValidParameters() {
        // テストデータのセットアップ
        Map<String, String[]> parameterMap = new HashMap<>();
        parameterMap.put("testParam", new String[]{"testValue"});

        // モックの設定
        ServletRequestAttributes attributes = mock(ServletRequestAttributes.class);
        when(attributes.getRequest()).thenReturn(request);
        when(request.getParameterMap()).thenReturn(parameterMap);

        // RequestContextHolderの設定
        RequestContextHolder.setRequestAttributes(attributes);

        try {
            // テスト実行
            requestParamsAspect.logRequestParams(joinPoint);

            // 検証
            verify(request, times(1)).getParameterMap();
            verify(joinPoint, times(1)).getSignature();
        } finally {
            // クリーンアップ
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    @DisplayName("異常系：RequestAttributesがnullの場合のテスト")
    void logRequestParams_WithNullRequestAttributes() {
        // RequestContextHolderをクリア
        RequestContextHolder.resetRequestAttributes();

        // テスト実行
        requestParamsAspect.logRequestParams(joinPoint);

        // 検証：エラーが発生せずに正常に処理が完了すること
        verify(joinPoint, never()).getSignature();
    }

    @Test
    @DisplayName("正常系：空のパラメータマップの場合のテスト")
    void logRequestParams_WithEmptyParameters() {
        // テストデータのセットアップ
        Map<String, String[]> emptyParameterMap = new HashMap<>();

        // モックの設定
        ServletRequestAttributes attributes = mock(ServletRequestAttributes.class);
        when(attributes.getRequest()).thenReturn(request);
        when(request.getParameterMap()).thenReturn(emptyParameterMap);

        RequestContextHolder.setRequestAttributes(attributes);

        try {
            // テスト実行
            requestParamsAspect.logRequestParams(joinPoint);

            // 検証
            verify(request, times(1)).getParameterMap();
            verify(joinPoint, never()).getSignature();
        } finally {
            // クリーンアップ
            RequestContextHolder.resetRequestAttributes();
        }
    }
}
