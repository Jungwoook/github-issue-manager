package com.jw.github_issue_manager;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import com.jw.github_issue_manager.comment.api.CommentFacade;
import com.jw.github_issue_manager.connection.api.PlatformConnectionFacade;
import com.jw.github_issue_manager.comment.internal.domain.CommentCache;
import com.jw.github_issue_manager.issue.internal.domain.IssueCache;
import com.jw.github_issue_manager.connection.internal.domain.PlatformConnection;
import com.jw.github_issue_manager.repository.internal.domain.RepositoryCache;
import com.jw.github_issue_manager.application.sync.SyncState;
import com.jw.github_issue_manager.connection.internal.domain.User;
import com.jw.github_issue_manager.issue.api.IssueFacade;
import com.jw.github_issue_manager.repository.api.RepositoryFacade;

@SpringBootTest
class GithubIssueManagerApplicationTests {

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private EntityManager entityManager;

	@Test
	void contextLoads() {
	}

	@Test
	void moduleFacadeBeansAreRegistered() {
		assertNotNull(applicationContext.getBean(PlatformConnectionFacade.class));
		assertNotNull(applicationContext.getBean(RepositoryFacade.class));
		assertNotNull(applicationContext.getBean(IssueFacade.class));
		assertNotNull(applicationContext.getBean(CommentFacade.class));
	}

	@Test
	void moduleEntitiesAreManagedByJpa() {
		Set<Class<?>> managedEntities = entityManager.getMetamodel().getEntities().stream()
			.map(entity -> entity.getJavaType())
			.collect(Collectors.toSet());

		assertTrue(managedEntities.containsAll(Set.of(
			PlatformConnection.class,
			User.class,
			RepositoryCache.class,
			IssueCache.class,
			CommentCache.class,
			SyncState.class
		)));
	}

}
