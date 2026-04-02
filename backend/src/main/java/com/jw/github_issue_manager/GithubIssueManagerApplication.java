package com.jw.github_issue_manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class GithubIssueManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(GithubIssueManagerApplication.class, args);
	}

}
