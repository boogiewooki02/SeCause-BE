package SeCause.SeCause_be.domain.analysis.service;

import SeCause.SeCause_be.domain.analysis.dto.AnalysisCallbackFailureRequest;
import SeCause.SeCause_be.domain.analysis.dto.AnalysisCallbackSuccessRequest;
import SeCause.SeCause_be.domain.analysis.entity.Analysis;
import SeCause.SeCause_be.domain.analysis.entity.AnalysisStatus;
import SeCause.SeCause_be.domain.analysis.exception.AnalysisException;
import SeCause.SeCause_be.domain.analysis.exception.code.AnalysisErrorCode;
import SeCause.SeCause_be.domain.analysis.repository.AnalysisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AnalysisCallbackService {

    private final AnalysisRepository analysisRepository;
    private final AnalysisFindingPersistenceService analysisFindingPersistenceService;

    @Transactional
    public void handleSuccess(AnalysisCallbackSuccessRequest request) {
        if (request.status() != AnalysisStatus.COMPLETED) {
            throw new AnalysisException(AnalysisErrorCode.ANALYSIS_CALLBACK_INVALID_STATUS);
        }

        Analysis analysis = getAnalysisForUpdate(request.analysisId());
        validateRepositoryId(analysis, request.repositoryId());
        if (isTerminal(analysis.getAnalysisStatus())) {
            return;
        }

        analysisFindingPersistenceService.saveAll(analysis, request.findings());

        analysis.complete();
    }

    @Transactional
    public void handleFailure(AnalysisCallbackFailureRequest request) {
        if (request.status() != AnalysisStatus.FAILED) {
            throw new AnalysisException(AnalysisErrorCode.ANALYSIS_CALLBACK_INVALID_STATUS);
        }

        Analysis analysis = getAnalysisForUpdate(request.analysisId());
        validateRepositoryId(analysis, request.repositoryId());
        if (isTerminal(analysis.getAnalysisStatus())) {
            return;
        }

        analysis.fail(createFailureReason(request));
    }

    // 콜백 대상 분석 조회
    private Analysis getAnalysisForUpdate(Long analysisId) {
        return analysisRepository.findForUpdateWithRepositoryByAnalysisId(analysisId)
                .orElseThrow(() -> new AnalysisException(AnalysisErrorCode.ANALYSIS_RESULT_NOT_FOUND));
    }

    // 콜백 payload의 repositoryId 검증
    private void validateRepositoryId(Analysis analysis, Long repositoryId) {
        if (!Objects.equals(analysis.getRepository().getRepositoryId(), repositoryId)) {
            throw new AnalysisException(AnalysisErrorCode.ANALYSIS_CALLBACK_INVALID_PAYLOAD);
        }
    }

    // 콜백 재시도 멱등 처리 기준
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
