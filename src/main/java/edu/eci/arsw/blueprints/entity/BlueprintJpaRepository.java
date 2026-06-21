package edu.eci.arsw.blueprints.entity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;

public interface BlueprintJpaRepository extends JpaRepository<BlueprintEntity, Long> {

    Optional<BlueprintEntity> findByAuthorAndName(String author, String name);
    Set<BlueprintEntity> findByAuthor(String author);
    boolean existsByAuthorAndName(String author, String name);

}
