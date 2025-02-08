package com.github.oosm032519.playlistviewernext.aspect;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
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

    @Mock
    private Signature signature;

    /**
     * リクエストパラメータが存在する場合に、ログが正しく出力されることを確認する。
     */
    @Test
    void logRequestParams_WithValidParameters() {
        // Arrange: テストデータの準備
        Map<String, String[]> parameterMap = new HashMap<>();
        parameterMap.put("testParam", new String[]{"testValue"});

        ServletRequestAttributes attributes = mock(ServletRequestAttributes.class);
        when(attributes.getRequest()).thenReturn(request);
        when(request.getParameterMap()).thenReturn(parameterMap);
        when(joinPoint.getSignature()).thenReturn(signature);

        RequestContextHolder.setRequestAttributes(attributes);

        // Act: テスト対象メソッドの実行
        requestParamsAspect.logRequestParams(joinPoint);

        // Assert: メソッド呼び出しの検証
        verify(request, times(1)).getParameterMap();
        verify(joinPoint, times(1)).getSignature();

        // クリーンアップ
        RequestContextHolder.resetRequestAttributes();
    }

    /**
     * RequestAttributesがnullの場合に、エラーが発生せずに処理が終了することを確認する。
     */
    @Test
    void logRequestParams_WithNullRequestAttributes() {
        // Arrange: RequestContextHolderをクリア
        RequestContextHolder.resetRequestAttributes();

        // Act: テスト対象メソッドの実行
        requestParamsAspect.logRequestParams(joinPoint);

        // Assert: エラーが発生せずに正常に処理が完了することを確認
        verify(joinPoint, never()).getSignature();
    }

    /**
     * 空のパラメータマップが渡された場合に、ログ出力が行われないことを確認する。
     */
    @Test
    void logRequestParams_WithEmptyParameters() {
        // Arrange: テストデータの準備
        Map<String, String[]> emptyParameterMap = new HashMap<>();

        ServletRequestAttributes attributes = mock(ServletRequestAttributes.class);
        when(attributes.getRequest()).thenReturn(request);
        when(request.getParameterMap()).thenReturn(emptyParameterMap);

        RequestContextHolder.setRequestAttributes(attributes);

        // Act: テスト対象メソッドの実行
        requestParamsAspect.logRequestParams(joinPoint);

        // Assert: メソッド呼び出しの検証
        verify(request, times(1)).getParameterMap();
        verify(joinPoint, never()).getSignature();

        // クリーンアップ
        RequestContextHolder.resetRequestAttributes();
    }
}
