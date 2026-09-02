package SeCause.SeCause_be.domain.analysis.controller;

import SeCause.SeCause_be.domain.analysis.dto.AnalysisCallbackFailureRequest;
import SeCause.SeCause_be.domain.analysis.dto.AnalysisCallbackSuccessRequest;
import SeCause.SeCause_be.domain.analysis.service.AnalysisCallbackService;
import SeCause.SeCause_be.global.apiPayload.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/analysis/callback")
public class AnalysisCallbackController {

    private final AnalysisCallbackService analysisCallbackService;

    @PostMapping("/success")
    public ApiResponse<Void> handleSuccess(@RequestBody @Valid AnalysisCallbackSuccessRequest request) {
        analysisCallbackService.handleSuccess(request);
        return ApiResponse.onSuccess("분석 성공 콜백 처리가 완료됐습니다.");
    }

    @PostMapping("/failure")
    public ApiResponse<Void> handleFailure(@RequestBody @Valid AnalysisCallbackFailureRequest request) {
        analysisCallbackService.handleFailure(request);
        return ApiResponse.onSuccess("분석 실패 콜백 처리가 완료됐습니다.");
    }
}
