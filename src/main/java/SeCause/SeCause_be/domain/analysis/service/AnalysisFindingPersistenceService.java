package SeCause.SeCause_be.domain.analysis.service;

import SeCause.SeCause_be.domain.analysis.dto.AnalysisCallbackFinding;
import SeCause.SeCause_be.domain.analysis.dto.AnalysisCallbackFixExample;
import SeCause.SeCause_be.domain.analysis.dto.AnalysisCallbackReferenceDocument;
import SeCause.SeCause_be.domain.analysis.entity.Analysis;
import SeCause.SeCause_be.domain.analysis.entity.AnalysisResult;
import SeCause.SeCause_be.domain.analysis.exception.AnalysisException;
import SeCause.SeCause_be.domain.analysis.exception.code.AnalysisErrorCode;
import SeCause.SeCause_be.domain.analysis.repository.AnalysisResultRepository;
import SeCause.SeCause_be.domain.projectRepository.entity.FileType;
import SeCause.SeCause_be.domain.projectRepository.entity.RepositoryFile;
import SeCause.SeCause_be.domain.projectRepository.repository.RepositoryFileRepository;
import SeCause.SeCause_be.domain.security.entity.ReferenceType;
import SeCause.SeCause_be.domain.security.entity.SecurityReference;
import SeCause.SeCause_be.domain.security.repository.SecurityReferenceRepository;
import SeCause.SeCause_be.domain.vulnerability.entity.CodeVulnerability;
import SeCause.SeCause_be.domain.vulnerability.entity.InfraVulnerability;
import SeCause.SeCause_be.domain.vulnerability.entity.Severity;
import SeCause.SeCause_be.domain.vulnerability.repository.CodeVulnerabilityRepository;
import SeCause.SeCause_be.domain.vulnerability.repository.InfraVulnerabilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AnalysisFindingPersistenceService {

    private final RepositoryFileRepository repositoryFileRepository;
    private final CodeVulnerabilityRepository codeVulnerabilityRepository;
    private final InfraVulnerabilityRepository infraVulnerabilityRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final SecurityReferenceRepository securityReferenceRepository;

    public void saveAll(Analysis analysis, List<AnalysisCallbackFinding> findings) {
        safeList(findings).stream()
                .filter(Objects::nonNull)
                .forEach(finding -> saveFinding(analysis, finding));
    }

    // FastAPI finding을 파일, 취약점, 상세 결과, 참고 문서로 저장
    private void saveFinding(Analysis analysis, AnalysisCallbackFinding finding) {
        RepositoryFile repositoryFile = getOrCreateRepositoryFile(analysis, finding);

        if (isInfraTool(finding.tool())) {
            InfraVulnerability vulnerability = infraVulnerabilityRepository.save(InfraVulnerability.create(
                    analysis,
                    repositoryFile,
                    limit(finding.type(), 100),
                    resolveSeverity(finding.severity()),
                    finding.evidence()
            ));
            saveAnalysisResult(vulnerability, finding);
            saveSecurityReferences(vulnerability, finding.referenceDocuments());
            return;
        }

        CodeVulnerability vulnerability = codeVulnerabilityRepository.save(CodeVulnerability.create(
                analysis,
                repositoryFile,
                limit(finding.type(), 100),
                resolveSeverity(finding.severity()),
                finding.lineStart(),
                finding.lineEnd(),
                finding.evidence()
        ));
        saveAnalysisResult(vulnerability, finding);
        saveSecurityReferences(vulnerability, finding.referenceDocuments());
    }

    // repositoryId와 filePath 기준으로 파일 row 조회 또는 생성
    private RepositoryFile getOrCreateRepositoryFile(Analysis analysis, AnalysisCallbackFinding finding) {
        String filePath = limit(finding.filePath(), 1000);

        return repositoryFileRepository.findByRepositoryRepositoryIdAndFilePath(
                        analysis.getRepository().getRepositoryId(),
                        filePath
                )
                .orElseGet(() -> repositoryFileRepository.save(RepositoryFile.create(
                        analysis.getRepository(),
                        filePath,
                        resolveFileType(finding.tool()),
                        resolveLanguage(finding.fixExamples()),
                        0L
                )));
    }

    // 코드 취약점 상세 결과 저장
    private void saveAnalysisResult(
            CodeVulnerability vulnerability,
            AnalysisCallbackFinding finding
    ) {
        analysisResultRepository.save(AnalysisResult.createForCodeVulnerability(
                vulnerability,
                defaultText(finding.rootCause(), finding.message(), finding.type()),
                defaultText(finding.summary(), finding.message(), finding.type()),
                finding.impact(),
                resolveFixCode(finding.fixExamples()),
                finding.recommendation()
        ));
    }

    // 인프라 취약점 상세 결과 저장
    private void saveAnalysisResult(
            InfraVulnerability vulnerability,
            AnalysisCallbackFinding finding
    ) {
        analysisResultRepository.save(AnalysisResult.createForInfraVulnerability(
                vulnerability,
                defaultText(finding.rootCause(), finding.message(), finding.type()),
                defaultText(finding.summary(), finding.message(), finding.type()),
                finding.impact(),
                resolveFixCode(finding.fixExamples()),
                finding.recommendation()
        ));
    }

    // 코드 취약점 참고 문서 저장
    private void saveSecurityReferences(
            CodeVulnerability vulnerability,
            List<AnalysisCallbackReferenceDocument> referenceDocuments
    ) {
        safeList(referenceDocuments).stream()
                .filter(Objects::nonNull)
                .filter(reference -> StringUtils.hasText(reference.url()) || StringUtils.hasText(reference.title()))
                .map(reference -> SecurityReference.createForCodeVulnerability(
                        vulnerability,
                        resolveReferenceType(reference),
                        limit(reference.title(), 500),
                        limit(reference.url(), 1000)
                ))
                .forEach(securityReferenceRepository::save);
    }

    // 인프라 취약점 참고 문서 저장
    private void saveSecurityReferences(
            InfraVulnerability vulnerability,
            List<AnalysisCallbackReferenceDocument> referenceDocuments
    ) {
        safeList(referenceDocuments).stream()
                .filter(Objects::nonNull)
                .filter(reference -> StringUtils.hasText(reference.url()) || StringUtils.hasText(reference.title()))
                .map(reference -> SecurityReference.createForInfraVulnerability(
                        vulnerability,
                        resolveReferenceType(reference),
                        limit(reference.title(), 500),
                        limit(reference.url(), 1000)
                ))
                .forEach(securityReferenceRepository::save);
    }

    private FileType resolveFileType(String tool) {
        if (isInfraTool(tool)) {
            return FileType.INFRA;
        }

        return FileType.SOURCE;
    }

    private boolean isInfraTool(String tool) {
        return "INFRA".equalsIgnoreCase(tool);
    }

    // FastAPI severity 문자열 변환, INFO만 LOW로 허용
    private Severity resolveSeverity(String severity) {
        if (!StringUtils.hasText(severity)) {
            throw new AnalysisException(AnalysisErrorCode.ANALYSIS_CALLBACK_INVALID_PAYLOAD);
        }

        if ("INFO".equalsIgnoreCase(severity)) {
            return Severity.LOW;
        }

        try {
            return Severity.valueOf(severity.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new AnalysisException(AnalysisErrorCode.ANALYSIS_CALLBACK_INVALID_PAYLOAD);
        }
    }

    // 참고 문서 title/url 기반 ReferenceType 추론
    private ReferenceType resolveReferenceType(AnalysisCallbackReferenceDocument reference) {
        String value = ((reference.title() == null ? "" : reference.title()) + " "
                + (reference.url() == null ? "" : reference.url())).toLowerCase(Locale.ROOT);

        if (value.contains("cwe") || value.contains("mitre.org")) {
            return ReferenceType.CWE;
        }
        if (value.contains("owasp")) {
            return ReferenceType.OWASP;
        }
        return ReferenceType.OTHER;
    }

    // 첫 번째 fixedCode를 저장용 수정 코드로 선택
    private String resolveFixCode(List<AnalysisCallbackFixExample> fixExamples) {
        return safeList(fixExamples).stream()
                .filter(Objects::nonNull)
                .map(AnalysisCallbackFixExample::fixedCode)
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse(null);
    }

    private String resolveLanguage(List<AnalysisCallbackFixExample> fixExamples) {
        return safeList(fixExamples).stream()
                .filter(Objects::nonNull)
                .map(AnalysisCallbackFixExample::language)
                .filter(StringUtils::hasText)
                .findFirst()
                .map(language -> limit(language, 50))
                .orElse(null);
    }

    private String defaultText(String primary, String secondary, String fallback) {
        if (StringUtils.hasText(primary)) {
            return primary;
        }
        if (StringUtils.hasText(secondary)) {
            return secondary;
        }
        return fallback;
    }

    private String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    // FastAPI 콜백에서 선택 배열 필드가 null로 오더라도 빈 배열처럼 처리
    private <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : values;
    }
}
