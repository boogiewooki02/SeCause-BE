package SeCause.SeCause_be.domain.analysis.service;

import SeCause.SeCause_be.domain.analysis.dto.AnalysisCallbackFailureRequest;
import SeCause.SeCause_be.domain.analysis.entity.Analysis;
import SeCause.SeCause_be.domain.analysis.entity.AnalysisStatus;
import SeCause.SeCause_be.domain.analysis.exception.AnalysisException;
import SeCause.SeCause_be.domain.analysis.exception.code.AnalysisErrorCode;
import SeCause.SeCause_be.domain.analysis.repository.AnalysisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AnalysisCallbackService {

    private final AnalysisRepository analysisRepository;

    @Transactional
    public void handleFailure(AnalysisCallbackFailureRequest request) {
        if (request.status() != AnalysisStatus.FAILED) {
            throw new AnalysisException(AnalysisErrorCode.ANALYSIS_CALLBACK_INVALID_STATUS);
        }

        Analysis analysis = getAnalysis(request.analysisId());
        if (isTerminal(analysis.getAnalysisStatus())) {
            return;
        }

        analysis.fail(createFailureReason(request));
    }

    private Analysis getAnalysis(Long analysisId) {
        return analysisRepository.findById(analysisId)
                .orElseThrow(() -> new AnalysisException(AnalysisErrorCode.ANALYSIS_RESULT_NOT_FOUND));
    }

    private boolean isTerminal(AnalysisStatus status) {
        return status == AnalysisStatus.COMPLETED
                || status == AnalysisStatus.FAILED
                || status == AnalysisStatus.CANCELLED;
    }

    private String createFailureReason(AnalysisCallbackFailureRequest request) {
        StringBuilder reason = new StringBuilder();

        append(reason, request.failedStage());
        append(reason, request.errorCode());
        append(reason, request.errorMessage());

        if (reason.isEmpty()) {
            return "분석 처리 중 오류가 발생했습니다.";
        }

        return reason.toString();
    }

    private void append(StringBuilder builder, String value) {
        if (!StringUtils.hasText(value)) {
            return;
        }

        if (!builder.isEmpty()) {
            builder.append(" - ");
        }
        builder.append(value.trim());
    }
}
