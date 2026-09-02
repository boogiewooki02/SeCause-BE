package SeCause.SeCause_be.domain.analysis.dto;

import SeCause.SeCause_be.domain.analysis.entity.AnalysisStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
        List<Finding> findings,

        @Valid
        Summary summary
) {

    public record Finding(
            String tool,

            @NotBlank(message = "취약점 타입은 필수입니다.")
            String type,

            String cweId,

            @NotBlank(message = "심각도는 필수입니다.")
            String severity,

            @NotBlank(message = "파일 경로는 필수입니다.")
            String filePath,

            Integer lineStart,
            Integer lineEnd,
            String message,
            String evidence,
            String summary,
            String rootCause,
            String impact,
            String recommendation,

            @Valid
            List<FixExample> fixExamples,

            List<String> references,

            @Valid
            List<ReferenceDocument> referenceDocuments
    ) {
    }

    public record FixExample(
            String language,
            String vulnerableCode,
            String fixedCode,
            String explanation
    ) {
    }

    public record ReferenceDocument(
            String title,
            String url
    ) {
    }

    public record Summary(
            Integer totalCount
    ) {
    }
}
