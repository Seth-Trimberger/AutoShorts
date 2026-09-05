package com.autoshorts.scraper.repository;

import com.autoshorts.scraper.Model.AutomationState;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AutomationStateRepository extends JpaRepository<AutomationState, String> {
}
