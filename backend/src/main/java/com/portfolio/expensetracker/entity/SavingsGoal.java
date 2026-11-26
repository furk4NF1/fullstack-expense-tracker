package com.portfolio.expensetracker.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "savings_goals")
public class SavingsGoal {
 @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
 @Column(name="owner_id", nullable=false) private Long ownerId;
 @Column(nullable=false, length=120) private String name;
 @Column(name="target_amount", nullable=false, precision=12, scale=2) private BigDecimal targetAmount;
 @Column(name="saved_amount", nullable=false, precision=12, scale=2) private BigDecimal savedAmount;
 @Column(name="target_date") private LocalDate targetDate;
 @Version private Long version;
 protected SavingsGoal() {}
 public SavingsGoal(Long ownerId) { this.ownerId = ownerId; }
 public Long getId() { return id; }
 public String getName() { return name; }
 public BigDecimal getTargetAmount() { return targetAmount; }
 public BigDecimal getSavedAmount() { return savedAmount; }
 public LocalDate getTargetDate() { return targetDate; }
 public Long getVersion() { return version; }
 public void update(String name, BigDecimal target, BigDecimal saved, LocalDate date) {
  this.name=name; this.targetAmount=target; this.savedAmount=saved; this.targetDate=date;
 }
}
