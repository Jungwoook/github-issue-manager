package com.jw.github_issue_manager.application.sync.run;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncRunRepository extends JpaRepository<SyncRun, Long> {

    List<SyncRun> findTop50ByOrderByStartedAtDesc();
}
