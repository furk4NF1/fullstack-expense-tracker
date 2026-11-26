package com.portfolio.expensetracker.service;
import com.portfolio.expensetracker.dto.*;
import com.portfolio.expensetracker.entity.SavingsGoal;
import com.portfolio.expensetracker.repository.SavingsGoalRepository;
import com.portfolio.expensetracker.exception.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class SavingsGoalService {
 private final SavingsGoalRepository repository;
 public SavingsGoalService(SavingsGoalRepository repository) { this.repository=repository; }
 @Transactional(readOnly=true)
 public List<SavingsGoalResponse> list(Long owner) {
  return repository.findAllByOwnerIdOrderByIdDesc(owner).stream().map(this::response).toList();
 }
 public SavingsGoalResponse create(Long owner, SavingsGoalRequest request) {
  SavingsGoal goal=new SavingsGoal(owner);
  apply(goal, request);
  return response(repository.saveAndFlush(goal));
 }
 public SavingsGoalResponse update(Long owner, Long id, SavingsGoalRequest request) {
  SavingsGoal goal=find(owner,id);
  if (!Objects.equals(goal.getVersion(), request.version()))
   throw new ResourceInUseException("Hedef başka bir sekmede değişti. Listeyi yenileyip tekrar dene.");
  apply(goal,request);
  return response(repository.saveAndFlush(goal));
 }
 public void delete(Long owner, Long id) { repository.delete(find(owner,id)); }
 private SavingsGoal find(Long owner, Long id) {
  return repository.findByIdAndOwnerId(id,owner).orElseThrow(() -> new ResourceNotFoundException("Birikim hedefi bulunamadı."));
 }
 private void apply(SavingsGoal goal, SavingsGoalRequest r) {
  if (r.targetDate()!=null && (r.targetDate().getYear()<1900 || r.targetDate().getYear()>9999))
   throw new IllegalArgumentException("Hedef tarihi 1900–9999 yılları arasında olmalı.");
  goal.update(r.name().trim(),r.targetAmount(),r.savedAmount(),r.targetDate());
 }
 private SavingsGoalResponse response(SavingsGoal g) {
  return new SavingsGoalResponse(g.getId(),g.getName(),g.getTargetAmount(),g.getSavedAmount(),g.getTargetDate(),g.getVersion());
 }
}
