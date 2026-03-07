package com.school.portal.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "MerchRequest")
public class MerchRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RequestId")
    private Integer requestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "StudentId", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MerchItemId", nullable = false)
    private MerchItem merchItem;

    @Column(name = "RequestDate", nullable = false)
    private LocalDateTime requestDate;

    @Column(name = "Status", nullable = false)
    private Integer status;

    @Column(name = "FulfilledDate")
    private LocalDateTime fulfilledDate;

    @OneToOne(mappedBy = "merchRequest")
    private TransactionHistory transaction;

    @PrePersist
    protected void onCreate() {
        requestDate = LocalDateTime.now();
        if (status == null) status = 0;
    }

    public Integer getRequestId() { return requestId; }
    public void setRequestId(Integer requestId) { this.requestId = requestId; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public MerchItem getMerchItem() { return merchItem; }
    public void setMerchItem(MerchItem merchItem) { this.merchItem = merchItem; }

    public LocalDateTime getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDateTime requestDate) { this.requestDate = requestDate; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDateTime getFulfilledDate() { return fulfilledDate; }
    public void setFulfilledDate(LocalDateTime fulfilledDate) { this.fulfilledDate = fulfilledDate; }

    public TransactionHistory getTransaction() { return transaction; }
    public void setTransaction(TransactionHistory transaction) { this.transaction = transaction; }
}