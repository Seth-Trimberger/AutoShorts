package com.autoshorts.scraper.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "automation_state")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AutomationState {

    @Id
    @Column(length = 64)
    private String stateKey;

    @Column(nullable = false)
    private LocalDate runDate;

    @Column(nullable = false)
    private int runCount;
}
