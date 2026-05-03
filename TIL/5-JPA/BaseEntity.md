옵션 A — @EntityListeners 제거 (현재 Hibernate 방식으로만 동작, 이걸로 충분)

@Getter                                                                                                                                                                                                
@MappedSuperclass                                                                                                                                                                                      
public class BaseEntity {                                                                                                                                                                              
@CreationTimestamp                                                                                                                                                                                 
@Column(nullable = false, updatable = false)

      @UpdateTimestamp
      @Column(nullable = false)                                                                                                                                                                          
      private LocalDateTime updatedAt;
}

옵션 B — @EnableJpaAuditing 추가 + @CreationTimestamp/@UpdateTimestamp를 @CreatedDate/@LastModifiedDate로 교체 (Spring Data Auditing 방식으로 통일)

createdAt, updatedAt만 관리한다면 옵션 A가 더 단순하고 깔끔해요. 