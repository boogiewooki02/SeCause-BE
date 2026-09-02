package SeCause.SeCause_be.domain.analysis.dto;

import SeCause.SeCause_be.domain.analysis.entity.AnalysisStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AnalysisCallbackSuccessRequest(
        @NotNull(message = "분석 ID는 필수입니다.")
        Long analysisId,

        @NotNull(message = "레포지토리 ID는 필수입니다.")
        Long repositoryId,

        @NotNull(message = "분석 상태는 필수입니다.")
        AnalysisStatus status,

        @Valid
        List<AnalysisCallbackFinding> findings,

        @Valid
        AnalysisCallbackSummary summary
) {
}
