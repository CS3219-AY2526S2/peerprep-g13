package com.g13cs3219.userservice.repositories;

import com.g13cs3219.userservice.model.History;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HistoryRepository extends JpaRepository<History, Long> {
    List<History> findByUserIdOrderByFinishedDateDesc(Long userId);
}
