package de.ostfalia.snakecore.SnakeServer.persistance;

import de.ostfalia.snakecore.model.SpielDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Benjamin Wulfert
 *
 * CRUD-Repository for ORM / RDBMS related operations for the type: {@link SpielDefinition}
 */
@Repository
public interface SpielDefinitionRepository extends JpaRepository<SpielDefinition, Long> {


}
