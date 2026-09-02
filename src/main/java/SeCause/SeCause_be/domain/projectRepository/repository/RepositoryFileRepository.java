package SeCause.SeCause_be.domain.projectRepository.repository;

import SeCause.SeCause_be.domain.projectRepository.entity.RepositoryFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepositoryFileRepository extends JpaRepository<RepositoryFile, Long> {
}
